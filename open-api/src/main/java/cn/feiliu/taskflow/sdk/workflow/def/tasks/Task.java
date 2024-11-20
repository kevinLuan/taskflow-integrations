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
package cn.feiliu.taskflow.sdk.workflow.def.tasks;

import cn.feiliu.taskflow.common.utils.Assertions;
import cn.feiliu.taskflow.expression.PathExpression;
import cn.feiliu.taskflow.serialization.SerializerFactory;
import cn.feiliu.taskflow.sdk.workflow.utils.MapBuilder;
import cn.feiliu.taskflow.common.metadata.tasks.TaskType;
import cn.feiliu.taskflow.common.metadata.workflow.FlowTask;

import javax.validation.constraints.NotEmpty;
import java.util.*;

/**
 * Workflow Task
 */
public abstract class Task<T> {

    @NotEmpty(message = "Task name cannot be empty or null")
    private String              name;
    @NotEmpty(message = "Task taskReferenceName name cannot be empty or null")
    private String              taskReferenceName;

    private String              description;

    private boolean             optional;

    private int                 startDelay;

    private TaskType            type;

    private Map<String, Object> input = new HashMap<>();

    public Task(String taskReferenceName, TaskType type) {
        this(type.name().toLowerCase(), taskReferenceName, type);
        if (type.isSimple()) {
            throw new IllegalArgumentException(
                "Please call using constructor: `(taskDefName, taskReferenceName, type)`");
        }
    }

    /**
     * SimpleType类型初始化
     *
     * @param taskDefName       任务定义名称
     * @param taskReferenceName 任务引用名称
     */
    public Task(String taskDefName, String taskReferenceName, TaskType type) {
        this.name = Objects.requireNonNull(taskDefName);
        this.taskReferenceName = Objects.requireNonNull(taskReferenceName);
        this.type = Objects.requireNonNull(type);
    }

    Task(FlowTask workflowTask) {
        this(workflowTask.getTaskReferenceName(), workflowTask.getType());
        this.input = workflowTask.getInputParameters();
        this.description = workflowTask.getDescription();
        this.name = workflowTask.getName();
    }

    public T name(String name) {
        this.name = name;
        return (T) this;
    }

    public T description(String description) {
        this.description = description;
        return (T) this;
    }

    public T input(String key, boolean value) {
        input.put(key, value);
        return (T) this;
    }

    public T input(String key, Object value) {
        input.put(key, value);
        return (T) this;
    }

    public T input(String key, char value) {
        input.put(key, value);
        return (T) this;
    }

    public T input(String key, PathExpression pathExpression) {
        input.put(key, pathExpression.getExpression());
        return (T) this;
    }

    public T input(String key, String value) {
        input.put(key, value);
        return (T) this;
    }

    public T input(String key, Number value) {
        input.put(key, value);
        return (T) this;
    }

    public T input(String key, Map<String, Object> value) {
        input.put(key, value);
        return (T) this;
    }

    public T input(Map<String, Object> map) {
        input.putAll(map);
        return (T) this;
    }

    public T input(MapBuilder builder) {
        input.putAll(builder.build());
        return (T) this;
    }

    public T input(Object... keyValues) {
        if (keyValues.length == 1) {
            Object kv = keyValues[0];
            input.putAll(SerializerFactory.getSerializer().convertMap(kv));
            return (T) this;
        }
        if (keyValues.length % 2 == 1) {
            throw new IllegalArgumentException("Not all keys have value specified");
        }
        for (int i = 0; i < keyValues.length;) {
            String key = keyValues[i].toString();
            Object value = keyValues[i + 1];
            input.put(key, value);
            i += 2;
        }
        return (T) this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTaskReferenceName() {
        return taskReferenceName;
    }

    public void setTaskReferenceName(String taskReferenceName) {
        this.taskReferenceName = taskReferenceName;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public int getStartDelay() {
        return startDelay;
    }

    public void setStartDelay(int startDelay) {
        this.startDelay = startDelay;
    }

    public TaskType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getInput() {
        return input;
    }

    public final List<FlowTask> getWorkflowDefTasks() {
        List<FlowTask> workflowTasks = new ArrayList<>();
        workflowTasks.addAll(getParentTasks());
        workflowTasks.add(toWorkflowTask());
        workflowTasks.addAll(getChildrenTasks());
        return workflowTasks;
    }

    protected final FlowTask toWorkflowTask() {
        FlowTask workflowTask = new FlowTask();
        workflowTask.setName(name);
        workflowTask.setTaskReferenceName(taskReferenceName);
        workflowTask.setType(type);
        workflowTask.setDescription(description);
        workflowTask.setInputParameters(input);

        // Let the sub-classes enrich the workflow task before returning back
        updateWorkflowTask(workflowTask);

        return workflowTask;
    }

    /**
     * Override this method when the sub-class should update the default WorkflowTask generated
     * using {@link #toWorkflowTask()}
     *
     * @param workflowTask
     */
    protected void updateWorkflowTask(FlowTask workflowTask) {
    }

    /**
     * Override this method when sub-classes will generate multiple workflow tasks. Used by tasks
     * which have children tasks such as do_while, fork, etc.
     *
     * @return
     */
    protected List<FlowTask> getChildrenTasks() {
        return List.of();
    }

    protected List<FlowTask> getParentTasks() {
        return List.of();
    }
}
