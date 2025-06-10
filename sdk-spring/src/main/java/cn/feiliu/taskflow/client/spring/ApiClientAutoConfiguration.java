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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Slf4j
@Configuration
@EnableConfigurationProperties(TaskflowProperties.class)
@ConditionalOnProperty(prefix = "taskflow", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ApiClientAutoConfiguration {

    @Bean
    public TaskflowConfig taskflowConfig(TaskflowProperties properties) {
        log.info("Initializing TaskFlow configuration with properties: keyId={}, baseUrl={}, autoRegister={}, updateExisting={}", 
                properties.getKeyId(), properties.getBaseUrl(), properties.getAutoRegister(), properties.getUpdateExisting());
        
        // 验证必需的配置项
        if (properties.getKeyId() == null || properties.getKeyId().trim().isEmpty()) {
            throw new IllegalArgumentException("TaskFlow keyId不能为空，请在配置文件中设置taskflow.key-id");
        }
        if (properties.getKeySecret() == null || properties.getKeySecret().trim().isEmpty()) {
            throw new IllegalArgumentException("TaskFlow keySecret不能为空，请在配置文件中设置taskflow.key-secret");
        }
        
        TaskflowConfig config = new TaskflowConfig();
        config.setKeyId(properties.getKeyId());
        config.setKeySecret(properties.getKeySecret());
        config.setBaseUrl(properties.getBaseUrl());
        config.setWebSocketUrl(properties.getWebSocketUrl());
        config.setAutoRegister(properties.getAutoRegister());
        config.setUpdateExisting(properties.getUpdateExisting());
        return config;
    }

    @Bean("workerTasksScanner")
    public WorkerTasksScanner workerTasksScanner() {
        return new WorkerTasksScanner();
    }

    @Bean("apiClient")
    @DependsOn("workerTasksScanner")
    public ApiClient apiClient(TaskflowConfig config, WorkerTasksScanner workerTasksScanner) {
        log.info("Creating TaskFlow ApiClient with configuration");
        return new ApiClient(config);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> apiClientInitializer(ApiClient apiClient,
                                                                           WorkerTasksScanner workerTasksScanner) {
        return new ApplicationListener<ApplicationReadyEvent>() {
            @Override
            public void onApplicationEvent(ApplicationReadyEvent event) {
                log.info("TaskFlow application ready, starting worker registration and client initialization");
                apiClient.addWorker(workerTasksScanner.getWorkerBeans());
                apiClient.start();
                log.info("TaskFlow client started successfully");
            }
        };
    }
}
