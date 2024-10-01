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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;

@Slf4j
public class ApiClientAutoConfiguration {

    public static final String TASKFLOW_SERVER_URL    = "taskflow.server.url";
    public static final String TASKFLOW_CLIENT_KEY_ID = "taskflow.security.client.key-id";
    public static final String TASKFLOW_CLIENT_SECRET = "taskflow.security.client.secret";
    public static final String TASKFLOW_GRPC_SERVER   = "taskflow.grpc.host";

    public static final String TASKFLOW_GRPC_PORT     = "taskflow.grpc.port";

    public static final String TASKFLOW_GRPC_SSL      = "taskflow.grpc.ssl";

    @Bean
    public ApiClient getApiClient(Environment env) {
        String rootUri = env.getProperty(TASKFLOW_SERVER_URL);
        String keyId = env.getProperty(TASKFLOW_CLIENT_KEY_ID);
        String secret = env.getProperty(TASKFLOW_CLIENT_SECRET);
        ApiClient apiClient = new ApiClient(rootUri, keyId, secret);
        apiClient = configureGrpc(apiClient, env);
        return apiClient;
    }

    private ApiClient configureGrpc(ApiClient apiClient, Environment env) {
        String grpcHost = env.getProperty(TASKFLOW_GRPC_SERVER);
        String grpcPort = env.getProperty(TASKFLOW_GRPC_PORT);
        boolean useSSL = Boolean.parseBoolean(env.getProperty(TASKFLOW_GRPC_SSL));
        if (StringUtils.isNotBlank(grpcHost)) {
            log.info("Using gRPC for worker communication {}:{}, usingSSL:{}", grpcHost, grpcPort, useSSL);
            int port = Integer.parseInt(grpcPort);
            apiClient.setUseGRPC(grpcHost, port);
            apiClient.setUseSSL(useSSL);
        }
        return apiClient;
    }

    @Bean("taskflowReadyListener")
    public ApplicationListener<ContextRefreshedEvent> taskflowReadyListener(ApiClient apiClient) {
        return new TaskflowReadyListener(apiClient);
    }
}
