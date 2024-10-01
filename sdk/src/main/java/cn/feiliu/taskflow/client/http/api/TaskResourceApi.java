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
package cn.feiliu.taskflow.client.http.api;

import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.open.ApiResponse;
import cn.feiliu.taskflow.client.http.*;
import cn.feiliu.taskflow.client.http.types.TypeFactory;
import cn.feiliu.taskflow.client.utils.Assertion;
import cn.feiliu.taskflow.client.utils.HttpHelper;
import cn.feiliu.taskflow.client.utils.SdkHelper;
import cn.feiliu.taskflow.open.exceptions.ApiException;
import com.google.common.collect.Lists;
import cn.feiliu.taskflow.common.metadata.tasks.PollData;
import cn.feiliu.taskflow.common.metadata.tasks.ExecutingTask;
import cn.feiliu.taskflow.common.metadata.tasks.TaskLog;
import cn.feiliu.taskflow.common.metadata.tasks.TaskExecResult;
import com.squareup.okhttp.Call;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static cn.feiliu.taskflow.common.utils.TaskflowUtils.f;

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
     * Batch poll for a task of a certain type
     *
     * @param taskType (required)
     * @param workerId (optional)
     * @param domain   (optional)
     * @param count    (optional, default to 1)
     * @param timeout  (optional, default to 100)
     * @return List&lt;Task&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    public List<ExecutingTask> batchPoll(String taskType, String workerId, String domain, Integer count, Integer timeout)
                                                                                                                         throws ApiException {
        Assertion.assertNotNull(taskType, "taskType");
        String path = f("/tasks/poll/batch/%s", taskType);
        List<Pair> queryParams = new ArrayList<>();
        if (workerId != null)
            queryParams.addAll(HttpHelper.parameterToPair("workerId", workerId));
        if (domain != null)
            queryParams.addAll(HttpHelper.parameterToPair("domain", domain));
        if (count != null)
            queryParams.addAll(HttpHelper.parameterToPair("count", count));
        if (timeout != null)
            queryParams.addAll(HttpHelper.parameterToPair("timeout", timeout));
        Call call = apiClient.buildGetCall(path, queryParams);
        ApiResponse<List<ExecutingTask>> resp = apiClient.doExecute(call, TypeFactory.ofList(ExecutingTask.class));
        return resp.getData();
    }

    /**
     * 获取所有任务类型的最新轮询数据
     *
     * @return List&lt;PollData&gt;
     * @throws ApiException 如果调用API失败，例如服务器错误或无法反序列化响应体
     */
    public List<PollData> getAllPollData() throws ApiException {
        String path = "/tasks/queue/polldata/all";
        Call call = apiClient.buildGetCall(path, new ArrayList<>());
        ApiResponse<List<PollData>> resp = apiClient.doExecute(call, TypeFactory.ofList(PollData.class));
        return resp.getData();
    }

    /**
     * 获取给定任务类型的最后一次轮询数据
     *
     * @param taskType (required)
     * @return List&lt;PollData&gt;
     */
    public List<PollData> getPollData(String taskType) throws ApiException {
        if (taskType == null) {
            throw new ApiException("Missing the required parameter 'taskType'");
        }
        String path = "/tasks/queue/polldata";
        List<Pair> queryParams = Lists.newArrayList(HttpHelper.parameterToPair("taskType", taskType));
        Call call = apiClient.buildGetCall(path, queryParams);
        ApiResponse<List<PollData>> resp = apiClient.doExecute(call, TypeFactory.ofList(PollData.class));
        return resp.getData();
    }

    /**
     * 按Id获取任务
     *
     * @param taskId 任务ID(必填)
     */
    public ExecutingTask getTask(String taskId) throws ApiException {
        if (taskId == null) {
            throw new ApiException("Missing the required parameter 'taskId'");
        }
        String localVarPath = f("/tasks/%s", taskId);
        Call call = apiClient.buildGetCall(localVarPath, new ArrayList<>());
        ApiResponse<ExecutingTask> resp = apiClient.execute(call, ExecutingTask.class);
        return resp.getData();
    }

    /**
     * 获取任务执行日志
     *
     * @param taskId 任务ID(必填)
     */
    public List<TaskLog> getTaskLogs(String taskId) throws ApiException {
        Assertion.assertNotNull(taskId, "taskId");
        String path = f("/tasks/%s/log", taskId);
        Call call = apiClient.buildGetCall(path, new ArrayList<>());
        ApiResponse<List<TaskLog>> resp = apiClient.doExecute(call, TypeFactory.ofList(TaskLog.class));
        return resp.getData();
    }

    /**
     * 记录日志任务执行明细
     *
     * @param body   日志Body
     * @param taskId 任务ID
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
     * @param workerId 选填
     * @param domain   选填
     */
    public ExecutingTask poll(String taskType, String workerId, String domain) throws ApiException {
        Assertion.assertNotNull(taskType, "taskType");
        String path = f("/tasks/poll/%s", taskType);
        List<Pair> queryParams = new ArrayList<>();
        if (workerId != null)
            queryParams.addAll(HttpHelper.parameterToPair("workerId", workerId));
        if (domain != null)
            queryParams.addAll(HttpHelper.parameterToPair("domain", domain));
        Call call = apiClient.buildGetCall(path, queryParams);
        ApiResponse<ExecutingTask> resp = apiClient.execute(call, ExecutingTask.class);
        return resp.getData();
    }

    /**
     * Requeue pending tasks
     *
     * @param taskType (required)
     * @return String
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    public String requeuePendingTask(String taskType) throws ApiException {
        Assertion.assertNotNull(taskType, "taskType");
        String path = f("/tasks/queue/requeue/%s", taskType);
        Call call = apiClient.buildPostCall(path, new ArrayList<>());
        ApiResponse<String> resp = apiClient.execute(call, String.class);
        return resp.getData();
    }

    /**
     * 更新任务
     */
    public String updateTask(TaskExecResult taskResult) throws ApiException {
        Assertion.assertNotNull(taskResult, "taskResult");
        String path = "/tasks/update";
        Call call = apiClient.buildPostCall(path, taskResult);
        ApiResponse<String> resp = apiClient.execute(call, String.class);
        return resp.getData();
    }

    private Call updateTaskByRefNameCall(Map<String, Object> body, String workflowId, String taskRefName, String status)
                                                                                                                        throws ApiException {
        Assertion.assertNotNull(body, "body");
        Assertion.assertNotNull(workflowId, "workflowId");
        Assertion.assertNotNull(taskRefName, "taskRefName");
        Assertion.assertNotNull(status, "status");
        String path = f("/tasks/%s/%s/%s", workflowId, taskRefName, status);
        List<Pair> queryParams = new ArrayList<>();
        String workerId = getIdentity();
        queryParams.addAll(HttpHelper.parameterToPair("workerId", workerId));
        return apiClient.buildPostCall(path, body, queryParams);
    }

    /**
     * 通过Ref Name更新任务(异步)
     *
     * @param output      Task Output
     * @param workflowId  Workflow Id
     * @param taskRefName Reference name of the task to be updated
     * @param status      Status
     * @return Task Id
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response
     */
    public String updateTaskByRefName(Map<String, Object> output, String workflowId, String taskRefName, String status)
                                                                                                                       throws ApiException {
        Call call = updateTaskByRefNameCall(output, workflowId, taskRefName, status);
        ApiResponse<String> resp = apiClient.execute(call, String.class);
        return resp.getData();
    }

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
