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
package cn.feiliu.taskflow.client.spring;

import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.utils.TaskflowConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;

@Slf4j
public class ApiClientAutoConfiguration {

    @Bean
    public TaskflowConfig getConfig(Environment env) {
        TaskflowConfig config = new TaskflowConfig();
        config.setBaseUrl(env.getProperty("taskflow.base-url"));
        config.setKeyId(env.getProperty("taskflow.key-id"));
        config.setKeySecret(env.getProperty("taskflow.key-secret"));
        config.setWebSocketUrl(env.getProperty("taskflow.web-socket-url"));
        config.setAutoRegister(env.getProperty("taskflow.auto-register", Boolean.class));
        config.setUpdateExisting(env.getProperty("taskflow.update-existing", Boolean.class));
        config.setWebSocketUrl(env.getProperty("taskflow.web-socket-url"));
        return config;
    }

    @Bean("workerTasksScanner")
    public WorkerTasksScanner workerTasksScanner() {
        return new WorkerTasksScanner();
    }

    @Bean("apiClient")
    @DependsOn("workerTasksScanner")
    public ApiClient apiClient(TaskflowConfig config, WorkerTasksScanner workerTasksScanner) {
        ApiClient apiClient = new ApiClient(config);
        apiClient.addWorker(workerTasksScanner.getWorkerBeans());
        return apiClient.start();
    }
}
