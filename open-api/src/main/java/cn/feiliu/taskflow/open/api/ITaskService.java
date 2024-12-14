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
package cn.feiliu.taskflow.open.api;

import cn.feiliu.taskflow.common.enums.TaskUpdateStatus;
import cn.feiliu.taskflow.common.metadata.tasks.ExecutingTask;
import cn.feiliu.taskflow.common.metadata.tasks.TaskLog;
import cn.feiliu.taskflow.common.metadata.tasks.TaskExecResult;
import cn.feiliu.taskflow.common.utils.ExternalPayloadStorage;

import java.util.List;
import java.util.Map;

/**
 * Client for taskflow task management including polling for task, updating task status etc.
 */
public interface ITaskService {

    /**
     * Perform a poll for a task of a specific task type.
     *
     * @param taskType The taskType to poll for
     * @param domain   The domain of the task type
     * @param workerId Name of the client worker. Used for logging.
     * @return Task waiting to be executed.
     */
    ExecutingTask pollTask(String taskType, String workerId, String domain);

    /**
     * Perform a batch poll for tasks by task type. Batch size is configurable by count.
     *
     * @param taskType             Type of task to poll for
     * @param workerId             Name of the client worker. Used for logging.
     * @param count                Maximum number of tasks to be returned. Actual number of tasks returned can be
     *                             less than this number.
     * @param timeoutInMillisecond Long poll wait timeout.
     * @return List of tasks awaiting to be executed.
     */
    List<ExecutingTask> batchPollTasksByTaskType(String taskType, String workerId, int count, int timeoutInMillisecond);

    /**
     * Batch poll for tasks in a domain. Batch size is configurable by count.
     *
     * @param taskType             Type of task to poll for
     * @param domain               The domain of the task type
     * @param workerId             Name of the client worker. Used for logging.
     * @param count                Maximum number of tasks to be returned. Actual number of tasks returned can be
     *                             less than this number.
     * @param timeoutInMillisecond Long poll wait timeout.
     * @return List of tasks awaiting to be executed.
     */
    List<ExecutingTask> batchPollTasksInDomain(String taskType, String domain, String workerId, int count,
                                               int timeoutInMillisecond);

    /**
     * Updates the result of a task execution. If the size of the task output payload is bigger than
     * {@link ExternalPayloadStorage}, if enabled, else the task is marked as
     * FAILED_WITH_TERMINAL_ERROR.
     *
     * @param taskResult the {@link TaskExecResult} of the executed task to be updated.
     */
    void updateTask(TaskExecResult taskResult);

    //    Optional<String> evaluateAndUploadLargePayload(Map<String, Object> taskOutputData, String taskType);

    /**
     * Log execution messages for a task.
     *
     * @param taskId     id of the task
     * @param logMessage the message to be logged
     */
    void logMessageForTask(String taskId, String logMessage);

    /**
     * Fetch execution logs for a task.
     *
     * @param taskId id of the task.
     */
    List<TaskLog> getTaskLogs(String taskId);

    /**
     * Retrieve information about the task
     *
     * @param taskId ID of the task
     * @return Task details
     */
    ExecutingTask getTaskDetails(String taskId);

    /**
     * Requeue pending tasks of a specific task type
     *
     * @return returns the number of tasks that have been requeued
     */
    String requeuePendingTasksByTaskType(String taskType);

    /**
     * Update the task status and output based given workflow id and task reference name
     *
     * @param workflowId        Workflow Id
     * @param taskReferenceName Reference name of the task to be updated
     * @param status            Status of the task
     * @param output            Output for the task
     */
    void updateTask(String workflowId, String taskReferenceName, TaskUpdateStatus status, Map<String, Object> output);

}
