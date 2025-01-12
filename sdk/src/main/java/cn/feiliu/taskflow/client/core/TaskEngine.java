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
package cn.feiliu.taskflow.client.core;

import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.client.TaskDefClient;
import cn.feiliu.taskflow.client.api.ITaskDefClient;
import cn.feiliu.taskflow.client.automator.TaskRunnerConfigurer;
import cn.feiliu.taskflow.core.executor.task.AnnotatedWorker;
import cn.feiliu.taskflow.core.executor.task.Worker;
import cn.feiliu.taskflow.core.task.WorkerTask;
import cn.feiliu.taskflow.dto.tasks.TaskDefinition;
import cn.feiliu.taskflow.exceptions.ApiException;
import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

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
    @Getter
    private final ITaskDefClient   taskDefClient;

    private TaskRunnerConfigurer   taskRunner;

    protected List<Worker>         workers                 = new ArrayList<>();

    private Map<String, Method>    workerToMethod          = new HashMap<>();

    protected Map<String, Integer> workerToThreadCount     = new HashMap<>();

    private Map<String, Integer>   workerToPollingInterval = new HashMap<>();

    protected Map<String, String>  workerDomains           = new HashMap<>();

    private Map<String, Object>    workerClassObjs         = new HashMap<>();

    public TaskEngine(ApiClient client) {
        this.client = client;
        this.taskDefClient = new TaskDefClient(client);
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

    /**
     * 获取任务定义列表
     *
     * @return
     */
    public List<TaskDefinition> getTaskDefs() {
        return taskDefClient.getTaskDefs();
    }

    /**
     * 注册任务定义(创建&发布)
     * @param tasks
     */
    public void registerTaskDefs(List<TaskDefinition> tasks) {
        taskDefClient.registerTaskDefs(tasks);
    }

    /**
     * 获取任务定义
     *
     * @param taskName
     * @return
     */
    public Optional<TaskDefinition> getTaskDef(String taskName) {
        try {
            TaskDefinition taskDef = taskDefClient.getTaskDef(taskName);
            return Optional.ofNullable(taskDef);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                return Optional.empty();
            } else {
                throw e;
            }
        }
    }

    /**
     * 创建任务定义
     *
     * @param taskDef
     * @return
     */
    public boolean createIfAbsent(TaskDefinition taskDef) {
        return taskDefClient.createIfAbsent(taskDef);
    }

    /**
     * 删除任务定义
     * @param taskName
     * @return
     */
    public boolean deleteTaskDef(String taskName) {
        Objects.requireNonNull(taskName, "taskName must not be null");
        return taskDefClient.deleteTaskDef(taskName);
    }

    /**
     * 更新任务定义
     * @param taskDefinition
     * @return
     */
    public boolean updateTaskDef(TaskDefinition taskDefinition) {
        return taskDefClient.updateTaskDef(taskDefinition);
    }

    /**
     * 发布任务定义
     * @param taskName
     * @return
     */
    public boolean publishTaskDef(String taskName) {
        return taskDefClient.publishTaskDef(taskName);
    }
}
