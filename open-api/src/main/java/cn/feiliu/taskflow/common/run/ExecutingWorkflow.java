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
package cn.feiliu.taskflow.common.run;

import cn.feiliu.taskflow.common.metadata.tasks.ExecutingTask;
import cn.feiliu.taskflow.common.metadata.workflow.WorkflowDefinition;
import com.google.common.base.Preconditions;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class ExecutingWorkflow {

    public enum WorkflowStatus {
        RUNNING(false, false), //
        COMPLETED(true, true), //
        FAILED(true, false), //
        TIMED_OUT(true, false), //
        TERMINATED(true, false), //
        PAUSED(false, true);//

        private final boolean terminal;

        private final boolean successful;

        WorkflowStatus(boolean terminal, boolean successful) {
            this.terminal = terminal;
            this.successful = successful;
        }

        public boolean isTerminal() {
            return terminal;
        }

        public boolean isSuccessful() {
            return successful;
        }
    }

    private WorkflowStatus      status                   = WorkflowStatus.RUNNING;
    private long                startTime;
    private long                endTime;

    private String              workflowId;

    private String              parentWorkflowId;

    private String              parentWorkflowTaskId;
    /* the tasks which are scheduled, in progress or completed.*/
    private List<ExecutingTask> tasks                    = new LinkedList<>();

    private Map<String, Object> input                    = new HashMap<>();

    private Map<String, Object> output                   = new HashMap<>();

    // ids 10,11 are reserved
    /*启动工作流时使用的关联业务id（可选）*/
    private String              correlationId;

    private String              reRunFromWorkflowId;

    private String              reasonForIncompletion;

    // id 15 is reserved
    /*启动工作流的事件的名称*/
    private String              event;
    /*映射到指定域的任务*/
    private Map<String, String> taskToDomain             = new HashMap<>();

    private Set<String>         failedReferenceTaskNames = new HashSet<>();

    private WorkflowDefinition  workflowDefinition;
    /*工作流输入有效负载的外部存储路径*/
    private String              externalInputPayloadStoragePath;
    /*存储工作流输出负载的外部存储路径*/
    private String              externalOutputPayloadStoragePath;
    /*在任务上定义优先级*/
    @Min(value = 0, message = "workflow priority: ${validatedValue} should be minimum {value}")
    @Max(value = 99, message = "workflow priority: ${validatedValue} should be maximum {value}")
    private int                 priority;
    /*全局工作流变量*/
    private Map<String, Object> variables                = new HashMap<>();
    /*捕获最后一次重试工作流的时间*/
    private long                lastRetriedTime;

    public ExecutingWorkflow() {
    }

    /**
     * @param priority priority of tasks (between 0 and 99)
     */
    public void setPriority(int priority) {
        if (priority < 0 || priority > 99) {
            throw new IllegalArgumentException("priority MUST be between 0 and 99 (inclusive)");
        }
        this.priority = priority;
    }

    /**
     * Convenience method for accessing the workflow definition name.
     *
     * @return the workflow definition name.
     */
    public String getWorkflowName() {
        Preconditions.checkNotNull(workflowDefinition, "Workflow definition is null");
        return workflowDefinition.getName();
    }

    /**
     * Convenience method for accessing the workflow definition version.
     *
     * @return the workflow definition version.
     */
    public int getWorkflowVersion() {
        Preconditions.checkNotNull(workflowDefinition, "Workflow definition is null");
        return workflowDefinition.getVersion();
    }

    public boolean hasParent() {
        return StringUtils.isNotEmpty(parentWorkflowId);
    }

    public ExecutingTask getTaskByRefName(String refName) {
        if (refName == null) {
            throw new RuntimeException(
                "refName passed is null.  Check the workflow execution.  For dynamic tasks, make sure referenceTaskName is set to a not null value");
        }
        LinkedList<ExecutingTask> found = new LinkedList<>();
        for (ExecutingTask t : tasks) {
            if (t.getReferenceTaskName() == null) {
                throw new RuntimeException("Task " + t.getTaskDefName() + ", seq=" + t.getSeq()
                                           + " does not have reference name specified.");
            }
            if (t.getReferenceTaskName().equals(refName)) {
                found.add(t);
            }
        }
        if (found.isEmpty()) {
            return null;
        }
        return found.getLast();
    }

    /**
     * @return a deep copy of the workflow instance
     */
    public ExecutingWorkflow copy() {
        ExecutingWorkflow copy = new ExecutingWorkflow();
        copy.setInput(input);
        copy.setOutput(output);
        copy.setStatus(status);
        copy.setWorkflowId(workflowId);
        copy.setParentWorkflowId(parentWorkflowId);
        copy.setParentWorkflowTaskId(parentWorkflowTaskId);
        copy.setReRunFromWorkflowId(reRunFromWorkflowId);
        copy.setCorrelationId(correlationId);
        copy.setEvent(event);
        copy.setReasonForIncompletion(reasonForIncompletion);
        copy.setWorkflowDefinition(workflowDefinition);
        copy.setPriority(priority);
        copy.setTasks(tasks.stream().map(ExecutingTask::deepCopy).collect(Collectors.toList()));
        copy.setVariables(variables);
        copy.setEndTime(endTime);
        copy.setLastRetriedTime(lastRetriedTime);
        copy.setTaskToDomain(taskToDomain);
        copy.setFailedReferenceTaskNames(failedReferenceTaskNames);
        copy.setExternalInputPayloadStoragePath(externalInputPayloadStoragePath);
        copy.setExternalOutputPayloadStoragePath(externalOutputPayloadStoragePath);
        return copy;
    }

    @Override
    public String toString() {
        String name = workflowDefinition != null ? workflowDefinition.getName() : null;
        Integer version = workflowDefinition != null ? workflowDefinition.getVersion() : null;
        return String.format("%s.%s/%s.%s", name, version, workflowId, status);
    }

    /**
     * A string representation of all relevant fields that identify this workflow. Intended for use
     * in log and other system generated messages.
     */
    public String toShortString() {
        String name = workflowDefinition != null ? workflowDefinition.getName() : null;
        Integer version = workflowDefinition != null ? workflowDefinition.getVersion() : null;
        return String.format("%s.%s/%s", name, version, workflowId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExecutingWorkflow workflow = (ExecutingWorkflow) o;
        return getEndTime() == workflow.getEndTime() && getWorkflowVersion() == workflow.getWorkflowVersion()
               && getStatus() == workflow.getStatus() && Objects.equals(getWorkflowId(), workflow.getWorkflowId())
               && Objects.equals(getParentWorkflowId(), workflow.getParentWorkflowId())
               && Objects.equals(getParentWorkflowTaskId(), workflow.getParentWorkflowTaskId())
               && Objects.equals(getTasks(), workflow.getTasks()) && Objects.equals(getInput(), workflow.getInput())
               && Objects.equals(getOutput(), workflow.getOutput())
               && Objects.equals(getWorkflowName(), workflow.getWorkflowName())
               && Objects.equals(getCorrelationId(), workflow.getCorrelationId())
               && Objects.equals(getReRunFromWorkflowId(), workflow.getReRunFromWorkflowId())
               && Objects.equals(getReasonForIncompletion(), workflow.getReasonForIncompletion())
               && Objects.equals(getEvent(), workflow.getEvent())
               && Objects.equals(getTaskToDomain(), workflow.getTaskToDomain())
               && Objects.equals(getFailedReferenceTaskNames(), workflow.getFailedReferenceTaskNames())
               && Objects.equals(getExternalInputPayloadStoragePath(), workflow.getExternalInputPayloadStoragePath())
               && Objects.equals(getExternalOutputPayloadStoragePath(), workflow.getExternalOutputPayloadStoragePath())
               && Objects.equals(getPriority(), workflow.getPriority())
               && Objects.equals(getWorkflowDefinition(), workflow.getWorkflowDefinition())
               && Objects.equals(getVariables(), workflow.getVariables())
               && Objects.equals(getLastRetriedTime(), workflow.getLastRetriedTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStatus(), getEndTime(), getWorkflowId(), getParentWorkflowId(),
            getParentWorkflowTaskId(), getTasks(), getInput(), getOutput(), getWorkflowName(), getWorkflowVersion(),
            getCorrelationId(), getReRunFromWorkflowId(), getReasonForIncompletion(), getEvent(), getTaskToDomain(),
            getFailedReferenceTaskNames(), getWorkflowDefinition(), getExternalInputPayloadStoragePath(),
            getExternalOutputPayloadStoragePath(), getPriority(), getVariables(), getLastRetriedTime());
    }
}
