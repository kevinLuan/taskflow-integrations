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

import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.common.dto.ApiResponse;
import cn.feiliu.taskflow.common.dto.tasks.*;
import cn.feiliu.taskflow.http.types.TypeFactory;
import okhttp3.Call;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务资源API类
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
     * 获取所有任务定义
     *
     * @return
     */
    public List<TaskBasicInfo> getTaskDefs() {
        String path = "/taskdef/list";
        Call call = apiClient.buildGetCall(path, new ArrayList<>());
        ApiResponse<List<TaskBasicInfo>> resp = apiClient.doExecute(call, TypeFactory.ofList(TaskBasicInfo.class));
        return resp.getData();
    }
}
