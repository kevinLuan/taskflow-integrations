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
package cn.feiliu.taskflow.automator.scheduling;

import cn.feiliu.taskflow.automator.TaskPollExecutor;
import cn.feiliu.taskflow.automator.WorkerProcess;
import cn.feiliu.taskflow.executor.task.Worker;
import cn.feiliu.taskflow.utils.TaskflowConfig;
import cn.feiliu.taskflow.ws.msg.SubTaskPayload;

import java.util.List;

/**
 * 工作调度器
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-03-08
 */
public interface WorkerScheduling {
    /**
     * 初始化工作节点
     *
     * @param workers
     */
    void initWorker(TaskflowConfig config, List<Worker> workers);

    /**
     * 启动工作节点调度程序
     *
     * @param workerProcess 消费程序(任务拉取成功后执行的程序)
     */
    void start(TaskPollExecutor taskPollExecutor, WorkerProcess workerProcess);

    /**
     * 关闭工作节点调度程序
     *
     * @param timeout 超时时间(单位秒)
     */
    void shutdown(int timeout);

    /**
     * 触发任务更新
     *
     * @param payload
     */
    void triggerTask(SubTaskPayload payload);
}
