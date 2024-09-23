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
package cn.feiliu.taskflow.common.metadata.workflow;

import cn.feiliu.taskflow.common.metadata.tasks.TaskDefinition;
import cn.feiliu.taskflow.common.metadata.tasks.TaskType;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.PositiveOrZero;
import java.util.*;

/**
 * This is the task definition definied as part of the {@link WorkflowDefinition}. The tasks definied in
 * the Workflow definition are saved as part of {@link WorkflowDefinition#getTasks}
 */
@Data
public class FlowTask {
    /**
     * 任务名称。(应用级唯一)
     */
    @NotEmpty(message = "FlowTask name cannot be empty or null")
    private String name;

    @NotEmpty(message = "FlowTask taskReferenceName name cannot be empty or null")
    private String taskReferenceName;

    private String description;

    private Map<String, Object> inputParameters = new HashMap<>();

    private TaskType type = TaskType.SIMPLE;
    /**
     * dynamicTaskNameParam the dynamicTaskNameParam to set to be used by DYNAMIC tasks
     */
    private String dynamicTaskNameParam;
    /**
     * @deprecated Use {@link FlowTask#getEvaluatorType()} and {@link
     * FlowTask#getExpression()} combination.
     */
    @Deprecated
    private String caseValueParam;
    /**
     * A javascript expression for decision cases. The result should be a scalar value that
     * * is used to decide the case branches.
     * * @see #getDecisionCases()
     * * @deprecated Use {@link FlowTask#getEvaluatorType()} and {@link
     * * WorkflowTask#getExpression()} combination.
     */
    @Deprecated
    private String caseExpression;

    private String scriptExpression;

    public void copyFrom(FlowTask flowTask) {
        this.setName(flowTask.getName());
        this.setTaskReferenceName(flowTask.getTaskReferenceName());
        this.setDescription(flowTask.getDescription());
        this.setInputParameters(flowTask.getInputParameters());
        this.setType(flowTask.getType());
        this.setDynamicTaskNameParam(flowTask.getDynamicTaskNameParam());
        this.setCaseValueParam(flowTask.getCaseValueParam());
        this.setCaseExpression(flowTask.getCaseExpression());
        this.setScriptExpression(flowTask.getScriptExpression());
        this.setDecisionCases(flowTask.getDecisionCases());
        this.setDynamicForkJoinTasksParam(flowTask.getDynamicForkJoinTasksParam());
        this.setDynamicForkTasksParam(flowTask.getDynamicForkTasksParam());
        this.setDynamicForkTasksInputParamName(flowTask.getDynamicForkTasksInputParamName());
        this.setDefaultCase(flowTask.getDefaultCase());
        this.setForkTasks(flowTask.getForkTasks());
        this.setStartDelay(flowTask.getStartDelay());
        this.setSubWorkflowParam(flowTask.getSubWorkflowParam());
        this.setJoinOn(flowTask.getJoinOn());
        this.setSink(flowTask.getSink());
        this.setOptional(flowTask.isOptional());
        this.setTaskDefinition(flowTask.getTaskDefinition());
        this.setRateLimited(flowTask.isRateLimited());
        this.setDefaultExclusiveJoinTask(flowTask.getDefaultExclusiveJoinTask());
        this.setAsyncComplete(flowTask.isAsyncComplete());
        this.setLoopCondition(flowTask.getLoopCondition());
        this.setLoopOver(flowTask.getLoopOver());
        this.setRetryCount(flowTask.getRetryCount());
        this.setEvaluatorType(flowTask.getEvaluatorType());
        this.setExpression(flowTask.getExpression());
    }

    @Data
    public static class WorkflowTaskList {
        private List<FlowTask> tasks;
    }

    // 为决策类型的任务进行填充
    private Map<String, @Valid List<@Valid FlowTask>> decisionCases = new LinkedHashMap<>();

    @Deprecated
    private String dynamicForkJoinTasksParam;

    private String dynamicForkTasksParam;

    private String dynamicForkTasksInputParamName;

    private List<@Valid FlowTask> defaultCase = new LinkedList<>();

    private List<@Valid List<@Valid FlowTask>> forkTasks = new LinkedList<>();

    @PositiveOrZero
    private int startDelay; // No. of seconds (at-least) to wait before starting a task.

    @Valid
    private SubFlowParams subWorkflowParam;

    private List<String> joinOn = new LinkedList<>();
    /**
     * Sink value for the EVENT type of task
     */
    private String sink;
    /**
     * If the task is optional. When set to true, the workflow execution continues even when the task is in failed status.
     */
    private boolean optional = false;
    /**
     * Task definition associated to the Workflow Task
     */
    private TaskDefinition taskDefinition;

    private boolean rateLimited;

    private List<String> defaultExclusiveJoinTask = new LinkedList<>();
    /**
     * whether wait for an external event to complete the task, for EVENT and HTTP tasks
     */
    private boolean asyncComplete = false;

    private String loopCondition;

    private List<FlowTask> loopOver = new LinkedList<>();

    private Integer retryCount = 3;

    private String evaluatorType;
    /**
     * An evaluation expression for switch cases evaluated by corresponding evaluator. The
     * result should be a scalar value that is used to decide the case branches.
     *
     * @see #getDecisionCases()
     */
    private String expression;

    private Collection<List<FlowTask>> children() {
        Collection<List<FlowTask>> workflowTaskLists = new LinkedList<>();

        switch (type) {
            case DECISION:
            case SWITCH:
                workflowTaskLists.addAll(decisionCases.values());
                workflowTaskLists.add(defaultCase);
                break;
            case FORK_JOIN:
                workflowTaskLists.addAll(forkTasks);
                break;
            case DO_WHILE:
            case FOR_EACH:
            case FORK_FOR_EACH:
                workflowTaskLists.add(loopOver);
                break;
            default:
                break;
        }
        return workflowTaskLists;
    }

    public List<FlowTask> collectTasks() {
        List<FlowTask> tasks = new LinkedList<>();
        tasks.add(this);
        for (List<FlowTask> workflowTaskList : children()) {
            for (FlowTask workflowTask : workflowTaskList) {
                tasks.addAll(workflowTask.collectTasks());
            }
        }
        return tasks;
    }

    public FlowTask next(String taskReferenceName, FlowTask parent) {
        switch (type) {
            case DO_WHILE:
            case DECISION:
            case FOR_EACH:
            case FORK_FOR_EACH:
            case SWITCH:
                for (List<FlowTask> workflowTasks : children()) {
                    Iterator<FlowTask> iterator = workflowTasks.iterator();
                    while (iterator.hasNext()) {
                        FlowTask task = iterator.next();
                        if (task.getTaskReferenceName().equals(taskReferenceName)) {
                            break;
                        }
                        FlowTask nextTask = task.next(taskReferenceName, this);
                        if (nextTask != null) {
                            return nextTask;
                        }
                        if (task.has(taskReferenceName)) {
                            break;
                        }
                    }
                    if (iterator.hasNext()) {
                        return iterator.next();
                    }
                }
                if (type == TaskType.DO_WHILE && this.has(taskReferenceName)) {
                    // come here means this is DO_WHILE task and `taskReferenceName` is the last
                    // task in
                    // this DO_WHILE task, because DO_WHILE task need to be executed to decide
                    // whether to
                    // schedule next iteration, so we just return the DO_WHILE task, and then ignore
                    // generating this task again in deciderService.getNextTask()
                    return this;
                }
                break;
            case FORK_JOIN:
                boolean found = false;
                for (List<FlowTask> workflowTasks : children()) {
                    Iterator<FlowTask> iterator = workflowTasks.iterator();
                    while (iterator.hasNext()) {
                        FlowTask task = iterator.next();
                        if (task.getTaskReferenceName().equals(taskReferenceName)) {
                            found = true;
                            break;
                        }
                        FlowTask nextTask = task.next(taskReferenceName, this);
                        if (nextTask != null) {
                            return nextTask;
                        }
                        if (task.has(taskReferenceName)) {
                            break;
                        }
                    }
                    if (iterator.hasNext()) {
                        return iterator.next();
                    }
                    if (found && parent != null) {
                        return parent.next(
                                this.taskReferenceName,
                                parent); // we need to return join task... -- get my sibling from my
                        // parent..
                    }
                }
                break;
            case DYNAMIC:
            case TERMINATE:
            case SIMPLE:
                return null;
            default:
                break;
        }
        return null;
    }

    public boolean has(String taskReferenceName) {
        if (this.getTaskReferenceName().equals(taskReferenceName)) {
            return true;
        }

        switch (type) {
            case DECISION:
            case SWITCH:
            case FOR_EACH:
            case FORK_FOR_EACH:
            case DO_WHILE:
            case FORK_JOIN:
                for (List<FlowTask> childx : children()) {
                    for (FlowTask child : childx) {
                        if (child.has(taskReferenceName)) {
                            return true;
                        }
                    }
                }
                break;
            default:
                break;
        }
        return false;
    }

    public FlowTask get(String taskReferenceName) {
        if (this.getTaskReferenceName().equals(taskReferenceName)) {
            return this;
        }
        for (List<FlowTask> childx : children()) {
            for (FlowTask child : childx) {
                FlowTask found = child.get(taskReferenceName);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name + "/" + taskReferenceName;
    }
}
