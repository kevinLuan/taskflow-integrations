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
package cn.feiliu.taskflow.common.metadata.tasks;

import com.google.protobuf.Any;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Result of the task execution.
 */
@Data
public class TaskExecResult {

    public enum Status {
        IN_PROGRESS, FAILED, FAILED_WITH_TERMINAL_ERROR, COMPLETED
    }

    /*运行时生成的工作流实例id*/
    @NotEmpty(message = "Workflow Id cannot be null or empty")
    private String              workflowInstanceId;

    @NotEmpty(message = "Task ID cannot be null or empty")
    private String              taskId;

    private String              reasonForIncompletion;
    /**
     * When set to non-zero values, the task remains in the queue for the specified seconds before
     * sent back to the worker when polled. Useful for the long running task, where the task is
     * updated as IN_PROGRESS and should not be polled out of the queue for a specified amount of
     * time. (delayed queue implementation)
     *
     * @param callbackAfterSeconds Amount of time in seconds the task should be held in the queue
     * before giving it to a polling worker.
     */
    private long                callbackAfterSeconds;
    /*工作节点ID（可以是注解名，IP地址或任意其他有意义的标识符，可帮助识别执行任务的主机进程，以便进行故障排除）*/
    private String              workerId;
    /**
     * <b>IN_PROGRESS<b>的状态:用于长时间运行的任务，表示任务仍在进行中，应该在以后的时间再次检查。
     * 例如，当作业由另一个进程执行时，worker在DB中检查作业的状态。<p>
     * <b>FAILED, FAILED_WITH_TERMINAL_ERROR, COMPLETED<b>:任务的终端状态。当您不希望重试任务时，使用FAILED_WITH_TERMINAL_ERROR。
     */
    private Status              status;
    /*任务执行输出数据*/
    private Map<String, Object> outputData = new HashMap<>();

    private Any                 outputMessage;

    private List<TaskLog>       logs       = new CopyOnWriteArrayList<>();
    /*the path where the task output is stored in external storage*/
    private String              externalOutputPayloadStoragePath;

    private String              subWorkflowId;

    public TaskExecResult(ExecutingTask task) {
        this.workflowInstanceId = task.getWorkflowInstanceId();
        this.taskId = task.getTaskId();
        this.reasonForIncompletion = task.getReasonForIncompletion();
        this.callbackAfterSeconds = task.getCallbackAfterSeconds();
        this.workerId = task.getWorkerId();
        this.outputData = task.getOutputData();
        this.externalOutputPayloadStoragePath = task.getExternalOutputPayloadStoragePath();
        this.subWorkflowId = task.getSubWorkflowId();
        switch (task.getStatus()) {
            case CANCELED:
            case COMPLETED_WITH_ERRORS:
            case TIMED_OUT:
            case SKIPPED:
                this.status = Status.FAILED;
                break;
            case SCHEDULED:
                this.status = Status.IN_PROGRESS;
                break;
            default:
                this.status = Status.valueOf(task.getStatus().name());
                break;
        }
    }

    public TaskExecResult() {
    }

    public void setReasonForIncompletion(String reasonForIncompletion) {
        this.reasonForIncompletion = StringUtils.substring(reasonForIncompletion, 0, 500);
    }

    /**
     * 添加输出项
     *
     * @param key   输出字段
     * @param value 输出值
     */
    public TaskExecResult addOutputData(String key, Object value) {
        this.outputData.put(key, value);
        return this;
    }

    /**
     * @param log Log line to be added
     * @return Instance of TaskResult
     */
    public TaskExecResult log(String log) {
        this.logs.add(new TaskLog(taskId, log));
        return this;
    }

    public static TaskExecResult complete() {
        return newTaskResult(Status.COMPLETED);
    }

    public static TaskExecResult failed() {
        return newTaskResult(Status.FAILED);
    }

    public static TaskExecResult failed(String failureReason) {
        TaskExecResult result = newTaskResult(Status.FAILED);
        result.setReasonForIncompletion(failureReason);
        return result;
    }

    public static TaskExecResult inProgress() {
        return newTaskResult(Status.IN_PROGRESS);
    }

    public static TaskExecResult newTaskResult(Status status) {
        TaskExecResult result = new TaskExecResult();
        result.setStatus(status);
        return result;
    }

    /**
     * Copy the given task result object
     *
     * @return a deep copy of the task result object except the externalOutputPayloadStoragePath
     * field
     */
    public TaskExecResult copy() {
        TaskExecResult taskExecResult = new TaskExecResult();
        taskExecResult.setReasonForIncompletion(reasonForIncompletion);
        taskExecResult.setWorkflowInstanceId(workflowInstanceId);
        taskExecResult.setTaskId(taskId);
        taskExecResult.setCallbackAfterSeconds(callbackAfterSeconds);
        taskExecResult.setWorkerId(workerId);
        taskExecResult.setStatus(status);
        taskExecResult.setOutputData(outputData);
        taskExecResult.setOutputMessage(outputMessage);
        taskExecResult.setLogs(logs);
        taskExecResult.setExternalOutputPayloadStoragePath(externalOutputPayloadStoragePath);
        taskExecResult.setSubWorkflowId(subWorkflowId);
        return taskExecResult;
    }
}
