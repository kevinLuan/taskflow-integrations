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

import cn.feiliu.taskflow.annotations.WorkerTask;
import cn.feiliu.taskflow.executor.task.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorkerTasksScanner implements ApplicationListener<ApplicationReadyEvent> {
    static final Logger                 logger      = LoggerFactory.getLogger(WorkerTasksScanner.class);
    private final Map<Class<?>, Object> workerBeans = new ConcurrentHashMap<>();

    @Autowired
    private ApplicationContext          applicationContext;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String[] names = applicationContext.getBeanNamesForType(Worker.class);
        for (String name : names) {
            Object bean = applicationContext.getBean(name);
            workerBeans.putIfAbsent(bean.getClass(), bean);
        }
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Service.class);
        /*这里不考虑使用 @Component 原因 考虑到暴露的方法通常应该是一个业务方法*/
        //applicationContext.getBeansWithAnnotation(Component.class);
        for (Object bean : beans.values()) {
            Method[] methods = bean.getClass().getDeclaredMethods();
            for (Method method : methods) {
                WorkerTask annotation = AnnotationUtils.findAnnotation(method, WorkerTask.class);
                if (annotation != null) {
                    workerBeans.putIfAbsent(bean.getClass(), bean);
                    break;
                }
            }
        }
    }

    public Collection<Object> getWorkerBeans() {
        workerBeans.forEach((k, v) -> {
            logger.info("register worker bean name:{} , {}", k, v);
        });
        return workerBeans.values();
    }
}