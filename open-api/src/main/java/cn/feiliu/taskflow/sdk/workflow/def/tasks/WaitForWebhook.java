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
package cn.feiliu.taskflow.sdk.workflow.def.tasks;

import cn.feiliu.taskflow.common.metadata.tasks.TaskType;
import cn.feiliu.taskflow.common.metadata.workflow.FlowTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 等待一个webhook调用
 * JSON PATH 匹配参数示例:
 * <pre>
 *     {
 *     "matches": {
 *         "$['event']['type']": "message",
 *         "$['event']['text']": "hello"
 *     }
 * }
 * </pre>
 *
 * @author SHOUSHEN.LUAN
 * @since 2025-01-01
 */

public class WaitForWebhook extends Task<WaitForWebhook> {
    public final static String MATCHES = "matches";

    public WaitForWebhook(String taskReferenceName, Payload payload) {
        super(taskReferenceName, TaskType.WAIT_FOR_WEBHOOK);
        input(MATCHES, payload.getMatches());
    }

    public WaitForWebhook(String taskReferenceName, Map<String, Object> matches) {
        super(taskReferenceName, TaskType.WAIT_FOR_WEBHOOK);
        input(MATCHES, Objects.requireNonNull(matches));
    }

    WaitForWebhook(FlowTask workflowTask) {
        super(workflowTask);
    }

    /**
     * 添加事件类型
     */
    public WaitForWebhook setPayload(Payload payload) {
        input(MATCHES, payload.getMatches());
        return this;
    }

    public static class Payload {
        private final Map<String, Object> matches;

        private Payload(Builder builder) {
            this.matches = builder.matches;
        }

        public static Payload.Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final Map<String, Object> matches = new HashMap<>();

            public Builder add(String name, String value) {
                this.matches.put(name, value);
                return this;
            }

            public Builder eventType(String value) {
                this.matches.put("$['event']['type']", Objects.requireNonNull(value));
                return this;
            }

            public Builder eventText(String value) {
                this.matches.put("$['event']['text']", Objects.requireNonNull(value));
                return this;
            }

            public Payload build() {
                return new Payload(this);
            }
        }

        public Map<String, Object> getMatches() {
            return matches;
        }
    }
}
