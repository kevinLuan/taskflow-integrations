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
import cn.feiliu.taskflow.dto.ApiResponse;
import cn.feiliu.taskflow.dto.WorkflowScheduleExecution;
import cn.feiliu.taskflow.exceptions.ApiException;
import com.squareup.okhttp.Call;

import static cn.feiliu.common.api.utils.CommonUtils.f;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-10-04
 */
public class WebhookResourceApi {
    private ApiClient apiClient;

    public WebhookResourceApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 触发WEBHOOK调用
     *
     * @param token (required)
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    public WorkflowScheduleExecution triggerWebhook(String token) throws ApiException {
        if (token == null) {
            throw new ApiException("The token parameter cannot be null");
        }
        String path = f("/trigger/webhook/%s", token);
        Call call = apiClient.buildPostCall(path, null);
        ApiResponse<WorkflowScheduleExecution> apiResponse = apiClient.execute(call, WorkflowScheduleExecution.class);
        return apiResponse.getData();
    }
}