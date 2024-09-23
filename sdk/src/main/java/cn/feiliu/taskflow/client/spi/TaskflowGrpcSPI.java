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
package cn.feiliu.taskflow.client.spi;

import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.common.metadata.tasks.ExecutingTask;
import cn.feiliu.taskflow.common.metadata.tasks.TaskExecResult;
import cn.feiliu.taskflow.common.metadata.tasks.TaskLog;
import cn.feiliu.taskflow.common.metadata.workflow.StartWorkflowRequest;
import cn.feiliu.taskflow.open.exceptions.ApiException;

import java.util.List;
import java.util.concurrent.Future;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-09-23
 */
public interface TaskflowGrpcSPI {
    /**
     * 初始化客户端
     *
     * @param client
     */
    void init(ApiClient client);

    /**
     * 批量拉取任务
     *
     * @param taskType
     * @param workerId
     * @param domain
     * @param count
     * @param timeoutInMillisecond
     * @return
     */
    List<ExecutingTask> batchPollTask(String taskType, String workerId, String domain, int count,
                                      int timeoutInMillisecond);

    /**
     * 更新任务
     *
     * @param taskResult
     */
    void updateTask(TaskExecResult taskResult);

    /**
     * 异步更新任务
     *
     * @param result
     * @return
     */
    Future<?> asyncUpdateTask(TaskExecResult result);

    /**
     * 添加任务日志
     *
     * @param taskLog
     * @return
     */
    Future<?> addLog(TaskLog taskLog);

    /**
     * 运行工作流
     *
     * @param req
     * @return
     * @throws ApiException
     */
    String startWorkflow(StartWorkflowRequest req) throws ApiException;

    /**
     * 释放GRPC资源
     */
    void shutdown();
}
