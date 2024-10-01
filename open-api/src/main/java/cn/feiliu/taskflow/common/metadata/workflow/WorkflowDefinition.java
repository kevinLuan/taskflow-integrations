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
package cn.feiliu.taskflow.common.metadata.workflow;

import cn.feiliu.taskflow.common.constraints.EmailConstraint;
import cn.feiliu.taskflow.common.constraints.TaskReferenceNameUniqueConstraint;
import cn.feiliu.taskflow.common.constraints.WorkflowNameConstraint;
import cn.feiliu.taskflow.common.metadata.tasks.TaskType;
import cn.feiliu.taskflow.sdk.workflow.def.tasks.Task;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.*;

@Setter
@Getter
@ToString
@TaskReferenceNameUniqueConstraint
public class WorkflowDefinition {
    public enum TimeoutPolicy {
        TIME_OUT_WF,
        ALERT_ONLY
    }

    /**
     * 工作流名称。(租户级唯一)
     */
    @WorkflowNameConstraint(message = "Invalid workflow name")
    private String name;

    private String description;

    private int version = 1;

    @NotNull
    @NotEmpty(message = "WorkflowTask list cannot be empty")
    private List<@Valid FlowTask> tasks = new LinkedList<>();

    private List<String> inputParameters = new LinkedList<>();

    private Map<String, Object> outputParameters = new HashMap<>();

    private String failureWorkflow;

    /**
     * True:表示工作流可重启, false:表示工作流不可重启
     */
    private boolean restartable = true;
    /**
     * Specify if workflow listener is enabled to invoke a callback for completed or terminated workflows
     * <p/>
     * True表示工作流监听器将在工作流进入终端状态时被调用
     */
    private boolean workflowStatusListenerEnabled = false;
    /**
     * 此工作流定义的所有者的电子邮件
     */
    @EmailConstraint(message = "ownerEmail should be valid email address")
    @Email(message = "ownerEmail should be valid email address")
    private String ownerEmail;

    private TimeoutPolicy timeoutPolicy = TimeoutPolicy.ALERT_ONLY;
    /**
     * 工作流程被认为超时的时间
     * the time after which a workflow is deemed to have timed out
     */
    @NotNull
    private long timeoutSeconds;
    /**
     * the global workflow variables
     */
    private Map<String, Object> variables = new HashMap<>();

    private Map<String, Object> inputTemplate = new HashMap<>();

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(String name, int version) {
        return newBuilder().name(name).version(version);
    }

    public static class Builder {
        private String name;
        private String description;
        private int version = 1;
        private List<Task> tasks = new LinkedList<>();
        private List<String> inputParameters = new LinkedList<>();
        private Map<String, Object> outputParameters = new HashMap<>();
        private String failureWorkflow;
        private boolean restartable = true;
        private boolean workflowStatusListenerEnabled = false;
        private String ownerEmail;
        private TimeoutPolicy timeoutPolicy = TimeoutPolicy.ALERT_ONLY;
        private long timeoutSeconds;
        private Map<String, Object> variables = new HashMap<>();
        private Map<String, Object> inputTemplate = new HashMap<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder version(int version) {
            this.version = version;
            return this;
        }

        public Builder tasks(List<Task> tasks) {
            this.tasks = tasks;
            return this;
        }

        public Builder addTask(Task task) {
            this.tasks.add(task);
            return this;
        }

        public Builder inputParameters(List<String> inputParameters) {
            this.inputParameters = inputParameters;
            return this;
        }

        public Builder outputParameters(Map<String, Object> outputParameters) {
            this.outputParameters = outputParameters;
            return this;
        }

        public Builder failureWorkflow(String failureWorkflow) {
            this.failureWorkflow = failureWorkflow;
            return this;
        }

        public Builder restartable(boolean restartable) {
            this.restartable = restartable;
            return this;
        }

        public Builder workflowStatusListenerEnabled(boolean workflowStatusListenerEnabled) {
            this.workflowStatusListenerEnabled = workflowStatusListenerEnabled;
            return this;
        }

        public Builder ownerEmail(String ownerEmail) {
            this.ownerEmail = ownerEmail;
            return this;
        }

        public Builder timeoutPolicy(TimeoutPolicy timeoutPolicy) {
            this.timeoutPolicy = timeoutPolicy;
            return this;
        }

        public Builder timeoutSeconds(long timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        public Builder variables(Map<String, Object> variables) {
            this.variables = variables;
            return this;
        }

        public Builder inputTemplate(Map<String, Object> inputTemplate) {
            this.inputTemplate = inputTemplate;
            return this;
        }

        public WorkflowDefinition build() {
            WorkflowDefinition workflowDefinition = new WorkflowDefinition();
            workflowDefinition.name = this.name;
            workflowDefinition.description = this.description;
            workflowDefinition.version = this.version;
            List<FlowTask> flowTasks = new ArrayList<>();
            for (Task task : tasks) {
                flowTasks.addAll(task.getWorkflowDefTasks());
            }
            workflowDefinition.tasks = flowTasks;
            workflowDefinition.inputParameters = this.inputParameters;
            workflowDefinition.outputParameters = this.outputParameters;
            workflowDefinition.failureWorkflow = this.failureWorkflow;
            workflowDefinition.restartable = this.restartable;
            workflowDefinition.workflowStatusListenerEnabled = this.workflowStatusListenerEnabled;
            workflowDefinition.ownerEmail = this.ownerEmail;
            workflowDefinition.timeoutPolicy = this.timeoutPolicy;
            workflowDefinition.timeoutSeconds = this.timeoutSeconds;
            workflowDefinition.variables = this.variables;
            workflowDefinition.inputTemplate = this.inputTemplate;
            return workflowDefinition;
        }
    }

    public String key() {
        return getKey(name, version);
    }

    public static String getKey(String name, int version) {
        return name + "." + version;
    }

    public boolean containsType(String taskType) {
        return collectTasks().stream().anyMatch(t -> t.getType().equals(taskType));
    }

    public FlowTask getNextTask(String taskReferenceName) {
        FlowTask workflowTask = getTaskByRefName(taskReferenceName);
        if (workflowTask != null && TaskType.TERMINATE.name().equals(workflowTask.getType())) {
            return null;
        }

        Iterator<FlowTask> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            FlowTask task = iterator.next();
            if (task.getTaskReferenceName().equals(taskReferenceName)) {
                // If taskReferenceName matches, break out
                break;
            }
            FlowTask nextTask = task.next(taskReferenceName, null);
            if (nextTask != null) {
                return nextTask;
            } else if (TaskType.DO_WHILE.name().equals(task.getType())
                    && !task.getTaskReferenceName().equals(taskReferenceName)
                    && task.has(taskReferenceName)) {
                // If the task is child of Loop Task and at last position, return null.
                return null;
            }

            if (task.has(taskReferenceName)) {
                break;
            }
        }
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    public FlowTask getTaskByRefName(String taskReferenceName) {
        return collectTasks().stream()
                .filter(
                        workflowTask ->
                                workflowTask.getTaskReferenceName().equals(taskReferenceName))
                .findFirst()
                .orElse(null);
    }

    public List<FlowTask> collectTasks() {
        List<FlowTask> tasks = new LinkedList<>();
        for (FlowTask workflowTask : this.tasks) {
            tasks.addAll(workflowTask.collectTasks());
        }
        return tasks;
    }
}
