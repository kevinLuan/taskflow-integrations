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
import cn.feiliu.taskflow.common.dto.TokenResponse;
import cn.feiliu.taskflow.common.exceptions.ApiException;
import okhttp3.Call;

/**
 * Token资源API类
 * 提供访问和刷新认证Token的相关功能
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-05-20
 */
public class TokenResourceApi {

    /**
     * 使用给定的访问密钥生成JWT Token
     *
     * @param apiClient API客户端实例
     * @return ApiResponse<TokenResponse> 包含Token信息的API响应对象
     * @throws ApiException 如果调用API失败，例如服务器错误或无法反序列化响应体
     */
    public static ApiResponse<TokenResponse> refreshTokenWithHttpInfo(ApiClient apiClient) throws ApiException {
        Call call = apiClient.buildPostCall("/auth/token", null);
        return apiClient.execute(call, TokenResponse.class);
    }
}
