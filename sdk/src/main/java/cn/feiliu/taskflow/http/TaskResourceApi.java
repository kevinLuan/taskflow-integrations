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
package cn.feiliu.taskflow.http;

import cn.feiliu.common.api.model.resp.DataResult;
import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.common.dto.tasks.ExecutingTask;
import cn.feiliu.taskflow.common.dto.tasks.PollData;
import cn.feiliu.taskflow.common.dto.tasks.TaskExecResult;
import cn.feiliu.taskflow.common.dto.tasks.TaskLog;
import cn.feiliu.taskflow.common.exceptions.ApiException;
import cn.feiliu.taskflow.http.types.TypeFactory;
import cn.feiliu.taskflow.utils.Assertion;
import cn.feiliu.taskflow.utils.ClientHelper;
import com.google.common.collect.Lists;
import okhttp3.Call;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.feiliu.common.api.utils.CommonUtils.f;

/**
 * 任务资源API类
 * 提供任务相关的操作接口,包括任务轮询、查询、更新等功能
 */
public class TaskResourceApi {
    private ApiClient apiClient;

    public TaskResourceApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 批量轮询指定类型的任务
     *
     * @param taskType 任务类型（必填）
     * @param workerId 工作节点ID（可选）
     * @param domain   域（可选）
     * @param count    获取任务数量（可选，默认为1）
     * @param timeout  超时时间（可选，默认为100）
     * @return List<Task> 任务列表
     * @throws ApiException 如果调用API失败，例如服务器错误或无法反序列化响应体
     */
    public List<ExecutingTask> batchPoll(String taskType, String workerId, String domain, Integer count, Integer timeout)
                                                                                                                         throws ApiException {
        Assertion.assertNotNull(taskType, "taskType");
        String path = f("/tasks/poll/batch/%s", taskType);
        List<Pair> queryParams = new ArrayList<>();
        if (workerId != null)
            queryParams.addAll(ClientHelper.parameterToPair("workerId", workerId));
        if (domain != null)
            queryParams.addAll(ClientHelper.parameterToPair("domain", domain));
        if (count != null)
            queryParams.addAll(ClientHelper.parameterToPair("count", count));
        if (timeout != null)
            queryParams.addAll(ClientHelper.parameterToPair("timeout", timeout));
        Call call = apiClient.buildGetCall(path, queryParams);
        DataResult<List<ExecutingTask>> resp = apiClient.doExecute(call, TypeFactory.ofList(ExecutingTask.class));
        return resp.getData();
    }

    /**
     * 获取所有任务类型的最新轮询数据
     *
     * @return List<PollData> 轮询数据列表
     * @throws ApiException 如果调用API失败，例如服务器错误或无法反序列化响应体
     */
    public List<PollData> getAllPollData() throws ApiException {
        String path = "/tasks/queue/polldata/all";
        Call call = apiClient.buildGetCall(path, new ArrayList<>());
        DataResult<List<PollData>> resp = apiClient.doExecute(call, TypeFactory.ofList(PollData.class));
        return resp.getData();
    }

    /**
     * 获取给定任务类型的最后一次轮询数据
     *
     * @param taskType 任务类型（必填）
     * @return List<PollData> 轮询数据列表
     * @throws ApiException 如果调用API失败
     */
    public List<PollData> getPollData(String taskType) throws ApiException {
        if (taskType == null) {
            throw new ApiException("Missing the required parameter 'taskType'");
        }
        String path = "/tasks/queue/polldata";
        List<Pair> queryParams = Lists.newArrayList(ClientHelper.parameterToPair("taskType", taskType));
        Call call = apiClient.buildGetCall(path, queryParams);
        DataResult<List<PollData>> resp = apiClient.doExecute(call, TypeFactory.ofList(PollData.class));
        return resp.getData();
    }

    /**
     * 按Id获取任务
     *
     * @param taskId 任务ID（必填）
     * @return ExecutingTask 任务详情
     * @throws ApiException 如果调用API失败
     */
    public ExecutingTask getTask(String taskId) throws ApiException {
        if (taskId == null) {
            throw new ApiException("Missing the required parameter 'taskId'");
        }
        String localVarPath = f("/tasks/%s", taskId);
        Call call = apiClient.buildGetCall(localVarPath, new ArrayList<>());
        DataResult<ExecutingTask> resp = apiClient.execute(call, ExecutingTask.class);
        return resp.getData();
    }

    /**
     * 获取任务执行日志
     *
     * @param taskId 任务ID（必填）
     * @return List<TaskLog> 任务日志列表
     * @throws ApiException 如果调用API失败
     */
    public List<TaskLog> getTaskLogs(String taskId) throws ApiException {
        Assertion.assertNotNull(taskId, "taskId");
        String path = f("/tasks/%s/log", taskId);
        Call call = apiClient.buildGetCall(path, new ArrayList<>());
        DataResult<List<TaskLog>> resp = apiClient.doExecute(call, TypeFactory.ofList(TaskLog.class));
        return resp.getData();
    }

    /**
     * 记录任务执行日志
     *
     * @param body   日志内容
     * @param taskId 任务ID
     * @throws ApiException 如果调用API失败
     */
    public void log(String body, String taskId) throws ApiException {
        Assertion.assertNotNull(body, "body");
        Assertion.assertNotNull(taskId, "taskId");
        String path = f("/tasks/%s/log/record", taskId);
        Call call = apiClient.buildPostCall(path, body);
        apiClient.execute(call);
    }

    /**
     * 轮询某一类型的任务
     *
     * @param taskType 任务类型（必填）
     * @param workerId 工作节点ID（可选）
     * @param domain   域（可选）
     * @return ExecutingTask 待执行的任务
     * @throws ApiException 如果调用API失败
     */
    public ExecutingTask poll(String taskType, String workerId, String domain) throws ApiException {
        Assertion.assertNotNull(taskType, "taskType");
        String path = f("/tasks/poll/%s", taskType);
        List<Pair> queryParams = new ArrayList<>();
        if (workerId != null)
            queryParams.addAll(ClientHelper.parameterToPair("workerId", workerId));
        if (domain != null)
            queryParams.addAll(ClientHelper.parameterToPair("domain", domain));
        Call call = apiClient.buildGetCall(path, queryParams);
        DataResult<ExecutingTask> resp = apiClient.execute(call, ExecutingTask.class);
        return resp.getData();
    }

    /**
     * 重新排队待处理的任务
     *
     * @param taskType 任务类型（必填）
     * @return String 操作结果
     * @throws ApiException 如果调用API失败，例如服务器错误或无法反序列化响应体
     */
    public String requeuePendingTask(String taskType) throws ApiException {
        Assertion.assertNotNull(taskType, "taskType");
        String path = f("/tasks/queue/requeue/%s", taskType);
        Call call = apiClient.buildPostCall(path, new ArrayList<>());
        DataResult<String> resp = apiClient.execute(call, String.class);
        return resp.getData();
    }

    /**
     * 更新任务
     *
     * @param taskResult 任务执行结果
     * @return String 操作结果
     * @throws ApiException 如果调用API失败
     */
    public String updateTask(TaskExecResult taskResult) throws ApiException {
        Assertion.assertNotNull(taskResult, "taskResult");
        String path = "/tasks/update";
        Call call = apiClient.buildPostCall(path, taskResult);
        DataResult<String> resp = apiClient.execute(call, String.class);
        return resp.getData();
    }

    /**
     * 构建通过引用名称更新任务的调用
     *
     * @param body 请求体
     * @param workflowId 工作流ID
     * @param taskRefName 任务引用名称
     * @param status 状态
     * @return Call HTTP调用对象
     * @throws ApiException 如果构建调用失败
     */
    private Call updateTaskByRefNameCall(Map<String, Object> body, String workflowId, String taskRefName, String status)
                                                                                                                        throws ApiException {
        Assertion.assertNotNull(body, "body");
        Assertion.assertNotNull(workflowId, "workflowId");
        Assertion.assertNotNull(taskRefName, "taskRefName");
        Assertion.assertNotNull(status, "status");
        String path = f("/tasks/%s/%s/%s", workflowId, taskRefName, status);
        List<Pair> queryParams = new ArrayList<>();
        String workerId = getIdentity();
        queryParams.addAll(ClientHelper.parameterToPair("workerId", workerId));
        return apiClient.buildPostCall(path, body, queryParams);
    }

    /**
     * 通过引用名称更新任务
     *
     * @param output 任务输出
     * @param workflowId 工作流ID
     * @param taskRefName 任务引用名称
     * @param status 状态
     * @return String 任务ID
     * @throws ApiException 如果调用API失败
     */
    public String updateTaskByRefName(Map<String, Object> output, String workflowId, String taskRefName, String status)
                                                                                                                       throws ApiException {
        Call call = updateTaskByRefNameCall(output, workflowId, taskRefName, status);
        DataResult<String> resp = apiClient.execute(call, String.class);
        return resp.getData();
    }

    /**
     * 获取当前节点标识
     *
     * @return String 节点标识
     */
    private String getIdentity() {
        String serverId;
        try {
            serverId = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            serverId = System.getenv("HOSTNAME");
        }
        if (serverId == null) {
            serverId = System.getProperty("user.name");
        }
        return serverId;
    }
}
