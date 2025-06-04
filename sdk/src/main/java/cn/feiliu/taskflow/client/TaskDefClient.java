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
import cn.feiliu.taskflow.http.TaskDefResourceApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
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

    public void createTaskDef(String taskDefName) {
        TaskDefinition taskDef = new TaskDefinition();
        taskDef.setName(taskDefName);
        taskDefResourceApi.createTaskDef(taskDef);
        log.info("create task def {} success", taskDefName);
    }

    public void updateTaskDef(String taskDefName) {
        TaskDefinition taskDef = new TaskDefinition();
        taskDef.setName(taskDefName);
        taskDefResourceApi.updateTaskDef(taskDef);
        log.info("update task def {} success", taskDefName);
    }
}
