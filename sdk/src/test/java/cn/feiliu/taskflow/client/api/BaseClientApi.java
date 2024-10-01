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
package cn.feiliu.taskflow.client.api;

import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.client.core.TaskEngine;
import cn.feiliu.taskflow.client.core.WorkflowEngine;
import cn.feiliu.taskflow.common.run.ExecutingWorkflow;
import cn.feiliu.taskflow.open.api.IWorkflowService;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;

public final class BaseClientApi {
    final static String      BASE_URL   = "taskflow_sdk_tests_url";
    final static String      KEY_ID     = "taskflow_sdk_tests_key_id";
    final static String      KEY_SECRET = "taskflow_sdk_tests_key_secret";
    @Getter
    private static ApiClient apiClient  = createApiClient();

    public static ApiClient createApiClient() {
        String basePath = getEnv(BASE_URL, "http://localhost:8082/api");
        String keyId = getEnv(KEY_ID, "19242c5a78a");
        String keySecret = getEnv(KEY_SECRET, "c3ec66ac239f45e2b650b5164f1c7ef0");
        apiClient = new ApiClient(basePath, keyId, keySecret);
        apiClient.setWriteTimeout(30_000);
        apiClient.setReadTimeout(30_000);
        apiClient.setConnectTimeout(30_000);
        return apiClient;
    }

    private static String getEnv(String key, String defaultValue) {
        return Optional.ofNullable(System.getenv(key)).orElse(defaultValue);
    }

    public static TaskEngine getTaskEngine() {
        return apiClient.getTaskEngine();
    }

    public static ISchedulerClient getSchedulerClient() {
        return apiClient.getSchedulerClient();
    }

    public static IWorkflowService getWorkflowClient() {
        return apiClient.getWorkflowClient();
    }

    public static WorkflowEngine getWorkflowEngine() {
        return apiClient.getWorkflowEngine();
    }

    @SneakyThrows
    public static ExecutingWorkflow waitForTerminal(String workflowId, int waitForSeconds) {
        long startTime = System.currentTimeMillis();
        for (;;) {
            ExecutingWorkflow workflow = apiClient.getWorkflowClient().getWorkflow(workflowId, true);
            if (workflow.getStatus().isTerminal()) {
                return workflow;
            } else {
                long cost = System.currentTimeMillis() - startTime;
                if (cost >= waitForSeconds * 1000) {
                    throw new TimeoutException("Timeout exceeded while waiting for workflow to reach terminal state.");
                }
                long remaining = waitForSeconds * 1000 - cost;
                Thread.sleep(Math.min(100, remaining));
            }
        }
    }
}
