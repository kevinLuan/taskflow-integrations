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
import cn.feiliu.taskflow.client.http.types.TypeFactory;
import cn.feiliu.taskflow.client.utils.Assertion;
import cn.feiliu.taskflow.dto.ApiResponse;
import cn.feiliu.taskflow.dto.tasks.TaskDefinition;
import cn.feiliu.taskflow.exceptions.ApiException;
import com.squareup.okhttp.Call;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-07-23
 */
public class TaskDefResourceApi {
    private ApiClient apiClient;

    public TaskDefResourceApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Create a new workflow definition
     */
    public boolean create(TaskDefinition taskDefinition) throws ApiException {
        Assertion.assertNotNull(taskDefinition, "taskDefinition");
        String localVarPath = "/taskdef/create";
        Call call = apiClient.buildPostCall(localVarPath, taskDefinition, new ArrayList<>());
        ApiResponse<Boolean> response = apiClient.execute(call, Boolean.class);
        return response.getData();
    }

    public boolean updateTaskDef(TaskDefinition taskDefinition) throws ApiException {
        Assertion.assertNotNull(taskDefinition, "taskDefinition");
        String localVarPath = "/taskdef/update";
        Call call = apiClient.buildPostCall(localVarPath, taskDefinition, new ArrayList<>());
        ApiResponse<Boolean> response = apiClient.execute(call, Boolean.class);
        return response.getData();
    }

    public TaskDefinition getTaskDef(String name) throws ApiException {
        Assertion.assertNotNull(name, "name");
        String localVarPath = "/taskdef/" + name;
        Call call = apiClient.buildGetCall(localVarPath, new ArrayList<>());
        ApiResponse<TaskDefinition> response = apiClient.doExecute(call, TypeFactory.of(TaskDefinition.class));
        return response.getData();
    }

    public List<TaskDefinition> getTaskDefs() {
        String localVarPath = "/taskdef/list";
        Call call = apiClient.buildGetCall(localVarPath, new ArrayList<>());
        ApiResponse<List<TaskDefinition>> response = apiClient
            .doExecute(call, TypeFactory.ofList(TaskDefinition.class));
        return response.getData();
    }

    public Boolean publishTaskDef(String name) {
        String localVarPath = "/taskdef/publish/" + name;
        Call call = apiClient.buildPostCall(localVarPath, null);
        ApiResponse<Boolean> response = apiClient.execute(call, Boolean.class);
        return response.getData();
    }

    public Boolean deleteTaskDef(String name) {
        String localVarPath = "/taskdef/" + name;
        Call call = apiClient.buildDeleteCall(localVarPath);
        ApiResponse<Boolean> response = apiClient.execute(call, Boolean.class);
        return response.getData();
    }
}
