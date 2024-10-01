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

/**
 * 工作任务节点参数异常
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-09-04
 */
public class TaskParameterException extends IllegalArgumentException {
    public TaskParameterException(String message) {
        super(message);
    }

    public TaskParameterException(String message, Throwable t) {
        super(message, t);
    }
}
