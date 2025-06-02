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

import cn.feiliu.taskflow.executor.task.Worker;
import com.google.common.collect.Lists;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 基于时间轮算法的工作者调度器
 * 使用Netty的HashedWheelTimer实现定时任务调度
 * 
 * @author SHOUSHEN.LUAN
 * @since 2024-03-12
 */
@Slf4j
public class WheelTimerWorkerScheduling implements WorkerScheduling {
    // 时间轮定时器实例
    final Timer                  timer  = new HashedWheelTimer();
    // 工作者列表
    private List<Worker>         workers;
    // 调度器停止标志
    volatile static boolean      isStop = false;
    // 工作者与其对应定时任务的映射关系
    private Map<Worker, Timeout> workerTaskMap;

    /**
     * 打印所有工作者的名称,用于调试
     */
    private void dumpWorkerName() {
        workers.forEach(worker -> log.info("worker name:{}", worker.getTaskDefName()));
    }

    /**
     * 初始化工作者列表
     * @param list 工作者列表
     */
    @Override
    public void initWorker(List<Worker> list) {
        this.workers = Lists.newArrayList(list);
        this.workerTaskMap = new ConcurrentHashMap<>(list.size());
        dumpWorkerName();
    }

    /**
     * 启动所有工作者的调度
     * @param isWorkerIdle 判断工作者是否空闲的函数
     * @param workerProcess 工作者处理逻辑
     */
    private void startAllWorker(Function<Worker, Boolean> isWorkerIdle, BiConsumer<TimerTask, Worker> workerProcess) {
        workers.forEach(worker -> addIfAbsent(new TimerTask() {
            public void run(Timeout timeout) throws Exception {
                workerTaskMap.remove(worker);
                if (isWorkerIdle.apply(worker)) {
                    workerProcess.accept(this, worker);
                } else {
                    addIfAbsent(this, worker, false);
                }
            }
        }, worker, false));
    }

    /**
     * 启动单任务处理模式
     * @param isWorkerIdle 判断工作者是否空闲的函数
     * @param consumer 任务处理函数
     */
    @Override
    public void start(Function<Worker, Boolean> isWorkerIdle, Consumer<Worker> consumer) {
        startAllWorker(isWorkerIdle, (timerTask, worker) -> {
            try {
                consumer.accept(worker);
            } finally {
                addIfAbsent(timerTask, worker, false);
            }
        });
    }

    /**
     * 启动批量任务处理模式
     * @param isWorkerIdle 判断工作者是否空闲的函数
     * @param workerProcess 批量任务处理函数
     */
    @Override
    public void startBatchTask(Function<Worker, Boolean> isWorkerIdle, Function<Worker, CompletableFuture<MultiTaskResult>> workerProcess) {
        startAllWorker(isWorkerIdle, (timerTask, worker) -> {
            CompletableFuture<MultiTaskResult> future = workerProcess.apply(worker);
            future.whenComplete((r, e) -> {
                if (e == null && r != null && r.isAllSuccessful() && r.hasTask()) {
                    addIfAbsent(timerTask, worker, true);
                } else {
                    addIfAbsent(timerTask, worker, false);
                }
            });
        });
    }

    /**
     * 将任务添加到时间轮定时器中
     * @param timerTask 定时任务
     * @param worker 关联的工作者
     * @param now 是否立即执行
     */
    private void addIfAbsent(TimerTask timerTask, Worker worker, boolean now) {
        if (!isStop) {
            workerTaskMap.computeIfAbsent(worker, k -> {
                if (now) {
                    return timer.newTimeout(timerTask, 1, TimeUnit.MILLISECONDS);
                }
                return timer.newTimeout(timerTask, worker.getPollingInterval(), TimeUnit.MILLISECONDS);
            });
        }
    }

    /**
     * 关闭调度器
     * @param timeout 超时时间
     */
    @Override
    public void shutdown(int timeout) {
        this.isStop = true;
        this.timer.stop();
    }
}
