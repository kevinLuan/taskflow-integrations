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
import cn.feiliu.taskflow.annotations.InputParam;
import cn.feiliu.taskflow.common.def.tasks.DynamicFork;
import cn.feiliu.taskflow.common.def.tasks.DynamicForkInput;
import cn.feiliu.taskflow.common.dto.tasks.ExecutingTask;
import cn.feiliu.taskflow.common.dto.tasks.TaskExecResult;
import cn.feiliu.taskflow.common.enums.TaskUpdateStatus;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class DynamicForkWorker implements Worker {

    private final int                                pollingInterval;

    private final Function<Object, DynamicForkInput> workerMethod;

    private final String                             name;

    public DynamicForkWorker(String name, Function<Object, DynamicForkInput> workerMethod, int pollingInterval) {
        this.name = name;
        this.workerMethod = workerMethod;
        this.pollingInterval = pollingInterval;
    }

    @Override
    public String getTaskDefName() {
        return name;
    }

    @Override
    public TaskExecResult execute(ExecutingTask task) throws Throwable {
        TaskExecResult result = new TaskExecResult(task);
        Object parameter = getInvocationParameters(this.workerMethod, task);
        DynamicForkInput output = this.workerMethod.apply(parameter);
        result.getOutputData().put(DynamicFork.FORK_TASK_PARAM, output.getTasks());
        result.getOutputData().put(DynamicFork.FORK_TASK_INPUT_PARAM, output.getInputs());
        result.setStatus(TaskUpdateStatus.COMPLETED);
        return result;
    }

    @Override
    public int getPollingInterval() {
        return pollingInterval;
    }

    @Override
    public Optional<String[]> getInputNames() {
        return Optional.empty();
    }

    @Override
    public Optional<String[]> getOutputNames() {
        return Optional.of(new String[] { DynamicFork.FORK_TASK_PARAM, DynamicFork.FORK_TASK_INPUT_PARAM });
    }

    private Object getInvocationParameters(Function<?, DynamicForkInput> function, ExecutingTask task) {
        InputParam annotation = null;
        Class<?> parameterType = null;
        for (Method method : function.getClass().getDeclaredMethods()) {
            if (method.getReturnType().equals(DynamicForkInput.class)) {
                annotation = method.getParameters()[0].getAnnotation(InputParam.class);
                parameterType = method.getParameters()[0].getType();
            }
        }

        if (parameterType.equals(ExecutingTask.class)) {
            return task;
        } else if (parameterType.equals(Map.class)) {
            return task.getInputData();
        }
        if (annotation != null) {
            String name = annotation.value();
            Object value = task.getInputData().get(name);
            return EncoderFactory.getJsonEncoder().convert(value, parameterType);
        }
        return EncoderFactory.getJsonEncoder().convert(task.getInputData(), parameterType);
    }

    @Override
    public String getTag() {
        return "动态ForkWorker";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean isOpen() {
        return false;
    }
}
