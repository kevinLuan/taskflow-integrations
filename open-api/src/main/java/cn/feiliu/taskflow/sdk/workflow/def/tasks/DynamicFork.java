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
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static cn.feiliu.taskflow.expression.Expr.task;

public class DynamicFork extends Task<DynamicFork> {

    public static final String FORK_TASK_PARAM       = "forkedTasks";

    public static final String FORK_TASK_INPUT_PARAM = "forkedTasksInputs";

    private String             forkTasksParameter;

    private String             forkTasksInputsParameter;

    private Join               join;

    private SimpleTask         forkPrepareTask;

    /**
     * Dynamic fork task that executes a set of tasks in parallel which are determined at run time.
     * Use cases: Based on the input, you want to fork N number of processes in parallel to be
     * executed. The number N is not pre-determined at the definition time and so a regular ForkJoin
     * cannot be used.
     *
     * @param taskReferenceName
     */
    public DynamicFork(String taskReferenceName, String forkTasksParameter, String forkTasksInputsParameter) {
        super(taskReferenceName, TaskType.FORK_JOIN_DYNAMIC);
        this.join = new Join(taskReferenceName + "_join");
        this.forkTasksParameter = forkTasksParameter;
        this.forkTasksInputsParameter = forkTasksInputsParameter;
        super.input(FORK_TASK_PARAM, forkTasksParameter);
        super.input(FORK_TASK_INPUT_PARAM, forkTasksInputsParameter);
    }

    /**
     * Dynamic fork task that executes a set of tasks in parallel which are determined at run time.
     * Use cases: Based on the input, you want to fork N number of processes in parallel to be
     * executed. The number N is not pre-determined at the definition time and so a regular ForkJoin
     * cannot be used.
     *
     * @param taskReferenceName
     * @param forkPrepareTask   A Task that produces the output as {@link DynamicForkInput} to specify
     *                          which tasks to fork.
     */
    public DynamicFork(String taskReferenceName, SimpleTask forkPrepareTask) {
        super(taskReferenceName, TaskType.FORK_JOIN_DYNAMIC);
        this.forkPrepareTask = forkPrepareTask;
        this.join = new Join(taskReferenceName + "_join");

        this.forkTasksParameter = task(forkPrepareTask.getTaskReferenceName()).output.get(FORK_TASK_PARAM)
            .getExpression();
        this.forkTasksInputsParameter = task(forkPrepareTask.getTaskReferenceName()).output.get(FORK_TASK_INPUT_PARAM)
            .getExpression();
        super.input(FORK_TASK_PARAM, forkTasksParameter);
        super.input(FORK_TASK_INPUT_PARAM, forkTasksInputsParameter);
    }

    DynamicFork(FlowTask workflowTask) {
        super(workflowTask);
        String nameOfParamForForkTask = workflowTask.getDynamicForkTasksParam();
        String nameOfParamForForkTaskInput = workflowTask.getDynamicForkTasksInputParamName();
        this.forkTasksParameter = (String) workflowTask.getInputParameters().get(nameOfParamForForkTask);
        this.forkTasksInputsParameter = (String) workflowTask.getInputParameters().get(nameOfParamForForkTaskInput);
    }

    public Join getJoin() {
        return join;
    }

    public String getForkTasksParameter() {
        return forkTasksParameter;
    }

    public String getForkTasksInputsParameter() {
        return forkTasksInputsParameter;
    }

    @Override
    public void updateWorkflowTask(FlowTask task) {
        task.setDynamicForkTasksParam("forkedTasks");
        task.setDynamicForkTasksInputParamName("forkedTasksInputs");
    }

    @Override
    protected List<FlowTask> getChildrenTasks() {
        List<FlowTask> tasks = new ArrayList<>();
        tasks.addAll(join.getWorkflowDefTasks());
        return tasks;
    }

    @Override
    protected List<FlowTask> getParentTasks() {
        if (forkPrepareTask != null) {
            return List.of(forkPrepareTask.toWorkflowTask());
        }
        return List.of();
    }

    public void refreshInput(Function<Pair<String/*参数名*/, String/*参数值*/>, Object/*修改后的值*/> function) {
        for (Map.Entry<String, Object> entry : forkPrepareTask.getInput().entrySet()) {
            if (entry.getValue() instanceof String) {
                Object newValue = function.apply(Pair.of(entry.getKey(), (String) entry.getValue()));
                if (newValue != null) {
                    entry.setValue(newValue);
                }
            }
        }
    }
}
