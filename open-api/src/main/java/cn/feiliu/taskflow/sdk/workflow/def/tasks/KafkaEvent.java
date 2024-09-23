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
package cn.feiliu.taskflow.sdk.workflow.def.tasks;

import java.util.Map;
import java.util.Objects;

/**
 * kafka 事件包装器
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-04-10
 */
public class KafkaEvent extends Event {
    public KafkaEvent(String taskReferenceName, String eventSink) {
        super(taskReferenceName, eventSink);
    }

    public KafkaEvent key(Object key) {
        super.input("key", Objects.requireNonNull(key));
        return this;
    }

    public KafkaEvent payload(Object payload) {
        super.input("payload", Objects.requireNonNull(payload));
        return this;
    }

    public KafkaEvent headers(Map<String, Object> headers) {
        super.input("headers", Objects.requireNonNull(headers));
        return this;
    }

}
