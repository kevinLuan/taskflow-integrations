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
package cn.feiliu.taskflow.client.http;

import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.common.model.WorkflowRun;
import cn.feiliu.taskflow.client.api.IWorkflowClient;
import cn.feiliu.taskflow.open.api.IWorkflowService;
import cn.feiliu.taskflow.open.dto.CorrelationIdsSearchRequest;
import cn.feiliu.taskflow.client.http.api.WorkflowBulkResourceApi;
import cn.feiliu.taskflow.client.http.api.WorkflowResourceApi;
import cn.feiliu.taskflow.open.dto.WorkflowProgressUpdate;
import cn.feiliu.taskflow.open.exceptions.ApiException;
import cn.feiliu.taskflow.open.exceptions.ConflictException;
import com.google.common.base.Preconditions;
import cn.feiliu.taskflow.common.metadata.workflow.WorkflowRerunRequest;
import cn.feiliu.taskflow.common.metadata.workflow.SkipTaskRequest;
import cn.feiliu.taskflow.common.metadata.workflow.StartWorkflowRequest;
import cn.feiliu.taskflow.common.model.BulkResponseResult;
import cn.feiliu.taskflow.common.run.ExecutingWorkflow;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class WorkflowClient implements IWorkflowClient {
    private static final Logger           log = LoggerFactory.getLogger(WorkflowClient.class);

    protected ApiClient                   apiClient;

    private final WorkflowResourceApi     httpClient;

    private final WorkflowBulkResourceApi bulkResourceApi;

    private ExecutorService               executorService;

    public WorkflowClient(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.httpClient = new WorkflowResourceApi(apiClient);
        this.bulkResourceApi = new WorkflowBulkResourceApi(apiClient);
        if (!apiClient.isUseGRPC()) {
            int threadCount = apiClient.getExecutorThreadCount() > 0 ? apiClient.getExecutorThreadCount() : 64;
            this.executorService = new ThreadPoolExecutor(0, threadCount, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<>());
        }
    }

    public IWorkflowService withReadTimeout(int readTimeout) {
        apiClient.setReadTimeout(readTimeout);
        return this;
    }

    public IWorkflowService setWriteTimeout(int writeTimeout) {
        apiClient.setWriteTimeout(writeTimeout);
        return this;
    }

    public IWorkflowService withConnectTimeout(int connectTimeout) {
        apiClient.setConnectTimeout(connectTimeout);
        return this;
    }

    @Override
    public String startWorkflow(StartWorkflowRequest req) throws ConflictException {
        if (apiClient.isUseGRPC()) {
            return apiClient.getApis().getGrpcApi().startWorkflow(req);
        } else {
            return httpClient.startWorkflow(req);
        }
    }

    @Override
    public ExecutingWorkflow getWorkflow(String workflowId, boolean includeTasks) {
        return httpClient.getExecutionStatus(workflowId, includeTasks);
    }

    @Override
    public List<ExecutingWorkflow> getWorkflows(String name, String correlationId, boolean includeClosed,
                                                boolean includeTasks) {
        return httpClient.getWorkflowsByCorrelationId(name, correlationId, includeClosed, includeTasks);
    }

    @Override
    public void deleteWorkflow(String workflowId, boolean archiveWorkflow) {
        httpClient.delete(workflowId, archiveWorkflow);
    }

    @Override
    public List<String> getRunningWorkflow(String workflowName, Integer version) {
        return httpClient.getRunningWorkflow(workflowName, version, null, null);
    }

    @Override
    public List<String> getWorkflowsByTimePeriod(String workflowName, int version, Long startTime, Long endTime) {
        return httpClient.getRunningWorkflow(workflowName, version, startTime, endTime);
    }

    @Override
    public void runDecider(String workflowId) {
        httpClient.decide(workflowId);
    }

    @Override
    public void pauseWorkflow(String workflowId) {
        httpClient.pauseWorkflow(workflowId);
    }

    @Override
    public void resumeWorkflow(String workflowId) {
        httpClient.resumeWorkflow(workflowId);
    }

    @Override
    public void skipTaskFromWorkflow(String workflowId, String taskReferenceName) {
        Preconditions.checkArgument(StringUtils.isNotBlank(workflowId), "workflow id cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(taskReferenceName), "Task reference name cannot be blank");
        SkipTaskRequest skipTaskRequest = new SkipTaskRequest();
        httpClient.skipTaskFromWorkflow(workflowId, taskReferenceName, skipTaskRequest);
    }

    @Override
    public String rerunWorkflow(String workflowId, WorkflowRerunRequest rerunWorkflowRequest) {
        return httpClient.rerun(rerunWorkflowRequest, workflowId);
    }

    @Override
    public void restart(String workflowId, boolean useLatestDefinitions) {
        httpClient.restart(workflowId, useLatestDefinitions);
    }

    @Override
    public void retryLastFailedTask(String workflowId) {
        httpClient.retry(workflowId, true);
    }

    @Override
    public void terminateWorkflow(String workflowId, String reason) {
        httpClient.terminateWithAReason(workflowId, reason);
    }

    @Override
    public BulkResponseResult terminateWorkflows(List<String> workflowIds, String reason) throws ApiException {
        Preconditions.checkArgument(!workflowIds.isEmpty(), "workflow id cannot be blank");
        return bulkResourceApi.terminate(workflowIds, reason);
    }

    @Override
    public Map<String, List<ExecutingWorkflow>> getWorkflowsByNamesAndCorrelationIds(Boolean includeClosed,
                                                                                     Boolean includeTasks,
                                                                                     CorrelationIdsSearchRequest request) {
        return httpClient.getWorkflowsByNamesAndCorrelationIds(request, includeClosed, includeTasks);
    }

    @Override
    public ExecutingWorkflow updateVariables(String workflowId, Map<String, Object> variables) {
        return httpClient.updateVariables(workflowId, variables);
    }

    @Override
    public WorkflowRun updateWorkflow(WorkflowProgressUpdate body) {
        String requestId = UUID.randomUUID().toString();
        body.setRequestId(requestId);
        return httpClient.updateWorkflowState(body);
    }

    @Override
    public void resetWorkflow(String workflowId) {
        httpClient.resetWorkflow(workflowId);
    }

    @Override
    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
