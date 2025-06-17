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
import cn.feiliu.taskflow.utils.TaskflowConfig;
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
        TaskflowConfig config = new PropertiesReader("local_config.properties").toConfig();
        ApiClient apiClient = new ApiClient(config);
        apiClient.addWorker(new MyWorker());
        return apiClient.start();
    }

    public static void main(String[] args) throws IOException {
        logger.info("Starting TaskFlow worker application...");
        ApiClient apiClient = getClient();
        logger.info("TaskFlow worker application started successfully");
    }
}