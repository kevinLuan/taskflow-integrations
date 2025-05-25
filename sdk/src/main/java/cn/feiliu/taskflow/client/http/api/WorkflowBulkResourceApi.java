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
import cn.feiliu.taskflow.client.utils.Assertion;
import cn.feiliu.taskflow.client.utils.HttpHelper;
import cn.feiliu.taskflow.dto.ApiResponse;
import cn.feiliu.taskflow.dto.result.BulkResponseResult;
import cn.feiliu.taskflow.exceptions.ApiException;
import com.google.common.collect.Lists;
import com.squareup.okhttp.Call;

import java.util.List;

public class WorkflowBulkResourceApi {
    private ApiClient apiClient;

    public WorkflowBulkResourceApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 终止工作流执行
     */
    public BulkResponseResult terminate(List<String> workflowIds, String reason) throws ApiException {
        Assertion.assertNotEmpty(workflowIds, "workflowIds");
        Assertion.assertNotNull(reason, "reason");
        String path = "/workflow/bulk/terminate";
        List<Pair> queryParams = Lists.newArrayList(HttpHelper.parameterToPair("reason", reason));
        Call call = apiClient.buildPostCall(path, workflowIds, queryParams);
        ApiResponse<BulkResponseResult> resp = apiClient.execute(call, BulkResponseResult.class);
        return resp.getData();
    }

}
