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

import cn.feiliu.common.api.model.resp.DataResult;
import cn.feiliu.taskflow.common.dto.TokenResponse;
import cn.feiliu.taskflow.http.TokenResourceApi;

/**
 * 认证客户端类
 * 用于处理Token相关的认证操作
 * 
 * @author SHOUSHEN.LUAN
 * @since 2024-06-30
 */
public class AuthClient {
    /**
     * API客户端实例
     */
    private final ApiClient apiClient;

    /**
     * 构造函数
     * @param apiClient API客户端实例
     */
    public AuthClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 刷新Token
     * 通过调用TokenResourceApi刷新当前的访问令牌
     * 
     * @return TokenResponse 包含新的Token信息的响应对象
     */
    public TokenResponse refreshToken() {
        DataResult<TokenResponse> response = TokenResourceApi.refreshTokenWithHttpInfo(apiClient);
        return response.getData();
    }
}
