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

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @author SHOUSHEN.LUAN
 * @since 2023-12-09
 */
@Setter
@Getter
@ToString
public class GenerateTokenRequest {
    @NotNull(message = "The keyId cannot be empty")
    private String keyId;
    @NotNull(message = "The keySecret cannot be empty")
    private String keySecret;

    public GenerateTokenRequest() {
    }

    public GenerateTokenRequest(String keyId, String keySecret) {
        this.keyId = keyId;
        this.keySecret = keySecret;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String keyId;
        private String keySecret;

        public Builder keyId(String keyId) {
            this.keyId = keyId;
            return this;
        }

        public Builder keySecret(String keySecret) {
            this.keySecret = keySecret;
            return this;
        }

        public GenerateTokenRequest build() {
            return new GenerateTokenRequest(keyId, keySecret);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;
        GenerateTokenRequest request = (GenerateTokenRequest) object;
        return Objects.equals(keyId, request.keyId) && Objects.equals(keySecret, request.keySecret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyId, keySecret);
    }
}
