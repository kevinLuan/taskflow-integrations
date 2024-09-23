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
package cn.feiliu.taskflow.client;

import cn.feiliu.taskflow.client.http.api.TaskResourceApi;
import cn.feiliu.taskflow.client.api.ITaskClient;
import cn.feiliu.taskflow.sdk.config.PropertyFactory;
import cn.feiliu.taskflow.serialization.SerializerFactory;
import cn.feiliu.taskflow.common.metadata.tasks.ExecutingTask;
import cn.feiliu.taskflow.common.metadata.tasks.TaskLog;
import cn.feiliu.taskflow.common.metadata.tasks.TaskExecResult;

import java.util.*;

public class TaskClient implements ITaskClient {

    protected ApiClient     apiClient;

    private TaskResourceApi taskResourceApi;

    public TaskClient(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.taskResourceApi = new TaskResourceApi(apiClient);
    }

    public TaskClient withReadTimeout(int readTimeout) {
        apiClient.setReadTimeout(readTimeout);
        return this;
    }

    public TaskClient setWriteTimeout(int writeTimeout) {
        apiClient.setWriteTimeout(writeTimeout);
        return this;
    }

    public TaskClient withWriteTimeout(int writeTimeout) {
        apiClient.setWriteTimeout(writeTimeout);
        return this;
    }

    public TaskClient withConnectTimeout(int connectTimeout) {
        apiClient.setConnectTimeout(connectTimeout);
        return this;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    @Override
    public ExecutingTask pollTask(String taskType, String workerId, String domain) {
        int timeout = PropertyFactory.getInteger(taskType, "batchPollTimeoutInMS", 1000);
        List<ExecutingTask> tasks = batchPollTasksInDomain(taskType, domain, workerId, 1, timeout);
        if (tasks == null || tasks.isEmpty()) {
            return null;
        }
        return tasks.get(0);
    }

    @Override
    public List<ExecutingTask> batchPollTasksByTaskType(String taskType, String workerId, int count,
                                                        int timeoutInMillisecond) {
        return batchPollTasksInDomain(taskType, null, workerId, count, timeoutInMillisecond);
    }

    @Override
    public List<ExecutingTask> batchPollTasksInDomain(String taskType, String domain, String workerId, int count,
                                                      int timeoutInMillisecond) {
        if (apiClient.isUseGRPC()) {
            return apiClient.getGrpcApi().batchPollTask(taskType, workerId, domain, count, timeoutInMillisecond);
        } else {
            return taskResourceApi.batchPoll(taskType, workerId, domain, count, timeoutInMillisecond);
        }
    }

    @Override
    public void updateTask(TaskExecResult taskResult) {
        if (apiClient.isUseGRPC()) {
            apiClient.getGrpcApi().updateTask(taskResult);
        } else {
            taskResourceApi.updateTask(taskResult);
        }
    }

    /**
     * 根据给定的工作流id和任务引用名称更新任务状态和输出
     *
     * @param workflowId        工作流ID
     * @param taskReferenceName 需要更新任务的引用名称
     * @param status            任务状态
     * @param output            任务输出
     */
    @Override
    public void updateTask(String workflowId, String taskReferenceName, TaskExecResult.Status status,
                           Map<String, Object> output) {
        taskResourceApi.updateTaskByRefName(output, workflowId, taskReferenceName, status.toString());
    }

    @Override
    public void updateTask(String workflowId, String taskReferenceName, TaskExecResult.Status status, Object output) {
        Map<String, Object> outputMap = new HashMap<>();
        try {
            outputMap = SerializerFactory.getSerializer().convertMap(output);
        } catch (Exception e) {
            outputMap.put("result", output);
        }
        taskResourceApi.updateTaskByRefName(outputMap, workflowId, taskReferenceName, status.toString());
    }

    @Override
    public void logMessageForTask(String taskId, String logMessage) {
        taskResourceApi.log(logMessage, taskId);
    }

    @Override
    public List<TaskLog> getTaskLogs(String taskId) {
        return taskResourceApi.getTaskLogs(taskId);
    }

    @Override
    public ExecutingTask getTaskDetails(String taskId) {
        return taskResourceApi.getTask(taskId);
    }

    @Override
    public String requeuePendingTasksByTaskType(String taskType) {
        return taskResourceApi.requeuePendingTask(taskType);
    }
}
