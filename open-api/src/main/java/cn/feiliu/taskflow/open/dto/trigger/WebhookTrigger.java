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
package cn.feiliu.taskflow.open.dto.trigger;

import lombok.Getter;
import lombok.Setter;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-10-04
 */
@Getter
@Setter
public class WebhookTrigger implements ITrigger {
    String token;

    public WebhookTrigger() {
    }

    public WebhookTrigger(String token) {
        this.token = token;
    }
}
