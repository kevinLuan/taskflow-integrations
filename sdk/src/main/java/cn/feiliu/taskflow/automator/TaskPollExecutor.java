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
package cn.feiliu.taskflow.automator;

import cn.feiliu.common.api.utils.CommonUtils;
import cn.feiliu.taskflow.automator.scheduling.MultiTaskResult;
import cn.feiliu.taskflow.automator.scheduling.PollExecuteStatus;
import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.client.TaskClient;
import cn.feiliu.taskflow.common.dto.tasks.ExecutingTask;
import cn.feiliu.taskflow.common.dto.tasks.TaskExecResult;
import cn.feiliu.taskflow.common.enums.TaskStatus;
import cn.feiliu.taskflow.common.enums.TaskUpdateStatus;
import cn.feiliu.taskflow.executor.task.Worker;
import com.google.common.base.Stopwatch;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 管理工作节点用于执行任务和服务器通信(轮询和任务更新)的线程池
 */
class TaskPollExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskPollExecutor.class);
    // API客户端
    protected final ApiClient apiClient;
    // 更新重试次数
    private final int updateRetryCount;
    // 执行服务线程池
    protected final ThreadPoolExecutor executorService;
    // 轮询信号量映射表
    private final Map<String, PollingSemaphore> pollingSemaphoreMap;
    // 任务类型到域的映射
    protected final Map<String /*任务类型*/, String /*domain*/> taskToDomain;
    // 所有工作节点的标识
    protected static final String ALL_WORKERS = "all";
    @SuppressWarnings("FieldCanBeLocal")
    // 未捕获异常处理器
    private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler = (thread, error) -> {
        // JVM可能处于不稳定状态,尝试发送指标然后退出
        LOGGER.error("Uncaught exception. Thread {} will exit now", thread, error);
    };

    /**
     * 构造函数
     *
     * @param apiClient        API客户端
     * @param threadCount      线程数
     * @param updateRetryCount 更新重试次数
     * @param taskToDomain     任务类型到域的映射
     * @param workerNamePrefix 工作线程名称前缀
     * @param taskThreadCount  每个任务类型的线程数配置
     */
    TaskPollExecutor(
            ApiClient apiClient,
            int threadCount,
            int updateRetryCount,
            Map<String, String> taskToDomain,
            String workerNamePrefix,
            Map<String, Integer> taskThreadCount) {
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
            // 所有工作节点共享的轮询信号量
            pollingSemaphoreMap.put(ALL_WORKERS, new PollingSemaphore(threadCount));
        }

        LOGGER.info("Initialized the TaskPollExecutor with {} threads", totalThreadCount);
        this.executorService = new ThreadPoolExecutor(0, totalThreadCount, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new BasicThreadFactory.Builder().namingPattern(workerNamePrefix).uncaughtExceptionHandler(uncaughtExceptionHandler).build(),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * 停止任务执行器
     *
     * @param timeout 超时时间(秒)
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

    /**
     * 执行具体任务
     *
     * @param worker 工作节点
     * @param task   待执行的任务
     */
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
                task.setStatus(TaskStatus.FAILED);
                result = new TaskExecResult(task);
            }
            handleException(e, result, worker, task);
        } finally {
            stopwatch.stop();
        }
        updateTaskResult(updateRetryCount, task, result, worker);
    }

    /**
     * 完成任务的最终处理
     *
     * @param task      执行的任务
     * @param throwable 执行过程中的异常(如果有)
     */
    private void finalizeTask(ExecutingTask task, Throwable throwable) {
        if (throwable != null) {
            LOGGER.error(
                    "Error processing task: {} of type: {}",
                    task.getTaskId(),
                    task.getTaskType(),
                    throwable);
        } else {
            LOGGER.debug(
                    "Task:{} of type:{} finished processing with status:{}",
                    task.getTaskId(),
                    task.getTaskDefName(),
                    task.getStatus());
        }
    }

    /**
     * 更新任务执行结果
     *
     * @param count  重试次数
     * @param task   执行的任务
     * @param result 执行结果
     * @param worker 工作节点
     */
    private void updateTaskResult(int count, ExecutingTask task, TaskExecResult result, Worker worker) {
        Runnable runnable = () -> {
            TaskClient taskClient = apiClient.getApis().getTaskClient();
            taskClient.updateTask(result);
        };
        try {
            CommonUtils.retryOperation(runnable, count, "updateTask");
        } catch (Exception e) {
            worker.onErrorUpdate(task);
            LOGGER.error("Failed to update result: {} for task: {} in worker: {}", result.toString(), task.getTaskDefName(), worker.getIdentity(), e);
        }
    }

    /**
     * 处理任务执行过程中的异常
     *
     * @param t      异常
     * @param result 执行结果
     * @param worker 工作节点
     * @param task   执行的任务
     */
    private void handleException(Throwable t, TaskExecResult result, Worker worker, ExecutingTask task) {
        LOGGER.error(String.format("Error while executing task %s", task.toString()), t);
        result.setStatus(TaskUpdateStatus.FAILED);
        result.setReasonForIncompletion("Error while executing the task: " + t);
        result.log(CommonUtils.dumpFullStackTrace(t));
        updateTaskResult(updateRetryCount, task, result, worker);
    }

    /**
     * 获取工作节点对应的轮询信号量
     *
     * @param worker 工作节点
     * @return 轮询信号量
     */
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
     * @param worker 工作节点
     * @return 可用线程数
     */
    private int getAvailableThreads(Worker worker) {
        PollingSemaphore pollingSemaphore = getPollingSemaphore(worker);
        return pollingSemaphore.availableThreads();
    }

    /**
     * 判断工作节点是否处于繁忙状态
     *
     * @param worker 工作节点
     * @return true表示繁忙, false表示空闲
     */
    boolean isBusy(Worker worker) {
        return getAvailableThreads(worker) <= 0;
    }

    /**
     * 批量拉取任务并执行
     *
     * @param worker 工作节点
     * @return 多任务执行结果的Future
     */
    CompletableFuture<MultiTaskResult> fastPollAndExecute(Worker worker) {
        Supplier<MultiTaskResult> supplier = () -> {
            String taskType = worker.getTaskDefName();
            PollingSemaphore pollingSemaphore = getPollingSemaphore(worker);
            String domain = taskToDomain.get(taskType);
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
        };
        return CompletableFuture.supplyAsync(supplier);
    }

    /**
     * 批量获取任务
     *
     * @param worker    工作节点
     * @param domain    域
     * @param maxAmount 最大获取数量
     * @return 任务列表
     * @throws Exception 获取失败时抛出异常
     */
    private List<ExecutingTask> getBatchTasks(Worker worker, String domain, int maxAmount) throws Exception {
        LOGGER.info("Polling tasks of type: {}", worker.getTaskDefName());
        String workerId = worker.getIdentity();
        int timeout = 100;
        String taskName = worker.getTaskDefName();
        TaskClient taskClient = apiClient.getApis().getTaskClient();
        return taskClient.batchPollTasksInDomain(taskName, domain, workerId, maxAmount, timeout);
    }

    /**
     * 提交任务到线程池执行
     *
     * @param worker           工作节点
     * @param tasks            任务列表
     * @param domain           域
     * @param pollingSemaphore 轮询信号量
     * @return 任务执行Future列表
     */
    private List<CompletableFuture<ExecutingTask>> submitTasks(Worker worker, List<ExecutingTask> tasks, String domain, PollingSemaphore pollingSemaphore) {
        List<CompletableFuture<ExecutingTask>> futures = new ArrayList<>();
        String taskType = worker.getTaskDefName();
        for (ExecutingTask task : tasks) {
            try {
                if (Objects.nonNull(task) && StringUtils.isNotBlank(task.getTaskId())) {
                    LOGGER.info("Polled task: {} of type: {}, from worker: {}", task.getTaskId(), taskType, worker.getIdentity());
                    futures.add(executingTask(worker, task, pollingSemaphore));
                } else {
                    // 没有获取到任务,释放许可
                    pollingSemaphore.complete();
                    futures.add(CompletableFuture.completedFuture(null));
                }
            } finally {
                pollingSemaphore.complete();
            }
        }
        return futures;
    }

    /**
     * 执行单个任务
     *
     * @param worker           工作节点
     * @param task             待执行的任务
     * @param pollingSemaphore 轮询信号量
     * @return 任务执行Future
     */
    private CompletableFuture<ExecutingTask> executingTask(Worker worker, ExecutingTask task, PollingSemaphore pollingSemaphore) {
        CompletableFuture<ExecutingTask> future = CompletableFuture.supplyAsync(() -> {
            try {
                doExecuteTask(worker, task);
            } catch (Throwable t) {
                task.setStatus(TaskStatus.FAILED);
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
