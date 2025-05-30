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
import cn.feiliu.taskflow.dto.ApiResponse;
import cn.feiliu.taskflow.dto.Application;
import cn.feiliu.taskflow.dto.TokenResponse;
import cn.feiliu.taskflow.exceptions.ApiException;
import com.squareup.okhttp.Call;

import java.util.*;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-05-20
 */
public class TokenResourceApi {

    /**
     * Generate JWT with the given access key
     *
     * @param apiClient ApiClient
     * @return ApiResponse&lt;Response&gt;
     * @throws ApiException 如果调用API失败，例如服务器错误或无法反序列化响应体
     */
    public static ApiResponse<TokenResponse> refreshTokenWithHttpInfo(ApiClient apiClient) throws ApiException {
        Call call = apiClient.buildPostCall("/auth/token", null);
        return apiClient.execute(call, TokenResponse.class);
    }

    /**
     * 构建getUserInfo的调用
     */
    public static Call getAppInfoCall(ApiClient apiClient) throws ApiException {
        String localVarPath = "/app/info";
        return apiClient.buildGetCall(localVarPath, new ArrayList<>());
    }

    /**
     * 从令牌获取用户信息
     *
     * @return ApiResponse&lt;Object&gt;
     * @throws ApiException 如果调用API失败，例如服务器错误或无法反序列化响应体
     */
    public static ApiResponse<Application> getAppInfoWithHttpInfo(ApiClient apiClient) throws ApiException {
        Call call = getAppInfoCall(apiClient);
        return apiClient.execute(call, Application.class);
    }
}
