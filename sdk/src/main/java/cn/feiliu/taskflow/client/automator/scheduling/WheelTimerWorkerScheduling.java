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
 * @author SHOUSHEN.LUAN
 * @since 2024-03-12
 */
@Slf4j
public class WheelTimerWorkerScheduling implements WorkerScheduling {
    final Timer                  timer  = new HashedWheelTimer();
    private List<Worker>         workers;
    volatile static boolean      isStop = false;
    private Map<Worker, Timeout> workerTaskMap;

    private void dumpWorkerName() {
        workers.forEach(worker -> log.info("worker name:{}", worker.getTaskDefName()));
    }

    @Override
    public void initWorker(List<Worker> list) {
        this.workers = Lists.newArrayList(list);
        this.workerTaskMap = new ConcurrentHashMap<>(list.size());
        dumpWorkerName();
    }

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
     * 添加任务到延迟队列
     *
     * @param timerTask
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

    @Override
    public void shutdown(int timeout) {
        this.isStop = true;
        this.timer.stop();
    }
}
