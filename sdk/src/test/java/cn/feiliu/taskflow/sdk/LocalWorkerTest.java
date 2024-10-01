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
package cn.feiliu.taskflow.sdk;

import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.client.automator.TaskRunnerConfigurer;
import cn.feiliu.taskflow.sdk.worker.Worker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-05-20
 */
public class LocalWorkerTest {
    public static void main(String[] args) {
        String basePath = "http://localhost:8082/api";
        ApiClient apiClient = new ApiClient(basePath, "e7f76187b8b3427581667bd3d00ac7bc",
            "c3ec66ac239f45e2b650b5164f1c7ef0");
        List<Worker> workers = new ArrayList<>();
        Map<String, Integer> taskThreadCount = new HashMap<>();

        workers.add(new LoadTestWorker("x_test_worker_4"));
        taskThreadCount.put("x_test_worker_4", 1000);

        for (int i = 0; i < 4; i++) {
            workers.add(new LoadTestWorker("x_test_worker_" + i));
            taskThreadCount.put("x_test_worker_" + i, 100);
        }

        TaskRunnerConfigurer configurer = new TaskRunnerConfigurer.Builder(apiClient, workers).withSleepWhenRetry(10)
            .withTaskThreadCount(taskThreadCount).build();
        configurer.init();
        configurer.startRunningTasks();
        System.out.println("Ready...");
    }
}
