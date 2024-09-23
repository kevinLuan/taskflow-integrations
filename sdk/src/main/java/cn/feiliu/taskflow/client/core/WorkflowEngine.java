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
package cn.feiliu.taskflow.client.core;

import cn.feiliu.taskflow.client.*;
import cn.feiliu.taskflow.common.metadata.workflow.FlowTask;
import cn.feiliu.taskflow.common.model.WorkflowRun;
import cn.feiliu.taskflow.common.utils.Validator;
import cn.feiliu.taskflow.common.metadata.tasks.TaskDefinition;
import cn.feiliu.taskflow.common.metadata.tasks.TaskType;
import cn.feiliu.taskflow.common.metadata.workflow.StartWorkflowRequest;
import cn.feiliu.taskflow.common.metadata.workflow.WorkflowDefinition;
import cn.feiliu.taskflow.sdk.workflow.def.tasks.*;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 工作流执行器
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-06-15
 */
public class WorkflowEngine {
    private static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

    @Getter
    private ApiClient           apiClient;
    @Getter
    private WorkflowDefClient   workflowDefClient;

    static {
        initTaskImplementations();
    }

    public static void initTaskImplementations() {
        TaskRegistry.register(TaskType.DO_WHILE.name(), DoWhile.class);
        TaskRegistry.register(TaskType.DYNAMIC.name(), Dynamic.class);
        TaskRegistry.register(TaskType.FORK_JOIN_DYNAMIC.name(), DynamicFork.class);
        TaskRegistry.register(TaskType.FORK_JOIN.name(), ForkJoin.class);
        TaskRegistry.register(TaskType.HTTP.name(), Http.class);
        TaskRegistry.register(TaskType.INLINE.name(), Javascript.class);
        TaskRegistry.register(TaskType.JOIN.name(), Join.class);
        TaskRegistry.register(TaskType.JSON_JQ_TRANSFORM.name(), JQ.class);
        TaskRegistry.register(TaskType.SET_VARIABLE.name(), SetVariable.class);
        TaskRegistry.register(TaskType.SIMPLE.name(), WorkTask.class);
        TaskRegistry.register(TaskType.SUB_WORKFLOW.name(), SubWorkflow.class);
        TaskRegistry.register(TaskType.SWITCH.name(), Switch.class);
        TaskRegistry.register(TaskType.TERMINATE.name(), Terminate.class);
        TaskRegistry.register(TaskType.WAIT.name(), Wait.class);
        TaskRegistry.register(TaskType.EVENT.name(), Event.class);
        TaskRegistry.register(TaskType.FOR_EACH.name(), For.class);
        TaskRegistry.register(TaskType.FORK_FOR_EACH.name(), ForkFor.class);
    }

    public WorkflowEngine(String apiServerURL, String keyId, String keySecret) {
        this(new ApiClient(apiServerURL, keyId, keySecret));
    }

    public WorkflowEngine(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.workflowDefClient = new WorkflowDefClient(apiClient);
    }

    /**
     * 执行工作流
     *
     * @param name    已注册的工作流名称
     * @param version 版本号
     * @param input   输入参数
     * @return
     */
    @SneakyThrows
    public String start(String name, Integer version, Map<String, Object> input) {
        if (!Validator.isValidWorkflowDefName(name)) {
            throw new IllegalArgumentException("Invalid workflowDef name");
        }
        if (version < 1) {
            throw new IllegalArgumentException("Invalid workflow version");
        }
        Objects.requireNonNull(input, "Input cannot be null");
        return start(StartWorkflowRequest.newBuilder().name(name).version(version).input(input).build());
    }

    @SneakyThrows
    public String start(StartWorkflowRequest req) {
        if (!Validator.isValidWorkflowDefName(req.getName())) {
            throw new IllegalArgumentException("Invalid workflowDef name");
        }
        if (req.getVersion() < 1) {
            throw new IllegalArgumentException("Invalid workflow version");
        }
        Objects.requireNonNull(req.getInput(), "Input cannot be null");
        return apiClient.getWorkflowClient().startWorkflow(req);
    }

    public String start(WorkflowDefinition workflowDef, Map<String, Object> input) {
        return start(workflowDef.getName(), workflowDef.getVersion(), input);
    }

    /**
     * 注册工作流定义
     *
     * @param overwrite     如果工作流存在则覆盖更新时，请设置为true。
     * @param registerTasks 如果设置为true，则用默认配置注册缺失的任务定义。
     * @return true if success, false otherwise.
     */
    public boolean registerWorkflow(WorkflowDefinition workflowDef, boolean overwrite, boolean registerTasks) {
        List<String> missing = getMissingTasks(workflowDef);
        if (!missing.isEmpty()) {
            if (!registerTasks) {
                throw new RuntimeException(
                        "Workflow cannot be registered.  The following tasks do not have definitions.  "
                                + "Please register these tasks before creating the workflow.  Missing Tasks = "
                                + missing);
            } else {
                missing.stream().forEach(taskName -> registerTaskDef(taskName, workflowDef.getOwnerEmail()));
            }
        }
        return workflowDefClient.registerWorkflow(workflowDef, overwrite);
    }

    public boolean registerWorkflow(WorkflowDefinition workflowDef, boolean overwrite) {
        return workflowDefClient.registerWorkflow(workflowDef, overwrite);
    }

    /**
     * 获取未注册任务名称
     *
     * @param workflowDef
     * @return
     */
    public List<String> getMissingTasks(WorkflowDefinition workflowDef) {
        List<String> missing = new ArrayList<>();
        workflowDef.collectTasks().stream()
                .filter(workflowTask -> workflowTask.getType().equals(TaskType.SIMPLE.name()))
                .map(FlowTask::getName)
                .distinct()
                .parallel()
                .forEach(
                        taskName -> {
                            Optional<TaskDefinition> optional = apiClient.getTaskEngine().getTaskDef(taskName);
                            if (optional.isEmpty()) {
                                missing.add(taskName);
                            }
                        });
        return missing;
    }

    private void registerTaskDef(String taskName, String ownerEmail) {
        TaskDefinition taskDef = TaskDefinition.newBuilder().name(taskName).ownerEmail(ownerEmail).build();
        apiClient.getTaskEngine().registerTaskDefs(Lists.newArrayList(taskDef));
    }

    /**
     * Removes the workflow definition of a workflow from the taskflow server. It does not remove
     * associated workflows. Use with caution.
     *
     * @param name    Name of the workflow to be unregistered.
     * @param version Version of the workflow definition to be unregistered.
     */
    public boolean deleteWorkflowDef(String name, int version) {
        return workflowDefClient.deleteWorkflowDef(name, version);
    }

    /**
     * create a workflow definition with the server
     *
     * @param workflowDef the workflow definition
     */
    public boolean createIfAbsent(WorkflowDefinition workflowDef) {
        return workflowDefClient.createIfAbsent(workflowDef);
    }

    /**
     * Updates an existing workflow definitions
     *
     * @param workflowDef workflow definitions to be updated
     */
    public boolean updateWorkflowDef(WorkflowDefinition workflowDef) {
        return workflowDefClient.updateWorkflowDef(workflowDef);
    }

    /**
     * Publishing the workflow definition to the scheduling engine server
     *
     * @param name
     * @param version
     * @param overwrite 调度引擎是否覆盖更新，缺省值:false
     * @return
     */
    public boolean publishWorkflowDef(String name, Integer version, Boolean overwrite) {
        return workflowDefClient.publishWorkflowDef(name, version, overwrite);
    }

    public WorkflowDefinition getWorkflowDef(String name, int version) {
        return workflowDefClient.getWorkflowDef(name, version);
    }
}
