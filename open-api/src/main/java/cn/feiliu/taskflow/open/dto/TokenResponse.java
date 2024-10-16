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
package cn.feiliu.taskflow.open.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author SHOUSHEN.LUAN
 * @since 2023-12-09
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    private String  accessToken;
    private String  type;
    /*过期时间 单位:秒*/
    private Integer expire;     // Expiration time

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String  accessToken;
        private String  type;
        private Integer expire;

        public Builder() {
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder expire(Integer expire) {
            this.expire = expire;
            return this;
        }

        public TokenResponse build() {
            return new TokenResponse(accessToken, type, expire);
        }
    }
}