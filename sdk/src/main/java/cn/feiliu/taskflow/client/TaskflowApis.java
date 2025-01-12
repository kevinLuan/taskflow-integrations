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

import cn.feiliu.common.api.core.ExecutionHookFactory;
import cn.feiliu.taskflow.client.api.*;
import cn.feiliu.taskflow.client.core.TaskEngine;
import cn.feiliu.taskflow.client.core.WorkflowEngine;
import cn.feiliu.taskflow.client.http.WebhookClient;
import cn.feiliu.taskflow.client.http.WorkflowClient;
import cn.feiliu.taskflow.client.spi.TaskflowGrpcSPI;
import cn.feiliu.taskflow.exceptions.ApiException;

import java.util.Objects;
import java.util.Optional;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-10-04
 */
public final class TaskflowApis {
    final ApiClient                         client;
    private IAuthClient                     authClient;
    private TaskEngine                      taskEngine;
    private WorkflowEngine                  workflowEngine;
    private final IWebhookClient            triggerClient;
    private final IWorkflowClient           workflowClient;
    private final ITaskClient               taskClient;
    private final ISchedulerClient          schedulerClient;
    private final IWorkflowDefClient        workflowDefClient;
    private final Optional<TaskflowGrpcSPI> grpc_api;

    {
        ExecutionHookFactory.register(TaskflowGrpcSPI.class);
        grpc_api = ExecutionHookFactory.getFirstServiceInstance(TaskflowGrpcSPI.class);
    }

    TaskflowApis(ApiClient client) {
        this.client = client;
        this.authClient = new AuthClient(client);
        this.taskEngine = new TaskEngine(client);
        this.workflowDefClient = new WorkflowDefClient(client);
        this.triggerClient = new WebhookClient(client);
        this.workflowClient = new WorkflowClient(client);
        this.taskClient = new TaskClient(client);
        this.schedulerClient = new SchedulerClient(client);
        this.workflowEngine = new WorkflowEngine(workflowDefClient, workflowClient, taskEngine);
    }

    /**
     * 获取 token 客户端
     *
     * @return
     */
    public IAuthClient getAuthClient() {
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
     * 获取工作流引擎API
     *
     * @return
     */
    public WorkflowEngine getWorkflowEngine() {
        return workflowEngine;
    }

    public IWorkflowDefClient getWorkflowDefClient() {
        return workflowDefClient;
    }

    /**
     * 获取工作流客户端
     *
     * @return
     */
    public IWorkflowClient getWorkflowClient() {
        return workflowClient;
    }

    /**
     * 获取任务客户端
     *
     * @return
     */
    public ITaskClient getTaskClient() {
        return taskClient;
    }

    /**
     * 获取调度客户端
     *
     * @return
     */

    public ISchedulerClient getSchedulerClient() {
        return schedulerClient;
    }

    /**
     * 触发操作客户端
     *
     * @return
     */
    public IWebhookClient getWebhookClient() {
        return triggerClient;
    }

    public TaskflowGrpcSPI getGrpcApi() {
        if (client.isUseGRPC()) {
            return Objects.requireNonNull(grpc_api.get());
        } else {
            throw new ApiException("The grpc api is currently not supported");
        }
    }

    public boolean isGrpcSpiAvailable() {
        return grpc_api.isPresent();
    }

    public void shutdown() {
        if (grpc_api.isPresent()) {
            grpc_api.get().shutdown();
        }
        workflowClient.shutdown();
        taskEngine.shutdown();
    }
}
