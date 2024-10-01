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

import java.util.List;
import java.util.Map;

public class ConflictException extends ApiException {

    public ConflictException(String message, int statusCode, Map<String, List<String>> headers,
                             TaskflowErrorInformation taskflowErrorInformation) {
        super(statusCode, message);
        super.headers = headers;
        super.setTaskflowErrorInformation(taskflowErrorInformation);
    }

    public ConflictException(String message) {
        super(409, message == null ? "Conflict" : message);
    }
}
