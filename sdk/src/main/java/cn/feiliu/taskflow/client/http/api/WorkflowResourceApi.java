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
import cn.feiliu.taskflow.client.http.*;
import cn.feiliu.taskflow.client.http.types.TypeFactory;
import cn.feiliu.taskflow.client.utils.Assertion;
import cn.feiliu.taskflow.client.utils.HttpHelper;
import cn.feiliu.taskflow.dto.ApiResponse;
import cn.feiliu.taskflow.dto.CorrelationIdsSearchRequest;
import cn.feiliu.taskflow.dto.WorkflowProgressUpdate;
import cn.feiliu.taskflow.dto.result.WorkflowRun;
import cn.feiliu.taskflow.dto.run.ExecutingWorkflow;
import cn.feiliu.taskflow.dto.workflow.SkipTaskRequest;
import cn.feiliu.taskflow.dto.workflow.StartWorkflowRequest;
import cn.feiliu.taskflow.dto.workflow.WorkflowRerunRequest;
import cn.feiliu.taskflow.exceptions.ApiException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.squareup.okhttp.Call;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.feiliu.common.api.utils.CommonUtils.f;

public class WorkflowResourceApi {
    private ApiClient apiClient;

    public WorkflowResourceApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 启动工作流的决策任务
     *
     * @param workflowId 工作流ID(必需)
     * @throws ApiException 如果调用API失败,例如服务器错误或无法反序列化响应体
     */
    public void decide(String workflowId) throws ApiException {
        Assertion.assertNotNull(workflowId, "workflowId");
        String path = f("/workflow/decide/%s", workflowId);
        Call call = apiClient.buildPostCall(path, new ArrayList<>());
        apiClient.execute(call);
    }

    /**
     * 从系统中删除工作流
     *
     * @param workflowId      工作流ID(必需)
     * @param archiveWorkflow 是否归档工作流(可选,默认为true)
     * @throws ApiException 如果调用API失败,例如服务器错误或无法反序列化响应体
     */
    public void delete(String workflowId, Boolean archiveWorkflow) throws ApiException {
        Assertion.assertNotNull(workflowId, "workflowId");
        String path = f("/workflow/%s/remove", workflowId);
        List<Pair> params = Lists.newArrayList(HttpHelper.parameterToPair("archiveWorkflow", archiveWorkflow));
        Call call = apiClient.buildDeleteCall(path, params);
        apiClient.execute(call);
    }

    /**
     * 构建获取执行状态的调用
     *
     * @param workflowId   工作流ID(必需)
     * @param includeTasks 是否包含任务(可选,默认为true)
     * @return 要执行的Call对象
     * @throws ApiException 如果序列化请求体对象失败
     */
    public Call getExecutionStatusCall(String workflowId, Boolean includeTasks) throws ApiException {
        String localVarPath = "/workflow/" + workflowId;
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        if (includeTasks != null)
            localVarQueryParams.addAll(HttpHelper.parameterToPair("includeTasks", includeTasks));
        return apiClient.buildCall(localVarPath, "GET", localVarQueryParams, null, null, null);
    }

    private Call getExecutionStatusValidateBeforeCall(String workflowId, Boolean includeTasks) throws ApiException {
        Assertion.assertNotNull(workflowId, "workflowId");
        Call call = getExecutionStatusCall(workflowId, includeTasks);
        return call;
    }

    /**
     * 通过工作流ID获取工作流
     *
     * @param workflowId   工作流ID(必需)
     * @param includeTasks 是否包含任务(可选,默认为true)
     * @return 工作流对象
     * @throws ApiException 如果调用API失败,例如服务器错误或无法反序列化响应体
     */
    public ExecutingWorkflow getExecutionStatus(String workflowId, Boolean includeTasks) throws ApiException {
        ApiResponse<ExecutingWorkflow> resp = getExecutionStatusWithHttpInfo(workflowId, includeTasks);
        return resp.getData();
    }

    /**
     * 通过工作流ID获取工作流
     *
     * @param workflowId   工作流ID(必需)
     * @param includeTasks 是否包含任务(可选,默认为true)
     * @return 带HTTP信息的工作流API响应
     * @throws ApiException 如果调用API失败,例如服务器错误或无法反序列化响应体
     */
    private ApiResponse<ExecutingWorkflow> getExecutionStatusWithHttpInfo(String workflowId, Boolean includeTasks)
                                                                                                                  throws ApiException {
        Call call = getExecutionStatusValidateBeforeCall(workflowId, includeTasks);
        Type localVarReturnType = new TypeReference<ExecutingWorkflow>() {
        }.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     * 检索所有正在运行的工作流
     *
     * @param name      工作流名称(必需)
     * @param version   版本号(可选,默认为1)
     * @param startTime 开始时间(可选)
     * @param endTime   结束时间(可选)
     * @return 工作流ID列表
     * @throws ApiException 如果调用API失败,例如服务器错误或无法反序列化响应体
     */
    public List<String> getRunningWorkflow(String name, Integer version, Long startTime, Long endTime)
                                                                                                      throws ApiException {
        if (name == null) {
            throw new ApiException("Missing the required parameter 'name' when calling getRunningWorkflow(Async)");
        }
        String path = f("/workflow/running/%s", name);
        List<Pair> queryParams = new ArrayList<>();
        if (version != null)
            queryParams.addAll(HttpHelper.parameterToPair("version", version));
        if (startTime != null)
            queryParams.addAll(HttpHelper.parameterToPair("startTime", startTime));
        if (endTime != null)
            queryParams.addAll(HttpHelper.parameterToPair("endTime", endTime));
        Call call = apiClient.buildGetCall(path, queryParams);
        ApiResponse<List<String>> resp = apiClient.doExecute(call, TypeFactory.ofList(String.class));
        return resp.getData();
    }

    /**
     * 构建获取工作流列表的调用
     *
     * @param body          请求体(必需)
     * @param name          工作流名称(必需)
     * @param includeClosed 是否包含已关闭的工作流(可选,默认为false)
     * @param includeTasks  是否包含任务(可选,默认为false)
     * @return 要执行的Call对象
     * @throws ApiException 如果序列化请求体对象失败
     */
    public Call getWorkflowsCall(List<String> body, String name, Boolean includeClosed, Boolean includeTasks)
                                                                                                             throws ApiException {
        Object localVarPostBody = body;
        String localVarPath = f("/workflow/%s/correlated", name);

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (includeClosed != null)
            localVarQueryParams.addAll(HttpHelper.parameterToPair("includeClosed", includeClosed));
        if (includeTasks != null)
            localVarQueryParams.addAll(HttpHelper.parameterToPair("includeTasks", includeTasks));

        return apiClient.buildCall(localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams,
            localVarPostBody, null);
    }

    private Call getWorkflowsValidateBeforeCall(List<String> body, String name, Boolean includeClosed,
                                                Boolean includeTasks) throws ApiException {
        Assertion.assertNotNull(body, "body");
        Assertion.assertNotNull(name, "name");
        Call call = getWorkflowsCall(body, name, includeClosed, includeTasks);
        return call;
    }

    /**
     * 获取给定关联ID列表的工作流列表
     *
     * @param body          关联ID列表(必需)
     * @param name          工作流名称(必需)
     * @param includeClosed 是否包含已关闭的工作流(可选,默认为false)
     * @param includeTasks  是否包含任务(可选,默认为false)
     * @return 关联ID到工作流列表的映射
     * @throws ApiException 如果调用API失败,例如服务器错误或无法反序列化响应体
     */
    public Map<String, List<ExecutingWorkflow>> getWorkflows(List<String> body, String name, Boolean includeClosed,
                                                             Boolean includeTasks) throws ApiException {
        Call call = getWorkflowsValidateBeforeCall(body, name, includeClosed, includeTasks);
        Type localVarReturnType = new TypeReference<Map<String, List<ExecutingWorkflow>>>() {
        }.getType();
        ApiResponse<Map<String, List<ExecutingWorkflow>>> resp = apiClient.execute(call, localVarReturnType);
        return resp.getData();
    }

    /**
     * 构建获取工作流列表的调用
     *
     * @param name          工作流名称(必需)
     * @param correlationId 关联ID(必需)
     * @param includeClosed 是否包含已关闭的工作流(可选,默认为false)
     * @param includeTasks  是否包含任务(可选,默认为false)
     * @return 要执行的Call对象
     * @throws ApiException 如果序列化请求体对象失败
     */
    public Call getWorkflows1Call(String name, String correlationId, Boolean includeClosed, Boolean includeTasks)
                                                                                                                 throws ApiException {
        Object localVarPostBody = null;
        String localVarPath = f("/workflow/%s/correlated/%s", name, correlationId);
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (includeClosed != null)
            localVarQueryParams.addAll(HttpHelper.parameterToPair("includeClosed", includeClosed));
        if (includeTasks != null)
            localVarQueryParams.addAll(HttpHelper.parameterToPair("includeTasks", includeTasks));
        return apiClient.buildCall(localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
            localVarPostBody, null);
    }

    /**
     * 获取给定工作流名称列表和关联ID列表的工作流列表
     *
     * @param request       请求对象(必需)
     * @param includeClosed 是否包含已关闭的工作流(可选,默认为false)
     * @param includeTasks  是否包含任务(可选,默认为false)
     * @return 关联ID到工作流列表的映射
     * @throws ApiException 如果调用API失败,例如服务器错误或无法反序列化响应体
     */
    public Map<String, List<ExecutingWorkflow>> getWorkflowsByNamesAndCorrelationIds(CorrelationIdsSearchRequest request,
                                                                                     Boolean includeClosed,
                                                                                     Boolean includeTasks)
                                                                                                          throws ApiException {
        Assertion.assertNotNull(request, "request");
        Call call = getWorkflowsByNamesAndCorrelationIdsCall(request, includeClosed, includeTasks);
        Type localVarReturnType = new TypeReference<Map<String, List<ExecutingWorkflow>>>() {
        }.getType();
        ApiResponse<Map<String, List<ExecutingWorkflow>>> response = apiClient.execute(call, localVarReturnType);
        return response.getData();
    }

    /**
     * 构建获取工作流列表的调用
     *
     * @param body          请求体(必需)
     * @param includeClosed 是否包含已关闭的工作流(可选,默认为false)
     * @param includeTasks  是否包含任务(可选,默认为false)
     * @return 要执行的Call对象
     * @throws ApiException 如果序列化请求体对象失败
     */
    private Call getWorkflowsByNamesAndCorrelationIdsCall(CorrelationIdsSearchRequest body, Boolean includeClosed,
                                                          Boolean includeTasks) throws ApiException {
        Object localVarPostBody = body;

        String localVarPath = "/workflow/correlated/batch";

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (includeClosed != null)
            localVarQueryParams.addAll(HttpHelper.parameterToPair("includeClosed", includeClosed));
        if (includeTasks != null)
            localVarQueryParams.addAll(HttpHelper.parameterToPair("includeTasks", includeTasks));

        Map<String, Object> localVarFormParams = new HashMap<>();
        return apiClient.buildCall(localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams,
            localVarPostBody, localVarFormParams);
    }

    /**
     * 获取给定关联ID的工作流列表
     *
     * @param name          工作流名称(必需)
     * @param correlationId 关联ID(必需)
     * @param includeClosed 是否包含已关闭的工作流(可选,默认为false)
     * @param includeTasks  是否包含任务(可选,默认为false)
     * @return 工作流列表
     * @throws ApiException 如果调用API失败,例如服务器错误或无法反序列化响应体
     */
    public List<ExecutingWorkflow> getWorkflowsByCorrelationId(String name, String correlationId,
                                                               Boolean includeClosed, Boolean includeTasks)
                                                                                                           throws ApiException {
        if (name == null) {
            throw new ApiException("Missing the required parameter 'name' when calling getWorkflows1(Async)");
        }
        if (correlationId == null) {
            throw new ApiException("Missing the required parameter 'correlationId' when calling getWorkflows1(Async)");
        }

        Call call = getWorkflows1Call(name, correlationId, includeClosed, includeTasks);
        Type localVarReturnType = new TypeReference<List<ExecutingWorkflow>>() {
        }.getType();
        ApiResponse<List<ExecutingWorkflow>> resp = apiClient.execute(call, localVarReturnType);
        return resp.getData();
    }

    /**
     * 构建暂停工作流的调用
     *
     * @param workflowId 工作流ID(必需)
     * @return 要执行的Call对象
     * @throws ApiException 如果序列化请求体对象失败
     */
    public Call pauseWorkflowCall(String workflowId) throws ApiException {
        String localVarPath = f("/workflow/%s/pause", workflowId);
        return apiClient.buildCall(localVarPath, "PUT", null, null, null, null);
    }

    /**
     * 暂停工作流
     *
     * @param workflowId 工作流ID(必需)
     * @throws ApiException 如果调用API失败,例如服务器错误或无法反序列化响应体
     */
    public void pauseWorkflow(String workflowId) throws ApiException {
        Assertion.assertNotNull(workflowId, "workflowId");
        Call call = pauseWorkflowCall(workflowId);
        ApiResponse<Void> response = apiClient.execute(call);
    }

    /**
     * 构建重新运行的调用
     *
     * @param rerunWorkflowRequest 重新运行工作流请求(必需)
     * @param workflowId           工作流ID(必需)
     * @return 要执行的Call对象
     * @throws ApiException 如果序列化请求体对象失败
     */
    public Call rerunCall(WorkflowRerunRequest rerunWorkflowRequest, String workflowId) throws ApiException {
        Object localVarPostBody = rerunWorkflowRequest;

        String localVarPath = f("/workflow/%s/rerun", workflowId);
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<>();
        return apiClient.buildCall(localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams,
            localVarPostBody, null);
    }

    /**
     * 从特定任务重新运行工作流
     *
     * @param rerunWorkflowRequest 重新运行工作流请求(必需)
     * @param workflowId           工作流ID(必需)
     * @return 字符串
     * @throws ApiException 如果调用API失败,例如服务器错误或无法反序列化响应体
     */
    public String rerun(WorkflowRerunRequest rerunWorkflowRequest, String workflowId) throws ApiException {
        Assertion.assertNotNull(rerunWorkflowRequest, "rerunWorkflowRequest");
        Assertion.assertNotNull(workflowId, "workflowId");
        Call call = rerunCall(rerunWorkflowRequest, workflowId);
        Type localVarReturnType = new TypeReference<String>() {
        }.getType();
        return (String) apiClient.execute(call, localVarReturnType).getData();
    }

    /**
     * 构建重置工作流的调用
     *
     * @param workflowId 工作流ID(必需)
     * @return 要执行的Call对象
     * @throws ApiException 如果序列化请求体对象失败
     */
    public Call resetWorkflowCall(String workflowId) throws ApiException {
        String localVarPath = f("/workflow/%s/resetcallbacks", workflowId);
        return apiClient.buildCall(localVarPath, "POST", null, null, null, null);
    }

    /**
     * 重置所有非终端简单任务的回调次数
     *
     * @param workflowId 工作流ID(必需)
     * @throws ApiException 如果调用API失败,例如服务器错误或无法反序列化响应体
     */
    public void resetWorkflow(String workflowId) throws ApiException {
        Assertion.assertNotNull(workflowId, "workflowId");
        Call call = resetWorkflowCall(workflowId);
        ApiResponse<Void> response = apiClient.execute(call);
    }

    /**
     * 构建重新启动的调用
     *
     * @param workflowId           工作流ID(必需)
     * @param useLatestDefinitions 是否使用最新定义(可选,默认为false)
     * @return 要执行的Call对象
     * @throws ApiException 如果序列化请求体对象失败
     */
    public Call restartCall(String workflowId, Boolean useLatestDefinitions) throws ApiException {
        Object localVarPostBody = null;
        String localVarPath = f("/workflow/%s/restart", workflowId);

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (useLatestDefinitions != null)
            localVarQueryParams.addAll(HttpHelper.parameterToPair("useLatestDefinitions", useLatestDefinitions));

        return apiClient.buildCall(localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams,
            localVarPostBody, null);
    }

    /**
     * 重新启动一个已完成的工作流
     *
     * @param workflowId           工作流ID(必需)
     * @param useLatestDefinitions 是否使用最新定义(可选,默认为false)
     * @throws ApiException 如果调用API失败,例如服务器错误或无法反序列化响应体
     */
    public void restart(String workflowId, Boolean useLatestDefinitions) throws ApiException {
        Assertion.assertNotNull(workflowId, "workflowId");
        Call call = restartCall(workflowId, useLatestDefinitions);
        ApiResponse<Void> response = apiClient.execute(call);

    }

    /**
     * 构建恢复工作流的调用
     *
     * @param workflowId 工作流ID(必需)
     * @return 要执行的Call对象
     * @throws ApiException 如果序列化请求体对象失败
     */
    public Call resumeWorkflowCall(String workflowId) throws ApiException {
        Object localVarPostBody = null;
        String localVarPath = f("/workflow/%s/resume", workflowId);
        return apiClient.buildCall(localVarPath, "PUT", null, null, localVarPostBody, null);
    }

    private Call resumeWorkflowValidateBeforeCall(String workflowId) throws ApiException {
        Assertion.assertNotNull(workflowId, "workflowId");
        Call call = resumeWorkflowCall(workflowId);
        return call;
    }

    /**
     * 恢复工作流
     *
     * @param workflowId 工作流ID(必需)
     * @throws ApiException 如果调用API失败,例如服务器错误或无法反序列化响应体
     */
    public void resumeWorkflow(String workflowId) throws ApiException {
        Call call = resumeWorkflowValidateBeforeCall(workflowId);
        ApiResponse<Void> resp = apiClient.execute(call);
    }

    /**
     * 构建重试的调用
     *
     * @param workflowId             工作流ID(必需)
     * @param resumeSubworkflowTasks 是否恢复子工作流任务(可选,默认为false)
     * @return 要执行的Call对象
     * @throws ApiException 如果序列化请求体对象失败
     */
    public Call retryCall(String workflowId, Boolean resumeSubworkflowTasks) throws ApiException {
        Object localVarPostBody = null;
        String localVarPath = f("/workflow/%s/retry", workflowId);

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (resumeSubworkflowTasks != null)
            localVarQueryParams.addAll(HttpHelper.parameterToPair("resumeSubworkflowTasks", resumeSubworkflowTasks));
        return apiClient.buildCall(localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams,
            localVarPostBody, null);
    }

    private Call retryValidateBeforeCall(String workflowId, Boolean resumeSubworkflowTasks) throws ApiException {
        Assertion.assertNotNull(workflowId, "workflowId");
        Call call = retryCall(workflowId, resumeSubworkflowTasks);
        return call;
    }

    /**
     * 重试最后一个失败的任务
     *
     * @param workflowId             工作流ID(必需)
     * @param resumeSubworkflowTasks 是否恢复子工作流任务(可选,默认为false)
     * @throws ApiException 如果调用API失败,例如服务器错误或无法反序列化响应体
     */
    public void retry(String workflowId, Boolean resumeSubworkflowTasks) throws ApiException {
        Call call = retryValidateBeforeCall(workflowId, resumeSubworkflowTasks);
        ApiResponse<Void> response = apiClient.execute(call);
    }

    /**
     * 构建跳过任务的调用
     *
     * @param workflowId        工作流ID(必需)
     * @param taskReferenceName 任务引用名称(必需)
     * @param skipTaskRequest   跳过任务请求(必需)
     * @return 要执行的Call对象
     * @throws ApiException 如果序列化请求体对象失败
     */
    public Call skipTaskFromWorkflowCall(String workflowId, String taskReferenceName, SkipTaskRequest skipTaskRequest)
                                                                                                                      throws ApiException {
        Object localVarPostBody = null;

        String localVarPath = f("/workflow/%s/skiptask/%s", workflowId, taskReferenceName);

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (skipTaskRequest != null)
            localVarQueryParams.addAll(HttpHelper.parameterToPair("skipTaskRequest", skipTaskRequest));

        return apiClient.buildCall(localVarPath, "PUT", localVarQueryParams, localVarCollectionQueryParams,
            localVarPostBody, null);
    }

    private Call skipTaskFromWorkflowValidateBeforeCall(String workflowId, String taskReferenceName,
                                                        SkipTaskRequest skipTaskRequest) throws ApiException {
        Assertion.assertNotNull(workflowId, "workflowId");
        Assertion.assertNotNull(taskReferenceName, "taskReferenceName");
        Assertion.assertNotNull(skipTaskRequest, "skipTaskRequest");
        Call call = skipTaskFromWorkflowCall(workflowId, taskReferenceName, skipTaskRequest);
        return call;
    }

    /**
     * 跳过一个当前正在运行的工作流中的给定任务
     *
     * @param workflowId        工作流ID(必需)
     * @param taskReferenceName 任务引用名称(必需)
     * @param skipTaskRequest   跳过任务请求(必需)
     * @throws ApiException 如果调用API失败,例如服务器错误或无法反序列化响应体
     */
    public void skipTaskFromWorkflow(String workflowId, String taskReferenceName, SkipTaskRequest skipTaskRequest)
                                                                                                                  throws ApiException {
        Call call = skipTaskFromWorkflowValidateBeforeCall(workflowId, taskReferenceName, skipTaskRequest);
        ApiResponse<Void> response = apiClient.execute(call);
    }

    private Call startWorkflowValidateBeforeCall(StartWorkflowRequest startWorkflowRequest) throws ApiException {
        Assertion.assertNotNull(startWorkflowRequest, "startWorkflowRequest");
        String path = "/workflow/start";
        return apiClient.buildPostCall(path, startWorkflowRequest);
    }

    /**
     * 使用StartWorkflowRequest启动一个新工作流，允许在域中执行任务
     *
     * @param startWorkflowRequest 启动工作流请求(必需)
     * @return 字符串
     * @throws ApiException 如果调用API失败,例如服务器错误或无法反序列化响应体
     */
    public String startWorkflow(StartWorkflowRequest startWorkflowRequest) throws ApiException {
        ApiResponse<String> resp = startWorkflowWithHttpInfo(startWorkflowRequest);
        return resp.getData();
    }

    /**
     * Start a new workflow with StartWorkflowRequest, which allows task to be executed in a domain
     *
     * @param startWorkflowRequest (required)
     * @return ApiResponse&lt;String&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    private ApiResponse<String> startWorkflowWithHttpInfo(StartWorkflowRequest startWorkflowRequest)
                                                                                                    throws ApiException {
        Call call = startWorkflowValidateBeforeCall(startWorkflowRequest);
        Type localVarReturnType = new TypeReference<String>() {
        }.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     * 终止工作流执行
     */
    public void terminateWithAReason(String workflowId, String reason) throws ApiException {
        Assertion.assertNotNull(workflowId, "workflowId");
        String path = f("/workflow/%s/terminate", workflowId);
        List<Pair> queryParams = Lists.newArrayList(HttpHelper.parameterToPair("reason", reason));
        Call call = apiClient.buildDeleteCall(path, queryParams);
        apiClient.execute(call);
    }

    public ExecutingWorkflow updateVariables(String workflowId, Map<String, Object> variables) {
        Call call = updateVariablesCall(workflowId, variables);
        Type returnType = new TypeReference<ExecutingWorkflow>() {
        }.getType();
        ApiResponse<ExecutingWorkflow> response = apiClient.execute(call, returnType);
        return response.getData();
    }

    private Call updateVariablesCall(String workflowId, Map<String, Object> variables) {
        Object localVarPostBody = variables;
        String localVarPath = f("/workflow/%s/variables", workflowId);
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        return apiClient.buildCall(localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams,
            localVarPostBody, null);
    }

    /**
     * 更新工作流和任务状态
     * 更新工作流变量、任务和触发器评估。
     *
     * @return WorkflowRun
     * @throws ApiException 如果调用API失败,例如服务器错误或无法反序列化响应体
     */
    public WorkflowRun updateWorkflowState(WorkflowProgressUpdate body) throws ApiException {
        ApiResponse<WorkflowRun> resp = updateWorkflowAndTaskStateWithHttpInfo(body);
        return resp.getData();
    }

    /**
     * 更新工作流和任务状态更新工作流变量、任务和触发器评估。
     */
    public ApiResponse<WorkflowRun> updateWorkflowAndTaskStateWithHttpInfo(WorkflowProgressUpdate body)
                                                                                                       throws ApiException {
        Assertion.assertNotNull(body, "body");
        String path = "/workflow/update";
        List<Pair> queryParams = new ArrayList<>();
        Call call = apiClient.buildPostCall(path, body, queryParams);
        return apiClient.execute(call, WorkflowRun.class);
    }
}
