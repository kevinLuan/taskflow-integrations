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
package cn.feiliu.taskflow.client.automator.scheduling;

import cn.feiliu.taskflow.sdk.worker.Worker;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

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
    void initWorker(List<Worker> workers);

    /**
     * 启动工作节点调度程序
     *
     * @param isWorkerIdle     检查工作节点繁忙情况
     * @param workerProcess 消费程序(任务拉取成功后执行的程序)
     */
    void start(Function<Worker, Boolean> isWorkerIdle, Consumer<Worker> workerProcess);

    /**
     * 启动工作节点调度程序
     *
     * @param isWorkerIdle     检查工作节点繁忙情况
     * @param workerProcess 消费程序(任务拉取成功后执行的程序)
     */
    void startBatchTask(Function<Worker, Boolean> isWorkerIdle,
                        Function<Worker, CompletableFuture<MultiTaskResult>> workerProcess);

    /**
     * 关闭工作节点调度程序
     *
     * @param timeout 超时时间(单位秒)
     */
    void shutdown(int timeout);
}
