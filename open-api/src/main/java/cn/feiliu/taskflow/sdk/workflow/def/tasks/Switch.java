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

import cn.feiliu.taskflow.common.metadata.tasks.TaskType;
import cn.feiliu.taskflow.common.metadata.workflow.FlowTask;
import cn.feiliu.taskflow.expression.PathExpression;

import java.util.*;

/**
 *   +----------------------+
 *   | switch (expression)  |
 *   +----------------------+
 *             |
 *             v
 *   +----------------------+
 *   |     case 张三         |
 *   +----------------------+
 *             |
 *             v
 *   +----------------------+
 *   |   do something task  |
 *   +----------------------+
 *             |
 *             v
 *   +----------------------+
 *   |      default:        |
 *   +----------------------+
 *             |
 *             v
 *   +----------------------+
 *   |  do something task   |
 *   +----------------------+
 */
public class Switch extends Task<Switch> {

    public static final String         VALUE_PARAM_NAME = "value-param";

    public static final String         JAVASCRIPT_NAME  = "javascript";

    private String                     caseExpression;

    private boolean                    useJavascript;

    private List<Task<?>>              defaultTasks     = new ArrayList<>();

    private Map<String, List<Task<?>>> branches         = new HashMap<>();

    /**
     * Switch case (similar to if...then...else or switch in java language)
     *
     * @param taskReferenceName
     * @param caseExpression An expression that outputs a string value to be used as case branches.
     *     Case expression can be a support value parameter e.g. ${workflow.input.key} or
     *     ${task.output.key} or a Javascript statement.
     * @param useJavascript set to true if the caseExpression is a javascript statement
     */
    public Switch(String taskReferenceName, String caseExpression, boolean useJavascript) {
        super(taskReferenceName, TaskType.SWITCH);
        this.caseExpression = caseExpression;
        this.useJavascript = useJavascript;
    }

    public Switch(String taskReferenceName, PathExpression expression, boolean useJavascript) {
        this(taskReferenceName, expression.getExpression(), useJavascript);
    }

    /**
     * Switch case (similar to if...then...else or switch in java language)
     *
     * @param taskReferenceName
     * @param caseExpression
     */
    public Switch(String taskReferenceName, String caseExpression) {
        super(taskReferenceName, TaskType.SWITCH);
        this.caseExpression = caseExpression;
        this.useJavascript = false;
    }

    public Switch(String taskReferenceName, PathExpression expression) {
        this(taskReferenceName, expression.getExpression());
    }

    Switch(FlowTask workflowTask) {
        super(workflowTask);
        Map<String, List<FlowTask>> decisions = workflowTask.getDecisionCases();

        decisions.entrySet().stream()
                .forEach(
                        branch -> {
                            String branchName = branch.getKey();
                            List<FlowTask> branchWorkflowTasks = branch.getValue();
                            List<Task<?>> branchTasks = new ArrayList<>();
                            for (FlowTask branchWorkflowTask : branchWorkflowTasks) {
                                branchTasks.add(TaskRegistry.getTask(branchWorkflowTask));
                            }
                            this.branches.put(branchName, branchTasks);
                        });

        List<FlowTask> defaultCases = workflowTask.getDefaultCase();
        for (FlowTask defaultCase : defaultCases) {
            this.defaultTasks.add(TaskRegistry.getTask(defaultCase));
        }
    }

    public Switch defaultCase(Task<?>... tasks) {
        defaultTasks = Arrays.asList(tasks);
        return this;
    }

    public Switch defaultCase(List<Task<?>> defaultTasks) {
        this.defaultTasks = defaultTasks;
        return this;
    }

    public Switch decisionCases(Map<String, List<Task<?>>> branches) {
        this.branches = branches;
        return this;
    }

    public Switch defaultCase(String... workerTasks) {
        for (String workerTask : workerTasks) {
            this.defaultTasks.add(new WorkTask(workerTask, workerTask));
        }
        return this;
    }

    public Switch switchCase(String caseValue, Task... tasks) {
        branches.put(caseValue, Arrays.asList(tasks));
        return this;
    }

    public Switch switchCase(String caseValue, String... workerTasks) {
        List<Task<?>> tasks = new ArrayList<>(workerTasks.length);
        int i = 0;
        for (String workerTask : workerTasks) {
            tasks.add(new WorkTask(workerTask, workerTask));
        }
        branches.put(caseValue, tasks);
        return this;
    }

    public List<Task<?>> getDefaultTasks() {
        return defaultTasks;
    }

    public Map<String, List<Task<?>>> getBranches() {
        return branches;
    }

    @Override
    public void updateWorkflowTask(FlowTask workflowTask) {

        if (useJavascript) {
            workflowTask.setEvaluatorType(JAVASCRIPT_NAME);
            workflowTask.setExpression(caseExpression);

        } else {
            workflowTask.setEvaluatorType(VALUE_PARAM_NAME);
            workflowTask.getInputParameters().put("switchCaseValue", caseExpression);
            workflowTask.setExpression("switchCaseValue");
        }

        Map<String, List<FlowTask>> decisionCases = new HashMap<>();
        branches.entrySet()
                .forEach(
                        entry -> {
                            String decisionCase = entry.getKey();
                            List<Task<?>> decisionTasks = entry.getValue();
                            List<FlowTask> decionTaskDefs =
                                    new ArrayList<>(decisionTasks.size());
                            for (Task<?> decisionTask : decisionTasks) {
                                decionTaskDefs.addAll(decisionTask.getWorkflowDefTasks());
                            }
                            decisionCases.put(decisionCase, decionTaskDefs);
                        });

        workflowTask.setDecisionCases(decisionCases);
        List<FlowTask> defaultCaseTaskDefs = new ArrayList<>(defaultTasks.size());
        for (Task<?> defaultTask : defaultTasks) {
            defaultCaseTaskDefs.addAll(defaultTask.getWorkflowDefTasks());
        }
        workflowTask.setDefaultCase(defaultCaseTaskDefs);
    }
}
