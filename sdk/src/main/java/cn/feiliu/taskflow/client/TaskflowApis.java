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

import cn.feiliu.taskflow.core.TaskEngine;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-10-04
 */
public final class TaskflowApis {
    final ApiClient          client;
    private AuthClient       authClient;
    private TaskEngine       taskEngine;
    private final TaskClient taskClient;
    private TaskDefClient    taskDefClient;

    TaskflowApis(ApiClient client) {
        this.client = client;
        this.authClient = new AuthClient(client);
        this.taskEngine = new TaskEngine(client);
        this.taskClient = new TaskClient(client);
        this.taskDefClient = new TaskDefClient(client);
    }

    /**
     * 获取 token 客户端
     *
     * @return
     */
    public AuthClient getAuthClient() {
        return this.authClient;
    }

    /**
     * 获取任务引擎客户端
     *
     * @return
     */
    public TaskEngine getTaskEngine() {
        return taskEngine;
    }

    /**
     * 获取任务客户端
     *
     * @return
     */
    public TaskClient getTaskClient() {
        return taskClient;
    }

    /**
     * 获取任务定义客户端
     *
     * @return
     */
    public TaskDefClient getTaskDefClient() {
        return taskDefClient;
    }

    public void shutdown() {
        taskEngine.shutdown();
    }
}
