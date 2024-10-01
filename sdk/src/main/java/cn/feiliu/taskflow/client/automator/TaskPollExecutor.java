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
package cn.feiliu.taskflow.client.automator;

import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.client.automator.scheduling.MultiTaskResult;
import cn.feiliu.taskflow.client.automator.scheduling.PollExecuteStatus;
import cn.feiliu.taskflow.client.spi.DiscoveryService;
import cn.feiliu.taskflow.client.telemetry.MetricsContainer;
import cn.feiliu.taskflow.common.metadata.tasks.ExecutingTask;
import cn.feiliu.taskflow.common.metadata.tasks.TaskExecResult;
import cn.feiliu.taskflow.common.metadata.tasks.TaskLog;
import cn.feiliu.taskflow.common.utils.TaskflowUtils;
import cn.feiliu.taskflow.sdk.config.WorkerPropertyManager;
import cn.feiliu.taskflow.sdk.worker.Worker;
import com.google.common.base.Stopwatch;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.Spectator;
import com.netflix.spectator.api.Timer;
import com.netflix.spectator.api.patterns.ThreadPoolMonitor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Manages the threadpool used by the workers for execution and server communication (polling and
 * task update).
 */
class TaskPollExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskPollExecutor.class);
    private static final Registry REGISTRY = Spectator.globalRegistry();
    protected final DiscoveryService discoveryService;
    protected final ApiClient apiClient;
    private final int updateRetryCount;
    protected final ThreadPoolExecutor executorService;
    private final Map<String, PollingSemaphore> pollingSemaphoreMap;
    protected final Map<String /*taskType*/, String /*domain*/> taskToDomain;
    protected static final String ALL_WORKERS = "all";

    TaskPollExecutor(
            DiscoveryService discoveryService,
            ApiClient apiClient,
            int threadCount,
            int updateRetryCount,
            Map<String, String> taskToDomain,
            String workerNamePrefix,
            Map<String, Integer> taskThreadCount) {
        this.discoveryService = discoveryService;
        this.apiClient = apiClient;
        this.updateRetryCount = updateRetryCount;
        this.taskToDomain = taskToDomain;

        this.pollingSemaphoreMap = new HashMap<>();
        int totalThreadCount = 0;
        if (!taskThreadCount.isEmpty()) {
            for (Map.Entry<String, Integer> entry : taskThreadCount.entrySet()) {
                String taskType = entry.getKey();
                int count = entry.getValue();
                totalThreadCount += count;
                pollingSemaphoreMap.put(taskType, new PollingSemaphore(count));
            }
        } else {
            totalThreadCount = threadCount;
            // shared poll for all workers
            pollingSemaphoreMap.put(ALL_WORKERS, new PollingSemaphore(threadCount));
        }

        LOGGER.info("Initialized the TaskPollExecutor with {} threads", totalThreadCount);
        this.executorService = new ThreadPoolExecutor(0, totalThreadCount, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new BasicThreadFactory.Builder().namingPattern(workerNamePrefix).uncaughtExceptionHandler(uncaughtExceptionHandler).build(),
                new ThreadPoolExecutor.CallerRunsPolicy());
        ThreadPoolMonitor.attach(REGISTRY, executorService, workerNamePrefix);
    }

    /**
     * 停止任务执行器
     *
     * @param timeout
     */
    public void shutdown(int timeout) {
        try {
            executorService.shutdown();
            if (executorService.awaitTermination(timeout, TimeUnit.SECONDS)) {
                LOGGER.debug("tasks completed, shutting down");
            } else {
                LOGGER.warn(String.format("forcing shutdown after waiting for %s second", timeout));
                executorService.shutdownNow();
            }
        } catch (InterruptedException ie) {
            LOGGER.warn("shutdown interrupted, invoking shutdownNow");
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler = (thread, error) -> {
        // JVM may be in unstable state, try to send metrics then exit
        MetricsContainer.incrementUncaughtExceptionCount();
        LOGGER.error("Uncaught exception. Thread {} will exit now", thread, error);
    };

    private void doExecuteTask(Worker worker, ExecutingTask task) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        TaskExecResult result = null;
        try {
            LOGGER.debug("Executing taskId: {} of type: {}", task.getTaskId(), task.getTaskDefName());
            result = worker.execute(task);
            result.setWorkflowInstanceId(task.getWorkflowInstanceId());
            result.setTaskId(task.getTaskId());
            result.setWorkerId(worker.getIdentity());
        } catch (Throwable e) {
            LOGGER.error("Unable to execute taskId: {} of type: {} ,error:{}", task.getTaskId(), task.getTaskDefName(), e);
            if (result == null) {
                task.setStatus(ExecutingTask.Status.FAILED);
                result = new TaskExecResult(task);
            }
            handleException(e, result, worker, task);
        } finally {
            stopwatch.stop();
            MetricsContainer.getExecutionTimer(worker.getTaskDefName())
                    .record(stopwatch.elapsed(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        }
        updateTaskResult(updateRetryCount, task, result, worker);
    }

    private void finalizeTask(ExecutingTask task, Throwable throwable) {
        if (throwable != null) {
            LOGGER.error(
                    "Error processing task: {} of type: {}",
                    task.getTaskId(),
                    task.getTaskType(),
                    throwable);
            MetricsContainer.incrementTaskExecutionErrorCount(task.getTaskType(), throwable);
        } else {
            LOGGER.debug(
                    "Task:{} of type:{} finished processing with status:{}",
                    task.getTaskId(),
                    task.getTaskDefName(),
                    task.getStatus());
        }
    }

    private void updateTaskResult(int count, ExecutingTask task, TaskExecResult result, Worker worker) {
        Runnable runnable = () -> {
            if (apiClient.isUseGRPC()) {
                List<Future<?>> futures = new ArrayList<>();
                futures.add(apiClient.getGrpcApi().asyncUpdateTask(result));
                for (TaskLog taskLog : result.getLogs()) {
                    if (StringUtils.isNotBlank(taskLog.getLog())) {
                        futures.add(apiClient.getGrpcApi().addLog(taskLog));
                    }
                }
                TaskflowUtils.blockedWait(futures, 30_000);
            } else {
                apiClient.getTaskClient().updateTask(result);
            }
        };
        try {
            TaskflowUtils.retryOperation(runnable, count, "updateTask");
        } catch (Exception e) {
            worker.onErrorUpdate(task);
            MetricsContainer.incrementTaskUpdateErrorCount(worker.getTaskDefName(), e);
            LOGGER.error("Failed to update result: {} for task: {} in worker: {}", result.toString(), task.getTaskDefName(), worker.getIdentity(), e);
        }
    }

    private void handleException(Throwable t, TaskExecResult result, Worker worker, ExecutingTask task) {
        LOGGER.error(String.format("Error while executing task %s", task.toString()), t);
        MetricsContainer.incrementTaskExecutionErrorCount(worker.getTaskDefName(), t);
        result.setStatus(TaskExecResult.Status.FAILED);
        result.setReasonForIncompletion("Error while executing the task: " + t);
        result.log(TaskflowUtils.dumpFullStackTrace(t));
        updateTaskResult(updateRetryCount, task, result, worker);
    }

    private PollingSemaphore getPollingSemaphore(Worker worker) {
        PollingSemaphore semaphore;
        if (pollingSemaphoreMap.containsKey(worker.getTaskDefName())) {
            semaphore = pollingSemaphoreMap.get(worker.getTaskDefName());
        } else {
            semaphore = pollingSemaphoreMap.get(ALL_WORKERS);
        }
        return Objects.requireNonNull(semaphore, "No polling semaphore found for task type: " + worker.getTaskDefName());
    }

    /**
     * 获取可用线程数
     *
     * @param worker
     * @return
     */
    private int getAvailableThreads(Worker worker) {
        PollingSemaphore pollingSemaphore = getPollingSemaphore(worker);
        return pollingSemaphore.availableThreads();
    }

    /**
     * 工作节点繁忙状态
     *
     * @param worker
     * @return
     */
    boolean isBusy(Worker worker) {
        return getAvailableThreads(worker) <= 0;
    }

    boolean isActive(Worker worker) {
        Boolean discoveryOverride = WorkerPropertyManager.getPollOutOfDiscovery(worker.getTaskDefName(),ALL_WORKERS,false);
        if (discoveryService != null && !discoveryService.getStatus().isUp() && !discoveryOverride) {
            LOGGER.debug("Instance is NOT UP in discovery - will not poll");
            return false;
        }
        if (worker.paused()) {
            MetricsContainer.incrementTaskPausedCount(worker.getTaskDefName());
            LOGGER.debug("Worker {} has been paused. Not polling anymore!", worker.getClass());
            return false;
        }
        return true;
    }

    /**
     * 批量拉取任务并执行
     *
     * @param worker
     * @return
     */
    CompletableFuture<MultiTaskResult> fastPollAndExecute(Worker worker) {
        Supplier<MultiTaskResult> supplier = () -> {
            if (isActive(worker)) {
                String taskType = worker.getTaskDefName();
                PollingSemaphore pollingSemaphore = getPollingSemaphore(worker);
                String domain = WorkerPropertyManager.getDomainWithFallback(taskType, ALL_WORKERS, taskToDomain.get(taskType));
                Optional<Integer> availablePermitsOpt = pollingSemaphore.tryAcquireAvailablePermits();
                if (availablePermitsOpt.isPresent()) {
                    final int maxAmount = availablePermitsOpt.get();
                    List<ExecutingTask> tasks;
                    try {
                        tasks = getBatchTasks(worker, domain, maxAmount);
                        if (tasks.isEmpty()) {
                            pollingSemaphore.complete(maxAmount);
                            return MultiTaskResult.of(PollExecuteStatus.NO_TASK, Collections.emptyList());
                        }
                    } catch (Exception e) {
                        LOGGER.error("Error when polling for tasks", e);
                        pollingSemaphore.complete(maxAmount);
                        return MultiTaskResult.of(PollExecuteStatus.FAIL, Collections.emptyList());
                    }
                    if (maxAmount > tasks.size()) {
                        pollingSemaphore.complete(maxAmount - tasks.size());
                    }
                    List<CompletableFuture<ExecutingTask>> futures = submitTasks(worker, tasks, domain, pollingSemaphore);
                    PollExecuteStatus status = maxAmount > tasks.size() ? PollExecuteStatus.NO_TASK : PollExecuteStatus.HAS_TASK;
                    return MultiTaskResult.of(status, futures);
                } else {
                    return MultiTaskResult.of(PollExecuteStatus.NO_TASK, Collections.emptyList());
                }
            } else {
                return MultiTaskResult.of(PollExecuteStatus.FAIL, Collections.emptyList());
            }
        };
        return CompletableFuture.supplyAsync(supplier);
    }

    /**
     * 批量获取任务
     *
     * @param worker
     * @param domain
     * @param maxAmount
     * @return
     * @throws Exception
     */
    private List<ExecutingTask> getBatchTasks(Worker worker, String domain, int maxAmount) throws Exception {
        LOGGER.debug("Polling tasks of type: {}", worker.getTaskDefName());
        String workerId = worker.getIdentity();
        int timeout = TaskflowUtils.getReasonableTimeout(worker);
        String taskName = worker.getTaskDefName();
        Timer timer = MetricsContainer.getBatchPollTimer(worker.getTaskDefName());
        if (apiClient.isUseGRPC()) {
            return timer.record(() -> {
                return apiClient.getGrpcApi().batchPollTask(taskName, workerId, domain, maxAmount, timeout);
            });
        } else {
            return timer.record(() ->
                    apiClient.getTaskClient().batchPollTasksInDomain(taskName, domain, workerId, maxAmount, timeout));
        }
    }

    private List<CompletableFuture<ExecutingTask>> submitTasks(Worker worker, List<ExecutingTask> tasks, String domain, PollingSemaphore pollingSemaphore) {
        List<CompletableFuture<ExecutingTask>> futures = new ArrayList<>();
        String taskType = worker.getTaskDefName();
        for (ExecutingTask task : tasks) {
            try {
                if (Objects.nonNull(task) && StringUtils.isNotBlank(task.getTaskId())) {
                    MetricsContainer.incrementTaskPollCount(taskType, 1);
                    LOGGER.debug("Polled task: {} of type: {}, from worker: {}", task.getTaskId(), taskType, worker.getIdentity());
                    futures.add(executingTask(worker, task, pollingSemaphore));
                } else {
                    // no task was returned in the poll, release the permit
                    pollingSemaphore.complete();
                    futures.add(CompletableFuture.completedFuture(null));
                }
            } catch (Throwable e) {
                // release the permit if exception is thrown during polling, because the thread would not be busy
                pollingSemaphore.complete();
                MetricsContainer.incrementTaskPollErrorCount(worker.getTaskDefName(), e);
                LOGGER.error("Error when polling for tasks", e);
                futures.add(CompletableFuture.failedFuture(e));
            }
        }
        return futures;
    }

    private CompletableFuture<ExecutingTask> executingTask(Worker worker, ExecutingTask task, PollingSemaphore pollingSemaphore) {
        CompletableFuture<ExecutingTask> future = CompletableFuture.supplyAsync(() -> {
            try {
                doExecuteTask(worker, task);
            } catch (Throwable t) {
                task.setStatus(ExecutingTask.Status.FAILED);
                TaskExecResult result = new TaskExecResult(task);
                handleException(t, result, worker, task);
            } finally {
                pollingSemaphore.complete();
            }
            return task;
        }, executorService);
        return future.whenComplete(this::finalizeTask);
    }
}
