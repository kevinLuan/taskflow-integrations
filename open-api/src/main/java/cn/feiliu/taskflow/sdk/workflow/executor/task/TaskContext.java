/*
 * Copyright 2024 taskflow, Inc.
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

import cn.feiliu.taskflow.common.metadata.tasks.ExecutingTask;
import cn.feiliu.taskflow.common.metadata.tasks.TaskExecResult;

/** Context for the task */
public class TaskContext {

    public static final ThreadLocal<TaskContext> TASK_CONTEXT_INHERITABLE_THREAD_LOCAL =
            InheritableThreadLocal.withInitial(() -> null);

    public TaskContext(ExecutingTask task, TaskExecResult taskResult) {
        this.task = task;
        this.taskResult = taskResult;
    }

    public static TaskContext get() {
        return TASK_CONTEXT_INHERITABLE_THREAD_LOCAL.get();
    }

    public static TaskContext set(ExecutingTask task) {
        TaskExecResult result = new TaskExecResult(task);
        TaskContext context = new TaskContext(task, result);
        TASK_CONTEXT_INHERITABLE_THREAD_LOCAL.set(context);
        return context;
    }

    private final ExecutingTask task;

    private final TaskExecResult taskResult;

    public String getWorkflowInstanceId() {
        return task.getWorkflowInstanceId();
    }

    public String getTaskId() {
        return task.getTaskId();
    }

    public int getRetryCount() {
        return task.getRetryCount();
    }

    public int getPollCount() {
        return task.getPollCount();
    }

    public long getCallbackAfterSeconds() {
        return task.getCallbackAfterSeconds();
    }

    public void addLog(String log) {
        this.taskResult.log(log);
    }

    public ExecutingTask getTask() {
        return task;
    }

    public TaskExecResult getTaskResult() {
        return taskResult;
    }

    public void setCallbackAfter(int seconds) {
        this.taskResult.setCallbackAfterSeconds(seconds);
    }
}
