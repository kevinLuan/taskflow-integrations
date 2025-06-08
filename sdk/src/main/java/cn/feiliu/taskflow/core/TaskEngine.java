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
import cn.feiliu.taskflow.executor.task.WorkerWrapper;
import cn.feiliu.taskflow.utils.TaskflowConfig;
import cn.feiliu.taskflow.ws.AutoReconnectClient;
import cn.feiliu.taskflow.ws.MessageType;
import cn.feiliu.taskflow.ws.WebSocketClient;
import cn.feiliu.taskflow.ws.msg.SubTaskPayload;
import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static cn.feiliu.common.api.utils.CommonUtils.f;

/**
 * taskflow 工作节点执行器
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-06-15
 */
public class TaskEngine {
    private static final Logger          LOGGER          = LoggerFactory.getLogger(TaskEngine.class);
    @Getter
    private ApiClient                    client;

    private TaskRunnerConfigurer         taskRunner;

    protected List<Worker>               workerList      = new ArrayList<>();

    private Map<String, Method>          workerToMethod  = new HashMap<>();

    protected Map<String, WorkerWrapper> workerMapping   = new HashMap<>();

    private Map<String, Object>          workerClassObjs = new HashMap<>();
    private AutoReconnectClient          wcClient;

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
        if (bean instanceof Worker) {
            Worker worker = (Worker) bean;
            client.getTaskHandlerManager().registerTask(worker);
            String name = worker.getTaskDefName();
            workerMapping.put(name, WorkerWrapper.of(worker));
            workerClassObjs.put(name, bean);
            Method method = client.getTaskHandlerManager().getTaskHandler(name).get().getWorkerMethod();
            workerToMethod.put(name, method);
        } else {
            Class<?> clazz = bean.getClass();
            for (Method method : clazz.getMethods()) {
                WorkerTask worker = method.getAnnotation(WorkerTask.class);
                if (worker == null) {
                    continue;
                }
                client.getTaskHandlerManager().registerTask(worker, bean, method);
                workerMapping.put(worker.value(), WorkerWrapper.of(worker));
                workerClassObjs.put(worker.value(), bean);
                workerToMethod.put(worker.value(), method);
            }
        }
    }

    /**
     * 初始化工作任务节点
     */
    private TaskEngine initWorkerTasks() {
        this.initWorkerExecutor();
        if (workerList.isEmpty()) {
            LOGGER.info("No workers to start");
        }
        this.taskRunner = new TaskRunnerConfigurer.Builder(client, workerList)//
            .withWorkerMapping(workerMapping)//
            .build();
        this.taskRunner.init();
        registerAndUpdateTasks();
        return this;
    }

    /**
     * 初始化并运行任务
     */
    public void start() {
        this.initWorkerTasks();
        this.startRunningTasks();
    }

    /**
     * 注册和更新任务定义
     */
    private void registerAndUpdateTasks() {
        if (workerList.size() > 0) {
            Set<String> taskDefNames = getClient().getApis().getTaskDefClient().getTaskNames();
            List<String> missingNames = new ArrayList<>();
            for (Worker worker : this.workerList) {
                if (worker.getTaskDefName().matches("^[a-zA-Z][a-zA-Z0-9_]{0,29}$")) {
                    if (taskDefNames.contains(worker.getTaskDefName())) {
                        if (getClient().getConfig().isUpdateExisting()) {
                            getClient().getApis().getTaskDefClient().updateTaskDef(worker);
                        }
                    } else {
                        if (getClient().getConfig().isAutoRegister()) {
                            getClient().getApis().getTaskDefClient().createTaskDef(worker);
                        } else {
                            missingNames.add(worker.getTaskDefName());
                        }
                    }
                } else {
                    throw new IllegalStateException(f("工作任务名称:'%s'不合法，格式要求：字母开头，限制包含字母数字下划线，最大30字符",
                        worker.getTaskDefName()));
                }
            }
            if (missingNames.size() > 0) {
                String names = String.join(",", missingNames);
                String msg = f("任务 [%s] 未注册，请访问平台注册：%s", names, "https://console.taskflow.cn/taskDef");
                throw new ApiException(msg);
            }
        }
    }

    /**
     * 运行工作任务节点
     */
    private TaskEngine startRunningTasks() {
        this.taskRunner.startRunningTasks();
        if (this.client.isSupportWebSocket()) {
            this.runWebSocket().thenAccept((nil) -> {
                broadcast();
            });
        }
        return this;
    }

    private CompletableFuture<Void> runWebSocket() {
        TaskflowConfig config = getClient().getConfig();
        String wcUrl = config.getWebSocketUrl();
        String userId = WebSocketClient.generateUniqueUserId(config.getKeyId());
        String keyId = config.getKeyId();
        String keySecret = config.getKeySecret();
        wcClient = new AutoReconnectClient(wcUrl, userId, keyId, keySecret, (message) -> {
            Optional<MessageType> optional = MessageType.fromValue(message.getType());
            if (optional.isPresent()) {
                if (optional.get() == MessageType.CONNECTION) {
                    LOGGER.info("✅ 连接建立确认: {}", message.getDescription());
                } else if (optional.get() == MessageType.PONG) {
                    LOGGER.debug("❤️ 收到心跳响应");
                } else if (optional.get() == MessageType.SUB_TASK) {
                    LOGGER.debug("任务通知: `{}`, data: `{}`", message.getDescription(), message.getData());
                    SubTaskPayload payload = message.getData(SubTaskPayload.class);
                    if (payload.getTaskNames().isEmpty()) {
                        this.broadcast();
                    } else {
                        taskRunner.getWorkerScheduling().triggerTask(payload);
                    }
                } else {
                    LOGGER.info("通知 type:`{}`, description:`{}`", message.getType(), message.getDescription());
                }
            } else {
                LOGGER.error("未支持的消息类型:{}", message.getType());
            }
        });
        wcClient.setOnConnectedCallback(() -> {
            LOGGER.info("🎉 WebSocket连接成功建立");
            List<String> names = workerList.stream().map((w) -> w.getTaskDefName()).collect(Collectors.toList());
            wcClient.subTasks(names);
            wcClient.sendPing();
        });
        return wcClient.connect();
    }

    /**
     * 初始化Worker执行器
     */
    protected final void initWorkerExecutor() {
        workerToMethod.forEach(
                (taskName, method) -> {
                    Object obj = workerClassObjs.get(taskName);
                    WorkerWrapper workerWrapper = workerMapping.get(taskName);
                    AnnotatedWorker executor = new AnnotatedWorker(workerWrapper, method, obj);
                    executor.setPollingInterval(workerMapping.get(taskName).pollingInterval());
                    workerList.add(executor);
                });
    }

    /**
     * 获取所有执行Workers
     *
     * @return
     */
    public List<Worker> getWorkers() {
        return Collections.unmodifiableList(workerList);
    }

    @VisibleForTesting
    TaskRunnerConfigurer getTaskRunner() {
        return taskRunner;
    }

    /**
     * 广播全部任务更新一次
     */
    private void broadcast() {
        for (Worker worker : workerList) {
            SubTaskPayload payload = SubTaskPayload.createSimple(worker.getTaskDefName());
            taskRunner.getWorkerScheduling().triggerTask(payload);
        }
    }
}
