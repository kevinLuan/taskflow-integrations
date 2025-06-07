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
import cn.feiliu.taskflow.core.TaskEngine;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 启动时，扫描所有被 {@link Workers} 注解的类，并注册为 Worker
 */
class TaskflowReadyListener implements ApplicationListener<ContextRefreshedEvent> {

    private final TaskEngine                   taskEngine;
    private static final Cache<String, Object> CACHE = CacheBuilder.newBuilder().maximumSize(1024)
                                                         .expireAfterWrite(1, TimeUnit.MINUTES).build();

    public TaskflowReadyListener(ApiClient apiClient) {
        this.taskEngine = apiClient.getApis().getTaskEngine();
    }

    @Override
    @SneakyThrows
    public void onApplicationEvent(ContextRefreshedEvent refreshedEvent) {
        ApplicationContext applicationContext = refreshedEvent.getApplicationContext();
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Workers.class);
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            if (CACHE.getIfPresent(entry.getKey()) == null) {
                Object bean = entry.getValue();
                taskEngine.addWorkers(bean);
                CACHE.put(entry.getKey(), bean);
            }
        }
        taskEngine.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            taskEngine.shutdown();
        }));
    }
}
