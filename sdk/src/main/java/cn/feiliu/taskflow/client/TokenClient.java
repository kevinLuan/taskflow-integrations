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
package cn.feiliu.taskflow.client;

import cn.feiliu.taskflow.client.api.ITokenClient;
import cn.feiliu.taskflow.client.http.TokenResourceApi;
import cn.feiliu.taskflow.open.ApiResponse;
import cn.feiliu.taskflow.open.dto.Application;
import cn.feiliu.taskflow.open.dto.GenerateTokenRequest;
import cn.feiliu.taskflow.open.dto.TokenResponse;

import java.util.Map;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-06-30
 */
public class TokenClient implements ITokenClient {
    private ApiClient apiClient;

    public TokenClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TokenResponse getToken(GenerateTokenRequest request) {
        ApiResponse<Map<String, Object>> response = TokenResourceApi.generateTokenWithHttpInfo(apiClient, request);
        String token = (String) response.getData().get("accessToken");
        int expire = ((Number) response.getData().get("expire")).intValue();
        return new TokenResponse(token, expire);
    }

    @Override
    public Application getApplication() {
        ApiResponse<Application> response = TokenResourceApi.getAppInfoWithHttpInfo(apiClient);
        return response.getData();
    }
}
