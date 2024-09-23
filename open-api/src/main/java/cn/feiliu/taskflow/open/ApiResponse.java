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
package cn.feiliu.taskflow.open;

import cn.feiliu.taskflow.open.exceptions.ApiException;
import cn.feiliu.taskflow.open.exceptions.ConflictException;
import lombok.*;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-06-11
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public final class ApiResponse<T> {
    private T                         data;
    /**
     * http 状态码
     */
    private int                       code    = 200;
    /**
     * 接口状态描述
     */
    private String                    msg     = "ok";
    private Map<String, List<String>> headers = new HashMap<>();
    private TaskflowErrorInformation  engineErrorResponse;

    public ApiResponse(T data) {
        this.data = data;
    }

    public ApiResponse(int code, Map<String, List<String>> headers, T data) {
        this.code = code;
        this.headers = headers;
        this.data = data;
    }

    public static ApiResponse noContent() {
        return ApiResponse.of(null, 204, "No Content");
    }

    public static <T> ApiResponse<T> ok(@Nullable T body) {
        return new ApiResponse(body);
    }

    public static ApiResponse of(Map<String, List<String>> headers, int code, String reason) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.code = code;
        apiResponse.msg = reason;
        apiResponse.setHeaders(headers);
        return apiResponse;
    }

    public static ApiResponse of(Map<String, List<String>> headers, TaskflowErrorInformation engineErrorResponse,
                                 int code, String reason) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setEngineErrorResponse(engineErrorResponse);
        apiResponse.code = code;
        apiResponse.msg = reason;
        apiResponse.headers = headers;
        return apiResponse;
    }

    public ApiException makeException() throws ApiException {
        String message = this.msg;
        int code = this.code;
        if (engineErrorResponse != null && engineErrorResponse.getMessage() != null) {
            message = engineErrorResponse.getMessage();
        }
        if (code == 409) {
            throw new ConflictException(message, code, headers, engineErrorResponse);
        }
        throw new ApiException(message, code, headers, engineErrorResponse);
    }

    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }

}
