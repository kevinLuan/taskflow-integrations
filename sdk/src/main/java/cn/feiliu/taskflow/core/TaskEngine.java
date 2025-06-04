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
package cn.feiliu.taskflow.core;

import cn.feiliu.taskflow.annotations.WorkerTask;
import cn.feiliu.taskflow.automator.TaskRunnerConfigurer;
import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.common.exceptions.ApiException;
import cn.feiliu.taskflow.executor.task.AnnotatedWorker;
import cn.feiliu.taskflow.executor.task.Worker;
import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

import static cn.feiliu.common.api.utils.CommonUtils.f;

/**
 * taskflow 工作节点执行器
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-06-15
 */
public class TaskEngine {
    private static final Logger    LOGGER                  = LoggerFactory.getLogger(TaskEngine.class);
    @Getter
    private ApiClient              client;

    private TaskRunnerConfigurer   taskRunner;

    protected List<Worker>         workers                 = new ArrayList<>();

    private Map<String, Method>    workerToMethod          = new HashMap<>();

    protected Map<String, Integer> workerToThreadCount     = new HashMap<>();

    private Map<String, Integer>   workerToPollingInterval = new HashMap<>();

    protected Map<String, String>  workerDomains           = new HashMap<>();

    private Map<String, Object>    workerClassObjs         = new HashMap<>();

    public TaskEngine(ApiClient client) {
        this.client = client;
    }

    /**
     * Shuts down the workers
     */
    public void shutdown() {
        if (taskRunner != null) {
            taskRunner.shutdown();
        }
    }

    /**
     * Register the worker implementation
     *
     * @param workers list of worker implementation
     */
    public TaskEngine addWorkers(Object... workers) {
        for (Object worker : workers) {
            try {
                addWorker(worker);
            } catch (Throwable t) {
                // trace because many classes won't have a default no-args
                // constructor and will fail
                LOGGER.trace("Caught exception while loading and scanning class {}", t.getMessage());
            }
        }
        return this;
    }

    /**
     * Register the worker implementation
     *
     * @param basePackage list of packages - comma separated - to scan for annotated worker implementation
     */
    public TaskEngine addWorkers(String basePackage) {
        ScanClasses.scan(basePackage).forEach((worker) -> {
            addWorker(worker);
        });
        return this;
    }

    /**
     * 注册Bean 实例下定义的Workers
     *
     * @param bean
     */
    private void addWorker(Object bean) {
        Class<?> clazz = bean.getClass();
        for (Method method : clazz.getMethods()) {
            WorkerTask annotation = method.getAnnotation(WorkerTask.class);
            if (annotation == null) {
                continue;
            }
            addMethod(annotation, method, bean);
        }
    }

    private void addMethod(WorkerTask worker, Method method, Object bean) {
        client.getTaskHandlerManager().registerTask(worker, bean, method);
        String name = worker.value();
        workerToThreadCount.put(name, Math.max(worker.threadCount(), 1));
        workerToPollingInterval.put(name, Math.max(worker.pollingInterval(), 100));
        workerDomains.put(name, worker.domain());
        workerClassObjs.put(name, bean);
        workerToMethod.put(name, method);
    }

    /**
     * 初始化工作任务节点
     */
    public final TaskEngine initWorkerTasks() {
        this.initWorkerExecutor();
        if (workers.isEmpty()) {
            LOGGER.warn("No workers to start");
            return this;
        }
        LOGGER.info("Starting workers with threadCount {}", workerToThreadCount);
        LOGGER.info("Worker domains {}", workerDomains);
        this.taskRunner = new TaskRunnerConfigurer.Builder(client, workers)//
            .withTaskThreadCount(workerToThreadCount)//
            .withTaskToDomain(workerDomains)//
            .build();
        this.taskRunner.init();
        if (workers.size() > 0) {
            Set<String> taskDefNames = getClient().getApis().getTaskDefClient().getTaskNames();
            List<String> workerNames = new ArrayList<>();
            for (Worker worker : this.workers) {
                if (worker.getTaskDefName().matches("^[a-zA-Z][a-zA-Z0-9_]{0,29}$")) {
                    if (!taskDefNames.contains(worker.getTaskDefName())) {
                        if (getClient().isAutoRegisterTask()) {
                            getClient().getApis().getTaskDefClient().createTaskDef(worker.getTaskDefName());
                        } else {
                            workerNames.add(worker.getTaskDefName());
                        }
                    }
                } else {
                    throw new IllegalStateException(f("工作任务名称:'%s'不合法，格式要求：字母开头，限制包含字母数字下划线，最大30字符",
                        worker.getTaskDefName()));
                }
            }
            if (workerNames.size() > 0) {
                String names = String.join(",", workerNames);
                String msg = f("任务 [%s] 未注册，请访问平台注册：%s", names, "https://console.taskflow.cn/taskDef");
                throw new ApiException(msg);
            }
        }
        return this;
    }

    /**
     * 运行工作任务节点
     */
    public TaskEngine startRunningTasks() {
        this.taskRunner.startRunningTasks();
        return this;
    }

    /**
     * 初始化Worker执行器
     */
    protected final void initWorkerExecutor() {
        workerToMethod.forEach(
                (taskName, method) -> {
                    Object obj = workerClassObjs.get(taskName);
                    AnnotatedWorker executor = new AnnotatedWorker(taskName, method, obj);
                    executor.setPollingInterval(workerToPollingInterval.get(taskName));
                    workers.add(executor);
                });
    }

    /**
     * 获取所有执行Workers
     *
     * @return
     */
    public List<Worker> getWorkers() {
        return Collections.unmodifiableList(workers);
    }

    @VisibleForTesting
    TaskRunnerConfigurer getTaskRunner() {
        return taskRunner;
    }

}
