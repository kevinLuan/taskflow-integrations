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

import cn.feiliu.taskflow.common.def.TaskDefinition;
import cn.feiliu.taskflow.common.dto.tasks.TaskBasicInfo;
import cn.feiliu.taskflow.executor.extension.TaskHandler;
import cn.feiliu.taskflow.executor.task.Worker;
import cn.feiliu.taskflow.http.TaskDefResourceApi;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author kevin.luan
 * @since 2025-06-02
 */
public class TaskDefClient {
    static final Logger              log = LoggerFactory.getLogger(TaskDefClient.class);
    // API客户端实例
    protected ApiClient              apiClient;

    // 任务资源API实例
    private final TaskDefResourceApi taskDefResourceApi;

    /**
     * 构造函数
     *
     * @param apiClient API客户端实例
     */
    public TaskDefClient(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.taskDefResourceApi = new TaskDefResourceApi(apiClient);
    }

    /**
     * 获取API客户端实例
     *
     * @return API客户端实例
     */
    public ApiClient getApiClient() {
        return apiClient;
    }

    /**
     * 获取所有自定义的任务名称
     *
     * @return
     */
    public Set<String> getTaskNames() {
        List<TaskBasicInfo> list = taskDefResourceApi.getTaskDefs();
        return list.stream().map((t) -> t.getName()).collect(Collectors.toSet());
    }

    /**
     * 创建任务定义
     *
     * @param worker
     */
    public void createTaskDef(Worker worker) {
        TaskDefinition taskDef = createTaskDefinition(worker);
        taskDefResourceApi.createTaskDef(taskDef);
        log.info("create task def {} success", worker.getTaskDefName());
    }

    /**
     * 更新任务定义
     *
     * @param worker
     */
    public void updateTaskDef(Worker worker) {
        TaskDefinition taskDefinition = taskDefResourceApi.getTaskDef(worker.getTaskDefName());
        Map<String, Object> template = new HashMap<>();
        for (String inputName : worker.getInputNames()) {
            template.put(inputName, "");
        }
        getTag(worker).ifPresent(taskDefinition::setTag);
        getDescription(worker).ifPresent(taskDefinition::setDescription);
        taskDefinition.getInputTemplate().putAll(template);
        taskDefinition.setInputKeys(Lists.newArrayList(template.keySet()));
        taskDefinition.setOutputKeys(Lists.newArrayList(worker.getOutputNames()));
        taskDefResourceApi.updateTaskDef(taskDefinition);
        log.info("update task def {} success", worker.getTaskDefName());
    }

    private Optional<String> getTag(Worker worker) {
        Optional<TaskHandler> optional = apiClient.getTaskHandlerManager().getTaskHandler(worker.getTaskDefName());
        if (optional.isPresent()) {
            TaskHandler taskHandler = optional.get();
            String tag = taskHandler.getWorker().tag();
            if (tag != null) {
                tag = tag.trim();
                if (tag.length() > 10) {
                    tag = tag.substring(0, 10);
                }
            }
            return Optional.of(tag);
        }
        return Optional.empty();
    }

    private Optional<String> getDescription(Worker worker) {
        Optional<TaskHandler> optional = apiClient.getTaskHandlerManager().getTaskHandler(worker.getTaskDefName());
        if (optional.isPresent()) {
            TaskHandler taskHandler = optional.get();
            return Optional.ofNullable(taskHandler.getWorker().description());
        }
        return Optional.empty();
    }

    private TaskDefinition createTaskDefinition(Worker worker) {
        TaskDefinition taskDef = new TaskDefinition();
        taskDef.setName(worker.getTaskDefName());
        taskDef.setRetryDelaySeconds(1);
        taskDef.setConcurrentExecLimit(10);
        getTag(worker).ifPresent(taskDef::setTag);
        getDescription(worker).ifPresent(taskDef::setDescription);
        Map<String, Object> template = new HashMap<>();
        for (String inputName : worker.getInputNames()) {
            taskDef.addInputKey(inputName);
            template.put(inputName, "");
        }
        taskDef.getInputTemplate().putAll(template);
        for (String outputName : worker.getOutputNames()) {
            taskDef.addOutputKey(outputName);
        }
        return taskDef;
    }
}
