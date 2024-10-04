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
package cn.feiliu.taskflow.client.http;

import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.client.api.IWebhookClient;
import cn.feiliu.taskflow.client.http.api.WebhookResourceApi;
import cn.feiliu.taskflow.open.dto.WorkflowScheduleExecution;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-10-04
 */
public class WebhookClient implements IWebhookClient {
    private WebhookResourceApi triggerResourceApi;

    public WebhookClient(ApiClient client) {
        this.triggerResourceApi = new WebhookResourceApi(client);
    }

    @Override
    public WorkflowScheduleExecution triggerWebhook(String token) {
        return triggerResourceApi.triggerWebhook(token);
    }
}
