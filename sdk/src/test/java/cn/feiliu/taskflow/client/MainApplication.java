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
package cn.feiliu.taskflow.client;

import cn.feiliu.taskflow.utils.PropertiesReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-09-17
 */
public class MainApplication {
    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);

    private static ApiClient getClient() throws IOException {
        logger.info("Initializing ApiClient...");
        PropertiesReader reader = new PropertiesReader("taskflow_config.properties");
        String url = reader.getProperty("taskflow.base-url");
        String keyId = reader.getProperty("taskflow.client.key-id");
        String keySecret = reader.getProperty("taskflow.client.secret");
        logger.info("Connecting to TaskFlow server at: {}", url);
        ApiClient apiClient = new ApiClient(url, keyId, keySecret);
        apiClient.autoRegisterTask(reader.getBoolean("taskflow.client.auto-register-task"));
        apiClient.getApis().getTaskEngine().addWorkers(new MyWorker()).initWorkerTasks().startRunningTasks();
        logger.info("ApiClient initialized successfully");
        return apiClient;
    }

    public static void main(String[] args) throws IOException {
        logger.info("Starting TaskFlow worker application...");
        ApiClient apiClient = getClient();
        logger.info("TaskFlow worker application started successfully");
    }
}