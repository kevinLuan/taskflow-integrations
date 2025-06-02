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
package cn.feiliu.taskflow.executor.extension;

import cn.feiliu.taskflow.annotations.WorkerTask;
import cn.feiliu.taskflow.common.utils.Assertions;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务处理程序管理器
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-02-29
 */
public class TaskHandlerManager {
    private final Map<String, TaskHandler> taskMap = new ConcurrentHashMap<>();

    /**
     * 注册任务到任务管理器
     *
     * @param worker
     * @param bean
     * @param method
     */
    public void registerTask(WorkerTask worker, Object bean, Method method) {
        if (worker == null || bean == null || method == null) {
            throw new IllegalArgumentException("worker, bean, method must not be null");
        }
        Assertions.assertTaskName(worker.value());
        if (taskMap.containsKey(worker.value())) {
            throw new IllegalArgumentException("taskName:`" + worker.value() + "` already exists");
        }
        taskMap.put(worker.value(), new TaskHandler(worker, bean, method));
    }

    /**
     * 获取任务定义
     *
     * @param taskName
     * @return
     */
    public Optional<TaskHandler> getTaskHandler(String taskName) {
        return Optional.ofNullable(taskMap.get(taskName));
    }

    /**
     * 获取所有的任务定义
     *
     * @return
     */
    public Map<String, TaskHandler> getTasks() {
        return Collections.unmodifiableMap(taskMap);
    }

    /**
     * 获取所有的任务名称
     *
     * @return
     */
    public Set<String> getTaskNames() {
        return Collections.unmodifiableSet(taskMap.keySet());
    }
}
