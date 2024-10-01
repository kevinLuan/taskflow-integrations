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
package cn.feiliu.taskflow.client.automator.scheduling;

import cn.feiliu.taskflow.common.metadata.tasks.ExecutingTask;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 多任务返回结果
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-03-13
 */
public class MultiTaskResult {
    /*拉取任务执行状态*/
    private final PollExecuteStatus                      status;
    /*子任务调度执行句柄*/
    private final List<CompletableFuture<ExecutingTask>> futures;

    public MultiTaskResult(PollExecuteStatus status, List<CompletableFuture<ExecutingTask>> list) {
        this.status = Objects.requireNonNull(status);
        this.futures = Objects.requireNonNull(list);
    }

    public static MultiTaskResult of(PollExecuteStatus status, List<CompletableFuture<ExecutingTask>> futures) {
        return new MultiTaskResult(status, futures);
    }

    /**
     * 判断是否还有任务
     *
     * @return
     */
    public boolean hasTask() {
        return status == PollExecuteStatus.HAS_TASK;
    }

    /**
     * 获取处理状态
     *
     * @return
     */
    public PollExecuteStatus getStatus() {
        return this.status;
    }

    /**
     * 获取所有的子任务
     *
     * @return
     */
    public List<CompletableFuture<ExecutingTask>> getFutures() {
        return this.futures;
    }

    /**
     * 等待子任务完成
     */
    public void waitTasksDone() {
        if (!isAllDone()) {
            this.futures.forEach(CompletableFuture::join);
        }
    }

    /**
     * 是否全部执行结束
     *
     * @return
     */
    public boolean isAllDone() {
        return futures.stream().allMatch((future) -> future.isDone() || future.isCancelled());
    }

    /**
     * 是否全部执行成功
     *
     * @return
     */
    public boolean isAllSuccessful() {
        return futures.stream().allMatch(future -> future.isDone() && !future.isCancelled() && !future.isCompletedExceptionally());
    }
}
