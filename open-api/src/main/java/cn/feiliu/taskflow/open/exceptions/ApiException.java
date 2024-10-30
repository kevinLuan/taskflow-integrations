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
package cn.feiliu.taskflow.open.exceptions;

import cn.feiliu.taskflow.open.TaskflowErrorInformation;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-05-20
 */
@Getter
public class ApiException extends TaskflowClientException {
    /*HTTP状态码*/
    private int statusCode = 0;

    public ApiException(Throwable throwable) {
        super(throwable.getMessage(), throwable);
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, int httpStatus, Map<String, List<String>> headers,
                        TaskflowErrorInformation taskflowErrorInformation) {
        super(message, taskflowErrorInformation);
        this.statusCode = httpStatus;
        super.headers = headers;
    }

    public ApiException(String message, Exception e, int statusCode, Map<String, List<String>> headers) {
        super(message, e);
        this.statusCode = statusCode;
        super.headers = headers;
    }

    public ApiException(String message, int statusCode, Map<String, List<String>> headers) {
        super(message);
        super.headers = headers;
        this.statusCode = statusCode;
    }

    public ApiException(int httpCode, String message) {
        super(message);
        this.statusCode = httpCode;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
