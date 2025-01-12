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
import com.squareup.okhttp.Interceptor;

import java.io.IOException;
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

    public Call executeWorkflowCall(StartWorkflowRequest body, ProgressResponseBody.ProgressListener progressListener,
                                    ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                        throws ApiException {
        Object localVarPostBody = body;
        String localVarPath = "/workflow/start";
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = { "application/json" };
        final String localVarAccept = HttpHelper.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null)
            localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = { "application/json" };
        final String localVarContentType = HttpHelper.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if (progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener)).build();
                }
            });
        }

        return apiClient.buildCall(localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams,
            localVarPostBody, localVarHeaderParams, localVarFormParams, progressRequestListener);
    }

    /**
     * Starts the decision task for a workflow
     *
     * @param workflowId (required)
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    public void decide(String workflowId) throws ApiException {
        Assertion.assertNotNull(workflowId, "workflowId");
        String path = f("/workflow/decide/%s", workflowId);
        Call call = apiClient.buildPostCall(path, new ArrayList<>());
        apiClient.execute(call);
    }

    /**
     * Removes the workflow from the system
     *
     * @param workflowId      (required)
     * @param archiveWorkflow (optional, default to true)
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    public void delete(String workflowId, Boolean archiveWorkflow) throws ApiException {
        Assertion.assertNotNull(workflowId, "workflowId");
        String path = f("/workflow/%s/remove", workflowId);
        List<Pair> params = Lists.newArrayList(HttpHelper.parameterToPair("archiveWorkflow", archiveWorkflow));
        Call call = apiClient.buildDeleteCall(path, params);
        apiClient.execute(call);
    }

    /**
     * Build call for getExecutionStatus
     *
     * @param workflowId              (required)
     * @param includeTasks            (optional, default to true)
     * @param progressListener        Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public Call getExecutionStatusCall(String workflowId, Boolean includeTasks,
                                       final ProgressResponseBody.ProgressListener progressListener,
                                       final ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                                 throws ApiException {
        Object localVarPostBody = null;
        String localVarPath = "/workflow/" + workflowId;
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (includeTasks != null)
            localVarQueryParams.addAll(HttpHelper.parameterToPair("includeTasks", includeTasks));

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = { "*/*" };
        final String localVarAccept = HttpHelper.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null)
            localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {};

        final String localVarContentType = HttpHelper.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if (progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener)).build();
                }
            });
        }

        return apiClient.buildCall(localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
            localVarPostBody, localVarHeaderParams, localVarFormParams, progressRequestListener);
    }

    private Call getExecutionStatusValidateBeforeCall(String workflowId,
                                                      Boolean includeTasks,
                                                      final ProgressResponseBody.ProgressListener progressListener,
                                                      final ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                                                throws ApiException {
        Assertion.assertNotNull(workflowId, "workflowId");
        Call call = getExecutionStatusCall(workflowId, includeTasks, progressListener, progressRequestListener);
        return call;
    }

    /**
     * Gets the workflow by workflow id
     *
     * @param workflowId   (required)
     * @param includeTasks (optional, default to true)
     * @return Workflow
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    public ExecutingWorkflow getExecutionStatus(String workflowId, Boolean includeTasks) throws ApiException {
        ApiResponse<ExecutingWorkflow> resp = getExecutionStatusWithHttpInfo(workflowId, includeTasks);
        return resp.getData();
    }

    /**
     * Gets the workflow by workflow id
     *
     * @param workflowId   (required)
     * @param includeTasks (optional, default to true)
     * @return ApiResponse&lt;Workflow&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    private ApiResponse<ExecutingWorkflow> getExecutionStatusWithHttpInfo(String workflowId, Boolean includeTasks)
                                                                                                                  throws ApiException {
        Call call = getExecutionStatusValidateBeforeCall(workflowId, includeTasks, null, null);
        Type localVarReturnType = new TypeReference<ExecutingWorkflow>() {
        }.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     * 检索所有正在运行的工作流
     *
     * @param name      (required)
     * @param version   (optional, default to 1)
     * @param startTime (optional)
     * @param endTime   (optional)
     * @return List&lt;String&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    public List<String> getRunningWorkflow(String name, Integer version, Long startTime, Long endTime)
                                                                                                      throws ApiException {
        if (name == null) {
            throw new ApiException("Missing the required parameter 'name' when calling getRunningWorkflow(Async)");
        }
        String path = f("/workflow/running/%s", name);
        List<Pair> queryParams = new ArrayList<Pair>();
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
     * Build call for getWorkflows
     *
     * @param body                    (required)
     * @param name                    (required)
     * @param includeClosed           (optional, default to false)
     * @param includeTasks            (optional, default to false)
     * @param progressListener        Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public Call getWorkflowsCall(List<String> body, String name, Boolean includeClosed, Boolean includeTasks,
                                 final ProgressResponseBody.ProgressListener progressListener,
                                 final ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                           throws ApiException {
        Object localVarPostBody = body;
        String localVarPath = f("/workflow/%s/correlated", name);

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (includeClosed != null)
            localVarQueryParams.addAll(HttpHelper.parameterToPair("includeClosed", includeClosed));
        if (includeTasks != null)
            localVarQueryParams.addAll(HttpHelper.parameterToPair("includeTasks", includeTasks));

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = { "*/*" };
        final String localVarAccept = HttpHelper.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null)
            localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = { "application/json" };
        final String localVarContentType = HttpHelper.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if (progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener)).build();
                }
            });
        }

        return apiClient.buildCall(localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams,
            localVarPostBody, localVarHeaderParams, localVarFormParams, progressRequestListener);
    }

    private Call getWorkflowsValidateBeforeCall(List<String> body,
                                                String name,
                                                Boolean includeClosed,
                                                Boolean includeTasks,
                                                final ProgressResponseBody.ProgressListener progressListener,
                                                final ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                                          throws ApiException {
        Assertion.assertNotNull(body, "body");
        Assertion.assertNotNull(name, "name");
        Call call = getWorkflowsCall(body, name, includeClosed, includeTasks, progressListener, progressRequestListener);
        return call;
    }

    /**
     * Lists workflows for the given correlation id list
     *
     * @param body          (required)
     * @param name          (required)
     * @param includeClosed (optional, default to false)
     * @param includeTasks  (optional, default to false)
     * @return Map&lt;String, List&lt;Workflow&gt;&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    public Map<String, List<ExecutingWorkflow>> getWorkflows(List<String> body, String name, Boolean includeClosed,
                                                             Boolean includeTasks) throws ApiException {
        ApiResponse<Map<String, List<ExecutingWorkflow>>> resp = getWorkflowsWithHttpInfo(body, name, includeClosed,
            includeTasks);
        return resp.getData();
    }

    /**
     * Lists workflows for the given correlation id list
     *
     * @param body          (required)
     * @param name          (required)
     * @param includeClosed (optional, default to false)
     * @param includeTasks  (optional, default to false)
     * @return ApiResponse&lt;Map&lt;String, List&lt;Workflow&gt;&gt;&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    private ApiResponse<Map<String, List<ExecutingWorkflow>>> getWorkflowsWithHttpInfo(List<String> body, String name,
                                                                                       Boolean includeClosed,
                                                                                       Boolean includeTasks)
                                                                                                            throws ApiException {
        Call call = getWorkflowsValidateBeforeCall(body, name, includeClosed, includeTasks, null, null);
        Type localVarReturnType = new TypeReference<Map<String, List<ExecutingWorkflow>>>() {
        }.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     * Build call for getWorkflows1
     *
     * @param name                    (required)
     * @param correlationId           (required)
     * @param includeClosed           (optional, default to false)
     * @param includeTasks            (optional, default to false)
     * @param progressListener        Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public Call getWorkflows1Call(String name, String correlationId, Boolean includeClosed, Boolean includeTasks,
                                  final ProgressResponseBody.ProgressListener progressListener,
                                  final ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                            throws ApiException {
        Object localVarPostBody = null;
        String localVarPath = f("/workflow/%s/correlated/%s", name, correlationId);
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (includeClosed != null)
            localVarQueryParams.addAll(HttpHelper.parameterToPair("includeClosed", includeClosed));
        if (includeTasks != null)
            localVarQueryParams.addAll(HttpHelper.parameterToPair("includeTasks", includeTasks));

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = { "*/*" };
        final String localVarAccept = HttpHelper.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null)
            localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {};

        final String localVarContentType = HttpHelper.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if (progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener)).build();
                }
            });
        }

        return apiClient.buildCall(localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
            localVarPostBody, localVarHeaderParams, localVarFormParams, progressRequestListener);
    }

    /**
     * Lists workflows for the given correlation id list and workflow name list
     *
     * @param request       (required)
     * @param includeClosed (optional, default to false)
     * @param includeTasks  (optional, default to false)
     * @return Map of correlation id to workflow list
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public Map<String, List<ExecutingWorkflow>> getWorkflowsByNamesAndCorrelationIds(CorrelationIdsSearchRequest request,
                                                                                     Boolean includeClosed,
                                                                                     Boolean includeTasks)
                                                                                                          throws ApiException {
        Call call = getWorkflowsByNamesAndCorrelationIdsBeforeCall(request, includeClosed, includeTasks, null, null);
        Type localVarReturnType = new TypeReference<Map<String, List<ExecutingWorkflow>>>() {
        }.getType();
        ApiResponse<Map<String, List<ExecutingWorkflow>>> response = apiClient.execute(call, localVarReturnType);
        return response.getData();
    }

    private Call getWorkflowsByNamesAndCorrelationIdsBeforeCall(CorrelationIdsSearchRequest request,
                                                                Boolean includeClosed,
                                                                Boolean includeTasks,
                                                                final ProgressResponseBody.ProgressListener progressListener,
                                                                final ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                                                          throws ApiException {
        Assertion.assertNotNull(request, "request");
        Call call = getWorkflowsByNamesAndCorrelationIdsCall(request, includeClosed, includeTasks, progressListener,
            progressRequestListener);
        return call;
    }

    /**
     * Build call for getWorkflows1
     *
     * @param body                    (required)
     * @param includeClosed           (optional, default to false)
     * @param includeTasks            (optional, default to false)
     * @param progressListener        Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    private Call getWorkflowsByNamesAndCorrelationIdsCall(CorrelationIdsSearchRequest body,
                                                          Boolean includeClosed,
                                                          Boolean includeTasks,
                                                          final ProgressResponseBody.ProgressListener progressListener,
                                                          final ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                                                    throws ApiException {
        Object localVarPostBody = body;

        String localVarPath = "/workflow/correlated/batch";

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (includeClosed != null)
            localVarQueryParams.addAll(HttpHelper.parameterToPair("includeClosed", includeClosed));
        if (includeTasks != null)
            localVarQueryParams.addAll(HttpHelper.parameterToPair("includeTasks", includeTasks));

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = { "*/*" };
        final String localVarAccept = HttpHelper.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null)
            localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = { "application/json" };
        final String localVarContentType = HttpHelper.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if (progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener)).build();
                }
            });
        }

        return apiClient.buildCall(localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams,
            localVarPostBody, localVarHeaderParams, localVarFormParams, progressRequestListener);
    }

    private Call getWorkflows1ValidateBeforeCall(String name,
                                                 String correlationId,
                                                 Boolean includeClosed,
                                                 Boolean includeTasks,
                                                 final ProgressResponseBody.ProgressListener progressListener,
                                                 final ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                                           throws ApiException {
        if (name == null) {
            throw new ApiException("Missing the required parameter 'name' when calling getWorkflows1(Async)");
        }
        if (correlationId == null) {
            throw new ApiException("Missing the required parameter 'correlationId' when calling getWorkflows1(Async)");
        }

        Call call = getWorkflows1Call(name, correlationId, includeClosed, includeTasks, progressListener,
            progressRequestListener);
        return call;
    }

    /**
     * Lists workflows for the given correlation id
     *
     * @param name          (required)
     * @param correlationId (required)
     * @param includeClosed (optional, default to false)
     * @param includeTasks  (optional, default to false)
     * @return List&lt;Workflow&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    public List<ExecutingWorkflow> getWorkflowsByCorrelationId(String name, String correlationId,
                                                               Boolean includeClosed, Boolean includeTasks)
                                                                                                           throws ApiException {
        ApiResponse<List<ExecutingWorkflow>> resp = getWorkflows1WithHttpInfo(name, correlationId, includeClosed,
            includeTasks);
        return resp.getData();
    }

    /**
     * Lists workflows for the given correlation id
     *
     * @param name          (required)
     * @param correlationId (required)
     * @param includeClosed (optional, default to false)
     * @param includeTasks  (optional, default to false)
     * @return ApiResponse&lt;List&lt;Workflow&gt;&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    private ApiResponse<List<ExecutingWorkflow>> getWorkflows1WithHttpInfo(String name, String correlationId,
                                                                           Boolean includeClosed, Boolean includeTasks)
                                                                                                                       throws ApiException {
        Call call = getWorkflows1ValidateBeforeCall(name, correlationId, includeClosed, includeTasks, null, null);
        Type localVarReturnType = new TypeReference<List<ExecutingWorkflow>>() {
        }.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     * Build call for pauseWorkflow
     *
     * @param workflowId              (required)
     * @param progressListener        Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public Call pauseWorkflowCall(String workflowId, final ProgressResponseBody.ProgressListener progressListener,
                                  final ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                            throws ApiException {
        Object localVarPostBody = null;

        String localVarPath = f("/workflow/%s/pause", workflowId);

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {};

        final String localVarAccept = HttpHelper.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null)
            localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {};

        final String localVarContentType = HttpHelper.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if (progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener)).build();
                }
            });
        }

        return apiClient.buildCall(localVarPath, "PUT", localVarQueryParams, localVarCollectionQueryParams,
            localVarPostBody, localVarHeaderParams, localVarFormParams, progressRequestListener);
    }

    private Call pauseWorkflowValidateBeforeCall(String workflowId,
                                                 final ProgressResponseBody.ProgressListener progressListener,
                                                 final ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                                           throws ApiException {
        Assertion.assertNotNull(workflowId, "workflowId");
        Call call = pauseWorkflowCall(workflowId, progressListener, progressRequestListener);
        return call;
    }

    /**
     * Pauses the workflow
     *
     * @param workflowId (required)
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    public void pauseWorkflow(String workflowId) throws ApiException {
        pauseWorkflowWithHttpInfo(workflowId);
    }

    /**
     * Pauses the workflow
     *
     * @param workflowId (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    private ApiResponse<Void> pauseWorkflowWithHttpInfo(String workflowId) throws ApiException {
        Call call = pauseWorkflowValidateBeforeCall(workflowId, null, null);
        return apiClient.execute(call);
    }

    /**
     * Build call for rerun
     *
     * @param rerunWorkflowRequest    (required)
     * @param workflowId              (required)
     * @param progressListener        Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public Call rerunCall(WorkflowRerunRequest rerunWorkflowRequest, String workflowId,
                          final ProgressResponseBody.ProgressListener progressListener,
                          final ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                    throws ApiException {
        Object localVarPostBody = rerunWorkflowRequest;

        String localVarPath = f("/workflow/%s/rerun", workflowId);
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = { "text/plain" };
        final String localVarAccept = HttpHelper.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null)
            localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = { "application/json" };
        final String localVarContentType = HttpHelper.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if (progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener)).build();
                }
            });
        }

        return apiClient.buildCall(localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams,
            localVarPostBody, localVarHeaderParams, localVarFormParams, progressRequestListener);
    }

    private Call rerunValidateBeforeCall(WorkflowRerunRequest rerunWorkflowRequest, String workflowId,
                                         final ProgressResponseBody.ProgressListener progressListener,
                                         final ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                                   throws ApiException {
        Assertion.assertNotNull(rerunWorkflowRequest, "rerunWorkflowRequest");
        Assertion.assertNotNull(workflowId, "workflowId");
        Call call = rerunCall(rerunWorkflowRequest, workflowId, progressListener, progressRequestListener);
        return call;
    }

    /**
     * Reruns the workflow from a specific task
     *
     * @param rerunWorkflowRequest (required)
     * @param workflowId           (required)
     * @return String
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    public String rerun(WorkflowRerunRequest rerunWorkflowRequest, String workflowId) throws ApiException {
        ApiResponse<String> resp = rerunWithHttpInfo(rerunWorkflowRequest, workflowId);
        return resp.getData();
    }

    /**
     * Reruns the workflow from a specific task
     *
     * @param rerunWorkflowRequest (required)
     * @param workflowId           (required)
     * @return ApiResponse&lt;String&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    private ApiResponse<String> rerunWithHttpInfo(WorkflowRerunRequest rerunWorkflowRequest, String workflowId)
                                                                                                               throws ApiException {
        Call call = rerunValidateBeforeCall(rerunWorkflowRequest, workflowId, null, null);
        Type localVarReturnType = new TypeReference<String>() {
        }.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     * Build call for resetWorkflow
     *
     * @param workflowId              (required)
     * @param progressListener        Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public Call resetWorkflowCall(String workflowId, final ProgressResponseBody.ProgressListener progressListener,
                                  final ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                            throws ApiException {
        Object localVarPostBody = null;
        String localVarPath = f("/workflow/%s/resetcallbacks", workflowId);

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {};

        final String localVarAccept = HttpHelper.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null)
            localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {};

        final String localVarContentType = HttpHelper.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if (progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener)).build();
                }
            });
        }

        return apiClient.buildCall(localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams,
            localVarPostBody, localVarHeaderParams, localVarFormParams, progressRequestListener);
    }

    private Call resetWorkflowValidateBeforeCall(String workflowId,
                                                 final ProgressResponseBody.ProgressListener progressListener,
                                                 final ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                                           throws ApiException {
        Assertion.assertNotNull(workflowId, "workflowId");
        Call call = resetWorkflowCall(workflowId, progressListener, progressRequestListener);
        return call;
    }

    /**
     * Resets callback times of all non-terminal SIMPLE tasks to 0
     *
     * @param workflowId (required)
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    public void resetWorkflow(String workflowId) throws ApiException {
        resetWorkflowWithHttpInfo(workflowId);
    }

    /**
     * Resets callback times of all non-terminal SIMPLE tasks to 0
     *
     * @param workflowId (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    private ApiResponse<Void> resetWorkflowWithHttpInfo(String workflowId) throws ApiException {
        Call call = resetWorkflowValidateBeforeCall(workflowId, null, null);
        return apiClient.execute(call);
    }

    /**
     * Build call for restart
     *
     * @param workflowId              (required)
     * @param useLatestDefinitions    (optional, default to false)
     * @param progressListener        Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public Call restartCall(String workflowId, Boolean useLatestDefinitions,
                            final ProgressResponseBody.ProgressListener progressListener,
                            final ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                      throws ApiException {
        Object localVarPostBody = null;
        String localVarPath = f("/workflow/%s/restart", workflowId);

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (useLatestDefinitions != null)
            localVarQueryParams.addAll(HttpHelper.parameterToPair("useLatestDefinitions", useLatestDefinitions));

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {};

        final String localVarAccept = HttpHelper.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null)
            localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {};

        final String localVarContentType = HttpHelper.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if (progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener)).build();
                }
            });
        }

        return apiClient.buildCall(localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams,
            localVarPostBody, localVarHeaderParams, localVarFormParams, progressRequestListener);
    }

    private Call restartValidateBeforeCall(String workflowId, Boolean useLatestDefinitions,
                                           final ProgressResponseBody.ProgressListener progressListener,
                                           final ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                                     throws ApiException {
        Assertion.assertNotNull(workflowId, "workflowId");
        Call call = restartCall(workflowId, useLatestDefinitions, progressListener, progressRequestListener);
        return call;
    }

    /**
     * Restarts a completed workflow
     *
     * @param workflowId           (required)
     * @param useLatestDefinitions (optional, default to false)
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    public void restart(String workflowId, Boolean useLatestDefinitions) throws ApiException {
        restartWithHttpInfo(workflowId, useLatestDefinitions);
    }

    /**
     * Restarts a completed workflow
     *
     * @param workflowId           (required)
     * @param useLatestDefinitions (optional, default to false)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    private ApiResponse<Void> restartWithHttpInfo(String workflowId, Boolean useLatestDefinitions) throws ApiException {
        Call call = restartValidateBeforeCall(workflowId, useLatestDefinitions, null, null);
        return apiClient.execute(call);
    }

    /**
     * Build call for resumeWorkflow
     *
     * @param workflowId              (required)
     * @param progressListener        Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public Call resumeWorkflowCall(String workflowId, final ProgressResponseBody.ProgressListener progressListener,
                                   final ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                             throws ApiException {
        Object localVarPostBody = null;
        String localVarPath = f("/workflow/%s/resume", workflowId);

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {};

        final String localVarAccept = HttpHelper.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null)
            localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {};

        final String localVarContentType = HttpHelper.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if (progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener)).build();
                }
            });
        }

        return apiClient.buildCall(localVarPath, "PUT", localVarQueryParams, localVarCollectionQueryParams,
            localVarPostBody, localVarHeaderParams, localVarFormParams, progressRequestListener);
    }

    private Call resumeWorkflowValidateBeforeCall(String workflowId,
                                                  final ProgressResponseBody.ProgressListener progressListener,
                                                  final ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                                            throws ApiException {
        Assertion.assertNotNull(workflowId, "workflowId");
        Call call = resumeWorkflowCall(workflowId, progressListener, progressRequestListener);
        return call;
    }

    /**
     * Resumes the workflow
     *
     * @param workflowId (required)
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    public void resumeWorkflow(String workflowId) throws ApiException {
        resumeWorkflowWithHttpInfo(workflowId);
    }

    /**
     * Resumes the workflow
     *
     * @param workflowId (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    private ApiResponse<Void> resumeWorkflowWithHttpInfo(String workflowId) throws ApiException {
        Call call = resumeWorkflowValidateBeforeCall(workflowId, null, null);
        return apiClient.execute(call);
    }

    /**
     * Build call for retry
     *
     * @param workflowId              (required)
     * @param resumeSubworkflowTasks  (optional, default to false)
     * @param progressListener        Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public Call retryCall(String workflowId, Boolean resumeSubworkflowTasks,
                          final ProgressResponseBody.ProgressListener progressListener,
                          final ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                    throws ApiException {
        Object localVarPostBody = null;
        String localVarPath = f("/workflow/%s/retry", workflowId);

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (resumeSubworkflowTasks != null)
            localVarQueryParams.addAll(HttpHelper.parameterToPair("resumeSubworkflowTasks", resumeSubworkflowTasks));

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {};

        final String localVarAccept = HttpHelper.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null)
            localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {};

        final String localVarContentType = HttpHelper.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if (progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener)).build();
                }
            });
        }

        return apiClient.buildCall(localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams,
            localVarPostBody, localVarHeaderParams, localVarFormParams, progressRequestListener);
    }

    private Call retryValidateBeforeCall(String workflowId, Boolean resumeSubworkflowTasks,
                                         final ProgressResponseBody.ProgressListener progressListener,
                                         final ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                                   throws ApiException {
        Assertion.assertNotNull(workflowId, "workflowId");
        Call call = retryCall(workflowId, resumeSubworkflowTasks, progressListener, progressRequestListener);
        return call;
    }

    /**
     * Retries the last failed task
     *
     * @param workflowId             (required)
     * @param resumeSubworkflowTasks (optional, default to false)
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    public void retry(String workflowId, Boolean resumeSubworkflowTasks) throws ApiException {
        retryWithHttpInfo(workflowId, resumeSubworkflowTasks);
    }

    /**
     * Retries the last failed task
     *
     * @param workflowId             (required)
     * @param resumeSubworkflowTasks (optional, default to false)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    private ApiResponse<Void> retryWithHttpInfo(String workflowId, Boolean resumeSubworkflowTasks) throws ApiException {
        Call call = retryValidateBeforeCall(workflowId, resumeSubworkflowTasks, null, null);
        return apiClient.execute(call);
    }

    /**
     * Build call for skipTaskFromWorkflow
     *
     * @param workflowId              (required)
     * @param taskReferenceName       (required)
     * @param skipTaskRequest         (required)
     * @param progressListener        Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public Call skipTaskFromWorkflowCall(String workflowId, String taskReferenceName, SkipTaskRequest skipTaskRequest,
                                         final ProgressResponseBody.ProgressListener progressListener,
                                         final ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                                   throws ApiException {
        Object localVarPostBody = null;

        String localVarPath = f("/workflow/%s/skiptask/%s", workflowId, taskReferenceName);

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (skipTaskRequest != null)
            localVarQueryParams.addAll(HttpHelper.parameterToPair("skipTaskRequest", skipTaskRequest));

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {};

        final String localVarAccept = HttpHelper.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null)
            localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {};

        final String localVarContentType = HttpHelper.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if (progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener)).build();
                }
            });
        }

        return apiClient.buildCall(localVarPath, "PUT", localVarQueryParams, localVarCollectionQueryParams,
            localVarPostBody, localVarHeaderParams, localVarFormParams, progressRequestListener);
    }

    private Call skipTaskFromWorkflowValidateBeforeCall(String workflowId,
                                                        String taskReferenceName,
                                                        SkipTaskRequest skipTaskRequest,
                                                        final ProgressResponseBody.ProgressListener progressListener,
                                                        final ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                                                  throws ApiException {
        Assertion.assertNotNull(workflowId, "workflowId");
        Assertion.assertNotNull(taskReferenceName, "taskReferenceName");
        Assertion.assertNotNull(skipTaskRequest, "skipTaskRequest");
        Call call = skipTaskFromWorkflowCall(workflowId, taskReferenceName, skipTaskRequest, progressListener,
            progressRequestListener);
        return call;
    }

    /**
     * Skips a given task from a current running workflow
     *
     * @param workflowId        (required)
     * @param taskReferenceName (required)
     * @param skipTaskRequest   (required)
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    public void skipTaskFromWorkflow(String workflowId, String taskReferenceName, SkipTaskRequest skipTaskRequest)
                                                                                                                  throws ApiException {
        skipTaskFromWorkflowWithHttpInfo(workflowId, taskReferenceName, skipTaskRequest);
    }

    /**
     * Skips a given task from a current running workflow
     *
     * @param workflowId        (required)
     * @param taskReferenceName (required)
     * @param skipTaskRequest   (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    private ApiResponse<Void> skipTaskFromWorkflowWithHttpInfo(String workflowId, String taskReferenceName,
                                                               SkipTaskRequest skipTaskRequest) throws ApiException {
        Call call = skipTaskFromWorkflowValidateBeforeCall(workflowId, taskReferenceName, skipTaskRequest, null, null);
        return apiClient.execute(call);
    }

    private Call startWorkflowValidateBeforeCall(StartWorkflowRequest startWorkflowRequest) throws ApiException {
        Assertion.assertNotNull(startWorkflowRequest, "startWorkflowRequest");
        String path = "/workflow/start";
        return apiClient.buildPostCall(path, startWorkflowRequest);
    }

    /**
     * Start a new workflow with StartWorkflowRequest, which allows task to be executed in a domain
     *
     * @param startWorkflowRequest (required)
     * @return String
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
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
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = { "*/*" };
        final String localVarAccept = HttpHelper.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null)
            localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {};

        final String localVarContentType = HttpHelper.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);
        return apiClient.buildCall(localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams,
            localVarPostBody, localVarHeaderParams, localVarFormParams, null);
    }

    /**
     * Update workflow and task status
     * Updates the workflow variables, tasks and triggers evaluation.
     *
     * @return WorkflowRun
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
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
