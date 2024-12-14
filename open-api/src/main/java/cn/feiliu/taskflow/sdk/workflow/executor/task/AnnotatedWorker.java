/*
 * Copyright 2024 Taskflow, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.feiliu.taskflow.sdk.workflow.executor.task;

import cn.feiliu.common.api.utils.CommonUtils;
import cn.feiliu.taskflow.common.enums.TaskUpdateStatus;
import cn.feiliu.taskflow.serialization.SerializerFactory;
import cn.feiliu.taskflow.sdk.worker.Worker;
import cn.feiliu.taskflow.common.metadata.tasks.ExecutingTask;
import cn.feiliu.taskflow.common.metadata.tasks.TaskExecResult;
import cn.feiliu.taskflow.common.metadata.workflow.FlowTask;
import cn.feiliu.taskflow.sdk.workflow.def.tasks.DynamicFork;
import cn.feiliu.taskflow.sdk.workflow.def.tasks.DynamicForkInput;
import cn.feiliu.taskflow.sdk.workflow.task.InputParam;
import cn.feiliu.taskflow.sdk.workflow.task.OutputParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import static cn.feiliu.common.api.utils.CommonUtils.f;

public class AnnotatedWorker implements Worker {
    private static Logger         log             = LoggerFactory.getLogger(AnnotatedWorker.class);
    private String                name;

    private Method                workerMethod;

    private Object                obj;

    private int                   pollingInterval = 100;

    private Set<TaskUpdateStatus> failedStatuses  = Set.of(TaskUpdateStatus.FAILED,
                                                      TaskUpdateStatus.FAILED_WITH_TERMINAL_ERROR);

    public AnnotatedWorker(String name, Method workerMethod, Object obj) {
        this.name = name;
        this.workerMethod = workerMethod;
        this.obj = obj;
    }

    @Override
    public String getTaskDefName() {
        return name;
    }

    @Override
    public TaskExecResult execute(ExecutingTask task) throws Throwable {
        TaskExecResult result = null;
        try {
            TaskContext context = TaskContext.set(task);
            Object[] parameters = getInvocationParameters(task);
            Object invocationResult = workerMethod.invoke(obj, parameters);
            result = setValue(invocationResult, context.getTaskResult());
            if (!failedStatuses.contains(result.getStatus()) && result.getCallbackAfterSeconds() > 0) {
                result.setStatus(TaskUpdateStatus.IN_PROGRESS);
            }
        } catch (IllegalArgumentException e) {
            if (result == null) {
                result = new TaskExecResult(task);
            }
            result.setStatus(TaskUpdateStatus.FAILED);
            result.setReasonForIncompletion(e.getMessage());
        } catch (InvocationTargetException invocationTargetException) {
            if (result == null) {
                result = new TaskExecResult(task);
            }
            Throwable e = invocationTargetException.getCause();
            log.error("invocation error", e);
            if (e instanceof NonRetryableException) {
                result.setStatus(TaskUpdateStatus.FAILED_WITH_TERMINAL_ERROR);
            } else {
                result.setStatus(TaskUpdateStatus.FAILED);
            }
            result.setReasonForIncompletion(e.getMessage());
            result.log(CommonUtils.dumpFullStackTrace(e));
        } catch (Throwable e) {
            throw e;
        }
        return result;
    }

    private Object[] getInvocationParameters(ExecutingTask task) {
        Class<?>[] parameterTypes = workerMethod.getParameterTypes();
        Parameter[] parameters = workerMethod.getParameters();

        if (parameterTypes.length == 1 && parameterTypes[0].equals(ExecutingTask.class)) {
            return new Object[] { task };
        } else if (parameterTypes.length == 1 && parameterTypes[0].equals(Map.class)) {
            //工作节点参数定义只接收一个Map参数的情况下，尝试检查是否包含@InputParam注解，若包含应该根据注解名称来提取数据
            Optional<InputParam> optional = findInputParamAnnotation(workerMethod.getParameterAnnotations()[0]);
            if (optional.isEmpty()) {
                return new Object[] { task.getInputData() };
            }
        }
        return getParameters(task, parameterTypes, parameters);
    }

    private Object[] getParameters(ExecutingTask task, Class<?>[] parameterTypes, Parameter[] parameters) {
        Annotation[][] parameterAnnotations = workerMethod.getParameterAnnotations();
        Object[] values = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Annotation[] paramAnnotation = parameterAnnotations[i];
            if (paramAnnotation != null && paramAnnotation.length > 0) {
                Type type = parameters[i].getParameterizedType();
                Class<?> parameterType = parameterTypes[i];
                values[i] = getInputValue(task, parameterType, type, paramAnnotation);
            } else {
                values[i] = SerializerFactory.getSerializer().convert(task.getInputData(), parameterTypes[i]);
            }
        }

        return values;
    }

    private Object getInputValue(ExecutingTask task, Class<?> parameterType, Type type, Annotation[] paramAnnotation) {
        Optional<InputParam> optional = findInputParamAnnotation(paramAnnotation);
        if (optional.isEmpty()) {
            return SerializerFactory.getSerializer().convert(task.getInputData(), parameterType);
        }
        InputParam inputParam = optional.get();
        final Object value = task.getInputData().get(inputParam.value());
        if (value == null) {
            if (inputParam.required()) {
                throw new IllegalArgumentException(f("The required %s('%s') parameter is missing", name,
                    inputParam.value()));
            }
            return null;
        }
        if (List.class.isAssignableFrom(parameterType)) {
            List<?> list = safeConvertList(inputParam, value);
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Class<?> typeOfParameter = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                List<Object> parameterizedList = new ArrayList<>();
                for (Object item : list) {
                    parameterizedList.add(safeConvert(inputParam, item, typeOfParameter));
                }

                return parameterizedList;
            } else {
                return list;
            }
        } else {
            return safeConvert(inputParam, value, parameterType);
        }
    }

    private List<?> safeConvertList(InputParam ip, Object value) {
        try {
            return SerializerFactory.getSerializer().convertList(value);
        } catch (Throwable t) {
            String msg = String.format("The required %s('%s') parameter is missing", name, ip.value());
            throw new IllegalArgumentException(msg, t);
        }
    }

    private Object safeConvert(InputParam ip, Object value, Class<?> type) {
        try {
            return SerializerFactory.getSerializer().convert(value, type);
        } catch (Throwable e) {
            String msg = String.format("The required %s('%s') parameter is missing", name, ip.value());
            throw new IllegalArgumentException(msg, e);
        }
    }

    private static Optional<InputParam> findInputParamAnnotation(Annotation[] paramAnnotation) {
        for (Annotation annotation : paramAnnotation) {
            if (annotation.annotationType() == InputParam.class) {
                return Optional.of((InputParam) annotation);
            }
        }
        return Optional.empty();
    }

    private TaskExecResult setValue(Object invocationResult, TaskExecResult result) {
        if (invocationResult == null) {
            result.setStatus(TaskUpdateStatus.COMPLETED);
            return result;
        }
        OutputParam opAnnotation = workerMethod.getAnnotatedReturnType().getAnnotation(OutputParam.class);
        if (opAnnotation == null) {
            opAnnotation = workerMethod.getAnnotation(OutputParam.class);
        }
        if (opAnnotation != null) {
            String name = opAnnotation.value();
            result.getOutputData().put(name, invocationResult);
            result.setStatus(TaskUpdateStatus.COMPLETED);
            return result;
        } else if (invocationResult instanceof TaskExecResult) {
            return (TaskExecResult) invocationResult;
        } else if (invocationResult instanceof Map) {
            Map resultAsMap = (Map) invocationResult;
            result.getOutputData().putAll(resultAsMap);
            result.setStatus(TaskUpdateStatus.COMPLETED);
            return result;
        } else if (invocationResult instanceof String || invocationResult instanceof Number
                   || invocationResult instanceof Boolean) {
            result.getOutputData().put("result", invocationResult);
            result.setStatus(TaskUpdateStatus.COMPLETED);
            return result;
        } else if (invocationResult instanceof List) {

            List resultAsList = SerializerFactory.getSerializer().convertList(invocationResult);
            result.getOutputData().put("result", resultAsList);
            result.setStatus(TaskUpdateStatus.COMPLETED);
            return result;

        } else if (invocationResult instanceof DynamicForkInput) {
            DynamicForkInput forkInput = (DynamicForkInput) invocationResult;
            List<cn.feiliu.taskflow.sdk.workflow.def.tasks.Task<?>> tasks = forkInput.getTasks();
            List<FlowTask> workflowTasks = new ArrayList<>();
            for (cn.feiliu.taskflow.sdk.workflow.def.tasks.Task<?> sdkTask : tasks) {
                workflowTasks.addAll(sdkTask.getWorkflowDefTasks());
            }
            result.getOutputData().put(DynamicFork.FORK_TASK_PARAM, workflowTasks);
            result.getOutputData().put(DynamicFork.FORK_TASK_INPUT_PARAM, forkInput.getInputs());
            result.setStatus(TaskUpdateStatus.COMPLETED);
            return result;

        } else {
            Map resultAsMap = SerializerFactory.getSerializer().convertMap(invocationResult);
            result.getOutputData().putAll(resultAsMap);
            result.setStatus(TaskUpdateStatus.COMPLETED);
            return result;
        }
    }

    public void setPollingInterval(int pollingInterval) {
        log.info("Setting the polling interval for " + getTaskDefName() + ", to " + pollingInterval);
        this.pollingInterval = pollingInterval;
    }

    @Override
    public int getPollingInterval() {
        return pollingInterval;
    }
}
