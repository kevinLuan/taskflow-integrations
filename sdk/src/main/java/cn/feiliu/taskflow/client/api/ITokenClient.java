/*
 * Copyright 2024 taskflow, Inc.
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
package cn.feiliu.taskflow.client.api;

import cn.feiliu.taskflow.open.api.ITokenService;
import cn.feiliu.taskflow.open.dto.GenerateTokenRequest;
import cn.feiliu.taskflow.open.dto.TokenResponse;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-06-30
 */
public interface ITokenClient extends ITokenService, AutoCloseable {

    default TokenResponse getToken(String keyId, String keySecret) {
        if (keyId == null || keySecret == null) {
            throw new RuntimeException("KeyId and KeySecret must be set in order to get an authentication token");
        }
        GenerateTokenRequest request = new GenerateTokenRequest();
        request.setKeyId(keyId);
        request.setKeySecret(keySecret);
        return getToken(request);
    }
}
