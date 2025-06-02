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
package cn.feiliu.taskflow.client;

import cn.feiliu.common.api.encoder.EncoderFactory;
import cn.feiliu.taskflow.common.dto.tasks.ExecutingTask;
import cn.feiliu.taskflow.common.dto.tasks.TaskExecResult;
import cn.feiliu.taskflow.common.dto.tasks.TaskLog;
import cn.feiliu.taskflow.common.enums.TaskUpdateStatus;
import cn.feiliu.taskflow.http.TaskResourceApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务客户端类
 */
public class TaskClient {

    // API客户端实例
    protected ApiClient     apiClient;

    // 任务资源API实例
    private TaskResourceApi taskResourceApi;

    /**
     * 构造函数
     *
     * @param apiClient API客户端实例
     */
    public TaskClient(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.taskResourceApi = new TaskResourceApi(apiClient);
    }

    /**
     * 获取API客户端实例
     *
     * @return API客户端实例
     */
    public ApiClient getApiClient() {
        return apiClient;
    }

    /**
     * 轮询单个任务
     *
     * @param taskType 任务类型
     * @param workerId 工作节点ID
     * @param domain   域
     * @return 待执行的任务, 如果没有任务则返回null
     */
    public ExecutingTask pollTask(String taskType, String workerId, String domain) {
        int timeout = 100;
        List<ExecutingTask> tasks = batchPollTasksInDomain(taskType, domain, workerId, 1, timeout);
        if (tasks == null || tasks.isEmpty()) {
            return null;
        }
        return tasks.get(0);
    }

    /**
     * 批量轮询指定类型的任务
     *
     * @param taskType             任务类型
     * @param workerId             工作节点ID
     * @param count                获取任务数量
     * @param timeoutInMillisecond 超时时间(毫秒)
     * @return 待执行的任务列表
     */
    public List<ExecutingTask> batchPollTasksByTaskType(String taskType, String workerId, int count,
                                                        int timeoutInMillisecond) {
        return batchPollTasksInDomain(taskType, null, workerId, count, timeoutInMillisecond);
    }

    /**
     * 在指定域中批量轮询任务
     *
     * @param taskType             任务类型
     * @param domain               域
     * @param workerId             工作节点ID
     * @param count                获取任务数量
     * @param timeoutInMillisecond 超时时间(毫秒)
     * @return 待执行的任务列表
     */
    public List<ExecutingTask> batchPollTasksInDomain(String taskType, String domain, String workerId, int count,
                                                      int timeoutInMillisecond) {
        return taskResourceApi.batchPoll(taskType, workerId, domain, count, timeoutInMillisecond);
    }

    /**
     * 更新任务执行结果
     *
     * @param taskResult 任务执行结果
     */
    public void updateTask(TaskExecResult taskResult) {
        taskResourceApi.updateTask(taskResult);
    }

    /**
     * 根据给定的工作流id和任务引用名称更新任务状态和输出
     *
     * @param workflowId        工作流ID
     * @param taskReferenceName 需要更新任务的引用名称
     * @param status            任务状态
     * @param output            任务输出(Map格式)
     */
    public void updateTask(String workflowId, String taskReferenceName, TaskUpdateStatus status,
                           Map<String, Object> output) {
        taskResourceApi.updateTaskByRefName(output, workflowId, taskReferenceName, status.name());
    }

    /**
     * 根据给定的工作流id和任务引用名称更新任务状态和输出
     *
     * @param workflowId        工作流ID
     * @param taskReferenceName 需要更新任务的引用名称
     * @param status            任务状态
     * @param output            任务输出(Object格式,会被转换为Map)
     */
    public void updateTask(String workflowId, String taskReferenceName, TaskUpdateStatus status, Object output) {
        Map<String, Object> outputMap = new HashMap<>();
        try {
            outputMap = EncoderFactory.getJsonEncoder().convert(output, Map.class);
        } catch (Exception e) {
            outputMap.put("result", output);
        }
        taskResourceApi.updateTaskByRefName(outputMap, workflowId, taskReferenceName, status.name());
    }

    /**
     * 为指定任务记录日志
     *
     * @param taskId     任务ID
     * @param logMessage 日志消息
     */
    public void logMessageForTask(String taskId, String logMessage) {
        taskResourceApi.log(logMessage, taskId);
    }

    /**
     * 获取指定任务的日志列表
     *
     * @param taskId 任务ID
     * @return 任务日志列表
     */
    public List<TaskLog> getTaskLogs(String taskId) {
        return taskResourceApi.getTaskLogs(taskId);
    }

    /**
     * 获取任务详情
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    public ExecutingTask getTaskDetails(String taskId) {
        return taskResourceApi.getTask(taskId);
    }

    /**
     * 重新排队指定类型的待处理任务
     *
     * @param taskType 任务类型
     * @return 操作结果
     */
    public String requeuePendingTasksByTaskType(String taskType) {
        return taskResourceApi.requeuePendingTask(taskType);
    }
}
