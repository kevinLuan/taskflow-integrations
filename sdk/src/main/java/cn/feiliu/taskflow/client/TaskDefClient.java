/*
 * Copyright 2024 taskflow, Inc.
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

import cn.feiliu.taskflow.client.api.ITaskDefClient;
import cn.feiliu.taskflow.client.http.api.TaskDefResourceApi;
import cn.feiliu.taskflow.common.metadata.tasks.TaskDefinition;

import java.util.List;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-07-23
 */
public class TaskDefClient implements ITaskDefClient {
    final ApiClient          apiClient;
    final TaskDefResourceApi taskDefResourceApi;

    public TaskDefClient(ApiClient apiClient) {
        this.apiClient = apiClient;
        taskDefResourceApi = new TaskDefResourceApi(apiClient);
    }

    @Override
    public boolean createIfAbsent(TaskDefinition taskDefinition) {
        return taskDefResourceApi.create(taskDefinition);
    }

    @Override
    public boolean updateTaskDef(TaskDefinition taskDefinition) {
        return taskDefResourceApi.updateTaskDef(taskDefinition);
    }

    @Override
    public TaskDefinition getTaskDef(String name) {
        return taskDefResourceApi.getTaskDef(name);
    }

    @Override
    public List<TaskDefinition> getTaskDefs() {
        return taskDefResourceApi.getTaskDefs();
    }

    @Override
    public boolean publishTaskDef(String name) {
        return taskDefResourceApi.publishTaskDef(name);
    }

    @Override
    public boolean deleteTaskDef(String name) {
        return taskDefResourceApi.deleteTaskDef(name);
    }

    @Override
    public void close() throws Exception {

    }
}
