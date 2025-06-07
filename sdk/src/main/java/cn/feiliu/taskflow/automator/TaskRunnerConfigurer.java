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

import cn.feiliu.taskflow.automator.scheduling.PollStatus;
import cn.feiliu.taskflow.automator.scheduling.WheelTimerWorkerScheduling;
import cn.feiliu.taskflow.automator.scheduling.WorkerScheduling;
import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.executor.task.Worker;
import com.google.common.base.Preconditions;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static cn.feiliu.common.api.utils.CommonUtils.f;

/**
 * 配置通过注册的{@link Worker}自动轮询和执行任务
 */
public class TaskRunnerConfigurer {
    private static final Logger                                         LOGGER                    = LoggerFactory
                                                                                                      .getLogger(TaskRunnerConfigurer.class);
    private static final String                                         INVALID_THREAD_COUNT      = "Invalid worker thread count specified, use either shared thread pool or config thread count per task";
    private static final String                                         MISSING_TASK_THREAD_COUNT = "Missing task thread count config for %s";

    private final WorkerScheduling                                      workerScheduling          = new WheelTimerWorkerScheduling();
    protected final ApiClient                                           apiClient;
    protected final List<Worker>                                        workers                   = new LinkedList<>();
    private final int                                                   sleepWhenRetry;                                                                                                                    // 重试前休眠时间
    protected final int                                                 updateRetryCount;                                                                                                                  // 更新重试次数
    protected final int                                                 threadCount;                                                                                                                       // 线程数
    protected final int                                                 shutdownGracePeriodSeconds;                                                                                                        // 优雅关闭等待时间(秒)
    protected final String                                              workerNamePrefix;                                                                                                                  // 工作线程名称前缀
    protected final Map<String /*taskType*/, String /*domain*/>       taskToDomain;                                                                                                                      // 任务类型到域的映射
    protected final Map<String /*taskType*/, Integer /*threadCount*/> taskThreadCount;                                                                                                                   // 每个任务类型的线程数配置

    protected final TaskPollExecutor                                    taskPollExecutor;                                                                                                                  // 任务轮询执行器

    /**
     * @see Builder
     * @see TaskRunnerConfigurer#init()
     */
    private TaskRunnerConfigurer(Builder builder) {
        // 只允许使用共享线程池或每个任务独立的线程池
        if (builder.threadCount != -1 && !builder.taskThreadCount.isEmpty()) {
            throw new IllegalArgumentException(INVALID_THREAD_COUNT);
        } else if (!builder.taskThreadCount.isEmpty()) {
            for (Worker worker : builder.workers) {
                if (!builder.taskThreadCount.containsKey(worker.getTaskDefName())) {
                    throw new IllegalArgumentException(f(MISSING_TASK_THREAD_COUNT, worker.getTaskDefName()));
                }
                workers.add(worker);
            }
            this.taskThreadCount = builder.taskThreadCount;
            this.threadCount = -1;
        } else {
            builder.workers.forEach(workers::add);
            this.taskThreadCount = builder.taskThreadCount;
            this.threadCount = (builder.threadCount == -1) ? workers.size() : builder.threadCount;
        }

        this.apiClient = builder.apiClient;
        this.sleepWhenRetry = builder.sleepWhenRetry;
        this.updateRetryCount = builder.updateRetryCount;
        this.workerNamePrefix = builder.workerNamePrefix;
        this.taskToDomain = builder.taskToDomain;
        this.shutdownGracePeriodSeconds = builder.shutdownGracePeriodSeconds;
        this.taskPollExecutor = new TaskPollExecutor(apiClient, threadCount, updateRetryCount, taskToDomain, workerNamePrefix, taskThreadCount);
    }

    /**
     * 用于创建TaskRunnerConfigurer实例的构建器
     */
    public static class Builder {

        private String                                              workerNamePrefix           = "workflow-worker-%d";
        private int                                                 sleepWhenRetry             = 500;
        private int                                                 updateRetryCount           = 3;
        private int                                                 threadCount                = -1;
        private int                                                 shutdownGracePeriodSeconds = 10;
        private final Iterable<Worker>                              workers;
        private final ApiClient                                     apiClient;
        private Map<String /*taskType*/, String /*domain*/>       taskToDomain               = new HashMap<>();
        private Map<String /*taskType*/, Integer /*threadCount*/> taskThreadCount            = new HashMap<>();

        public Builder(ApiClient apiClient, Iterable<Worker> workers) {
            Preconditions.checkNotNull(apiClient, "apiClient cannot be null");
            Preconditions.checkNotNull(workers, "Workers cannot be null");
            this.apiClient = apiClient;
            this.workers = workers;
        }

        /**
         * @param workerNamePrefix 工作线程名称前缀,如果不提供则默认为workflow-worker-
         * @return 返回当前实例
         */
        public Builder withWorkerNamePrefix(String workerNamePrefix) {
            this.workerNamePrefix = workerNamePrefix;
            return this;
        }

        /**
         * @param sleepWhenRetry 任务更新调用失败时,重试操作前线程休眠的毫秒数
         * @return 返回当前实例
         */
        public Builder withSleepWhenRetry(int sleepWhenRetry) {
            this.sleepWhenRetry = sleepWhenRetry;
            return this;
        }

        /**
         * @param updateRetryCount 失败的updateTask操作重试次数
         * @return Builder实例
         * @see #withSleepWhenRetry(int)
         */
        public Builder withUpdateRetryCount(int updateRetryCount) {
            this.updateRetryCount = updateRetryCount;
            return this;
        }

        /**
         * @param threadCount 分配给工作线程的线程数。应至少等于taskWorkers的大小以避免在繁忙系统中出现饥饿
         * @return Builder实例
         */
        public Builder withThreadCount(int threadCount) {
            if (threadCount < 1) {
                throw new IllegalArgumentException("线程数不能小于1");
            }
            this.threadCount = threadCount;
            return this;
        }

        /**
         * @param shutdownGracePeriodSeconds 强制关闭工作线程前的等待秒数
         * @return Builder实例
         */
        public Builder withShutdownGracePeriodSeconds(int shutdownGracePeriodSeconds) {
            if (shutdownGracePeriodSeconds < 1) {
                throw new IllegalArgumentException("优雅关闭等待时间不能小于1秒");
            }
            this.shutdownGracePeriodSeconds = shutdownGracePeriodSeconds;
            return this;
        }

        /**
         * @param taskToDomain 任务类型到域的映射关系
         * @return Builder实例
         */
        public Builder withTaskToDomain(Map<String, String> taskToDomain) {
            this.taskToDomain = taskToDomain;
            return this;
        }

        /**
         * @param taskThreadCount 每个任务类型的线程数配置
         * @return Builder实例
         */
        public Builder withTaskThreadCount(Map<String, Integer> taskThreadCount) {
            this.taskThreadCount = taskThreadCount;
            return this;
        }

        /**
         * 构建TaskRunnerConfigurer实例
         *
         * <p>请参阅{@link TaskRunnerConfigurer#init()}方法。构造函数之后必须调用该方法才能开始轮询。
         */
        public TaskRunnerConfigurer build() {
            return new TaskRunnerConfigurer(this);
        }
    }

    /**
     * @return 共享执行器池的线程数
     */
    public int getThreadCount() {
        return threadCount;
    }

    /**
     * @return 每个任务类型的线程数
     */
    public Map<String, Integer> getTaskThreadCount() {
        return taskThreadCount;
    }

    /**
     * @return 强制关闭工作线程前的等待秒数
     */
    public int getShutdownGracePeriodSeconds() {
        return shutdownGracePeriodSeconds;
    }

    /**
     * @return 从Taskflow服务器收到错误时, 任务更新重试前的休眠毫秒数
     */
    public int getSleepWhenRetry() {
        return sleepWhenRetry;
    }

    /**
     * @return 从Taskflow服务器收到错误时, updateTask应该重试的次数
     */
    public int getUpdateRetryCount() {
        return updateRetryCount;
    }

    /**
     * @return 工作线程名称使用的前缀
     */
    public String getWorkerNamePrefix() {
        return workerNamePrefix;
    }

    /**
     * 必须在调用{@link Builder#build()}方法之后调用
     */
    public synchronized void init() {
        if (workers.isEmpty()) {
            LOGGER.warn("没有工作线程需要启动");
        } else {
            workerScheduling.initWorker(apiClient.getConfig(), workers);
        }
    }

    /**
     * 开启拉取任务并运行
     */
    public void startRunningTasks() {
        workerScheduling.start(taskPollExecutor, new WorkerProcess() {
            @Override
            public PollStatus process(TimerTask timerTask, Worker worker) {
                return taskPollExecutor.fastPollAndExecute(worker);
            }
        });
    }

    /**
     * 在进程终止期间,在应用程序的PreDestroy块中调用此方法,以实现工作线程的优雅关闭
     */
    public void shutdown() {
        workerScheduling.shutdown(shutdownGracePeriodSeconds);
    }

    public WorkerScheduling getWorkerScheduling() {
        return workerScheduling;
    }
}
