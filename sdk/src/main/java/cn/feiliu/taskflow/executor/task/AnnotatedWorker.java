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
package cn.feiliu.taskflow.executor.task;

import cn.feiliu.common.api.encoder.EncoderFactory;
import cn.feiliu.common.api.utils.CommonUtils;
import cn.feiliu.taskflow.annotations.InputParam;
import cn.feiliu.taskflow.annotations.OutputParam;
import cn.feiliu.taskflow.common.def.FlowTask;
import cn.feiliu.taskflow.common.def.tasks.DynamicFork;
import cn.feiliu.taskflow.common.def.tasks.DynamicForkInput;
import cn.feiliu.taskflow.common.def.tasks.Task;
import cn.feiliu.taskflow.common.dto.tasks.ExecutingTask;
import cn.feiliu.taskflow.common.dto.tasks.TaskExecResult;
import cn.feiliu.taskflow.common.enums.TaskUpdateStatus;
import cn.feiliu.taskflow.utils.FieldUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import static cn.feiliu.common.api.utils.CommonUtils.f;

public class AnnotatedWorker implements Worker {
    private static Logger         log             = LoggerFactory.getLogger(AnnotatedWorker.class);
    private WorkerWrapper         workerWrapper;

    private Method                workerMethod;

    private Object                obj;

    private int                   pollingInterval = 100;

    private Set<TaskUpdateStatus> failedStatuses  = Sets.newHashSet(TaskUpdateStatus.FAILED,
                                                      TaskUpdateStatus.FAILED_WITH_TERMINAL_ERROR);

    public AnnotatedWorker(WorkerWrapper workerWrapper, Method workerMethod, Object obj) {
        this.workerWrapper = workerWrapper;
        this.workerMethod = workerMethod;
        this.obj = obj;
    }

    /**
     * 获取输入参数名称
     *
     * @return
     */
    public Optional<String[]> getInputNames() {
        List<String> names = Lists.newArrayList();
        if (workerMethod.getParameterCount() == 1) {
            Class<?> type = workerMethod.getParameterTypes()[0];
            if (type == ExecutingTask.class) {
                return Optional.empty();
            } else {
                if (Map.class.isAssignableFrom(type)) {
                    if (!workerMethod.isAnnotationPresent(InputParam.class)) {
                        return Optional.empty();
                    }
                }
            }
        }
        for (Parameter parameter : workerMethod.getParameters()) {
            if (parameter.isAnnotationPresent(InputParam.class)) {
                names.add(parameter.getAnnotation(InputParam.class).value());
            } else {
                throw new IllegalStateException(String.format("工作任务：'%s' ，参数：'%s' 缺少 @InputParam 注解", getTaskDefName(),
                    parameter.getName()));
            }
        }
        return Optional.of(names.toArray(new String[names.size()]));
    }

    /**
     * 获取输出名称
     *
     * @return
     */
    public Optional<String[]> getOutputNames() {
        if (workerMethod.getReturnType() == void.class) {
            return Optional.of(new String[0]);
        }
        OutputParam opAnnotation = workerMethod.getAnnotatedReturnType().getAnnotation(OutputParam.class);
        if (opAnnotation == null) {
            opAnnotation = workerMethod.getAnnotation(OutputParam.class);
        }
        if (opAnnotation != null) {
            return Optional.of(new String[] { opAnnotation.value() });
        } else {
            if (TaskHelper.isJavaType(workerMethod.getReturnType())) {
                return Optional.of(new String[] { "result" });
            } else if (TaskExecResult.class.isAssignableFrom(workerMethod.getReturnType())) {
                return Optional.empty();
            } else {
                String[] array = FieldUtils.getJavaFieldNames(workerMethod.getReturnType());
                if (array != null && array.length > 0) {
                    return Optional.of(array);
                } else {
                    return Optional.of(new String[] { "result" });
                }
            }
        }
    }

    @Override
    public String getTag() {
        return workerWrapper.tag();
    }

    @Override
    public String getDescription() {
        return workerWrapper.description();
    }

    @Override
    public String getTaskDefName() {
        return workerWrapper.value();
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
            if (!optional.isPresent()) {
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
                values[i] = EncoderFactory.getJsonEncoder().convert(task.getInputData(), parameterTypes[i]);
            }
        }

        return values;
    }

    private Object getInputValue(ExecutingTask task, Class<?> parameterType, Type type, Annotation[] paramAnnotation) {
        Optional<InputParam> optional = findInputParamAnnotation(paramAnnotation);
        if (!optional.isPresent()) {
            return EncoderFactory.getJsonEncoder().convert(task.getInputData(), parameterType);
        }
        InputParam inputParam = optional.get();
        final Object value = task.getInputData().get(inputParam.value());
        if (value == null) {
            if (inputParam.required()) {
                throw new IllegalArgumentException(String.format("缺少必须得参数：'%s'", inputParam.value()));
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
            return EncoderFactory.getJsonEncoder().convert(value, List.class);
        } catch (Throwable t) {
            String msg = String.format("数据转换 List 类型出错, 参数:'%s', 数据: `%s`", ip.value(), value);
            throw new IllegalArgumentException(msg, t);
        }
    }

    private Object safeConvert(InputParam ip, Object value, Class<?> type) {
        try {
            return EncoderFactory.getJsonEncoder().convert(value, type);
        } catch (Throwable e) {
            String msg = String.format("数据转换出错，参数:'%s', 类型: '%s', 数据: `%s`", ip.value(), type, value);
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
            result.getOutputData().put("result", invocationResult);
            result.setStatus(TaskUpdateStatus.COMPLETED);
            return result;

        } else if (invocationResult instanceof DynamicForkInput) {
            DynamicForkInput forkInput = (DynamicForkInput) invocationResult;
            List<Task<?>> tasks = forkInput.getTasks();
            List<FlowTask> workflowTasks = new ArrayList<>();
            for (Task<?> sdkTask : tasks) {
                workflowTasks.addAll(sdkTask.getWorkflowDefTasks());
            }
            result.getOutputData().put(DynamicFork.FORK_TASK_PARAM, workflowTasks);
            result.getOutputData().put(DynamicFork.FORK_TASK_INPUT_PARAM, forkInput.getInputs());
            result.setStatus(TaskUpdateStatus.COMPLETED);
            return result;

        } else {
            Map resultAsMap = EncoderFactory.getJsonEncoder().convert(invocationResult, Map.class);
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
