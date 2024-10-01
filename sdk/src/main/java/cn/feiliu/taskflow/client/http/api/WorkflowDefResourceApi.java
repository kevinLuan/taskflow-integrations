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
import cn.feiliu.taskflow.client.http.Pair;
import cn.feiliu.taskflow.client.utils.Assertion;
import cn.feiliu.taskflow.common.metadata.workflow.WorkflowDefinition;
import cn.feiliu.taskflow.open.ApiResponse;
import cn.feiliu.taskflow.open.exceptions.ApiException;
import com.google.common.collect.Lists;
import com.squareup.okhttp.Call;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作流定义资源服务api
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-07-25
 */
public class WorkflowDefResourceApi {
    private ApiClient apiClient;

    public WorkflowDefResourceApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public boolean createIfAbsent(WorkflowDefinition workflowDef) throws ApiException {
        Assertion.assertNotNull(workflowDef, "workflowDef");
        String localVarPath = "/workflowDef/create";
        Call call = apiClient.buildPostCall(localVarPath, workflowDef, new ArrayList<>());
        ApiResponse<Boolean> response = apiClient.execute(call, Boolean.class);
        return response.getData();
    }

    public boolean updateWorkflowDef(WorkflowDefinition workflowDef) {
        Assertion.assertNotNull(workflowDef, "workflowDef");
        String localVarPath = "/workflowDef/update";
        Call call = apiClient.buildPostCall(localVarPath, workflowDef, new ArrayList<>());
        ApiResponse<Boolean> response = apiClient.execute(call, Boolean.class);
        return response.getData();
    }

    public WorkflowDefinition getWorkflowDef(String name, Integer version) {
        Assertion.assertNotNull(name, "name");
        Assertion.assertNotNull(version, "version");
        String localVarPath = "/workflowDef/" + name;
        List<Pair> list = Lists.newArrayList(new Pair("version", version.toString()));
        Call call = apiClient.buildGetCall(localVarPath, list);
        ApiResponse<WorkflowDefinition> response = apiClient.execute(call, WorkflowDefinition.class);
        return response.getData();
    }

    public boolean publishWorkflowDef(String name, Integer version, Boolean overwrite) {
        Assertion.assertNotNull(name, "name");
        Assertion.assertNotNull(version, "version");
        String localVarPath = "/workflowDef/publish/" + name;
        List<Pair> list = Lists.newArrayList(new Pair("version", version.toString()));
        if (overwrite != null) {
            list.add(new Pair("overwrite", overwrite.toString()));
        }
        Call call = apiClient.buildPostCall(localVarPath, null, list);
        ApiResponse<Boolean> response = apiClient.execute(call, Boolean.class);
        return response.getData();
    }

    public boolean deleteWorkflowDef(String name, Integer version) {
        Assertion.assertNotNull(name, "name");
        Assertion.assertNotNull(version, "version");
        String localVarPath = "/workflowDef/delete/" + name;
        List<Pair> list = Lists.newArrayList(new Pair("version", version.toString()));
        Call call = apiClient.buildDeleteCall(localVarPath, list);
        ApiResponse<Boolean> response = apiClient.execute(call, Boolean.class);
        return response.getData();
    }

    public boolean registerWorkflow(WorkflowDefinition workflowDef, Boolean overwrite) {
        Assertion.assertNotNull(workflowDef, "workflowDef");
        String localVarPath = "/workflowDef/register";
        List<Pair> list = Lists.newArrayList(new Pair("overwrite", overwrite.toString()));
        Call call = apiClient.buildPostCall(localVarPath, workflowDef, list);
        ApiResponse<Boolean> response = apiClient.execute(call, Boolean.class);
        return response.getData();
    }
}
