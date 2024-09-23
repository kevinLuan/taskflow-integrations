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
package cn.feiliu.taskflow.sdk.workflow.def.tasks;

import cn.feiliu.taskflow.common.metadata.tasks.TaskType;
import cn.feiliu.taskflow.common.metadata.workflow.FlowTask;
import cn.feiliu.taskflow.common.utils.JsValidator;
import cn.feiliu.taskflow.common.utils.Validator;
import cn.feiliu.taskflow.expression.PathExpression;
import cn.feiliu.taskflow.sdk.workflow.def.ILoopTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.feiliu.taskflow.common.utils.TaskflowUtils.f;

/**
 * do while 可以在循环中执行一组任务，直到条件不再满足
 * 例如：${workflow.input.loopCount} 为循环遍历条件表达式
 * <pre>
 * ----------------------------
 * |           do             |
 * ----------------------------
 * |                          |
 * task1                      |
 * |                          |
 * task2                      |
 * |                          |
 * |--------------------------|
 * |    while($loopCount)     |
 * |--------------------------|
 * </pre>
 */
public class DoWhile extends Task<DoWhile> implements ILoopTask<DoWhile> {
    private String              loopCondition;
    public final static String  LOOP_COUNT = "loopCount";
    private final List<Task<?>> loopTasks  = new ArrayList<>();

    /**
     * Execute tasks in a loop determined by the condition set using condition parameter. The loop
     * will continue till the condition is true
     *
     * @param taskReferenceName
     * @param expression        ${workflow.input.loopCount} 要求数据类型为Number类型
     */
    public DoWhile(String taskReferenceName, String expression) {
        super(taskReferenceName, TaskType.DO_WHILE);
        JsValidator.assertVariableName(taskReferenceName);
        if (Validator.isExpression(expression)) {
            this.input(LOOP_COUNT, expression);
            this.loopCondition = getForLoopCondition();
        } else {
            throw new IllegalArgumentException("expression should be a valid expression");
        }
    }

    public DoWhile(String taskReferenceName, PathExpression expression) {
        this(taskReferenceName, expression.getExpression());
    }

    /**
     * Similar to a for loop, run tasks for N times
     *
     * @param taskReferenceName
     * @param loopCount
     */
    public DoWhile(String taskReferenceName, int loopCount) {
        super(taskReferenceName, TaskType.DO_WHILE);
        this.loopCondition = getForLoopCondition(loopCount);
    }

    DoWhile(FlowTask workflowTask) {
        super(workflowTask);
        this.loopCondition = workflowTask.getLoopCondition();
        for (FlowTask task : workflowTask.getLoopOver()) {
            Task<?> loopTask = TaskRegistry.getTask(task);
            this.loopTasks.add(loopTask);
        }
    }

    public DoWhile childTask(Task<?> task) {
        this.loopTasks.add(task);
        return this;
    }

    private String getForLoopCondition(int loopCount) {
        if (loopCount < 0) {
            throw new IllegalArgumentException("loopCount must be greater than or equal to zero");
        }
        JsValidator.assertVariableName(getTaskReferenceName());
        return "if ( $." + getTaskReferenceName() + "['iteration'] < " + loopCount + ") { true; } else { false; }";
    }

    /**
     * 获取for循环遍历条件表达式
     *
     * @return
     */
    private String getForLoopCondition() {
        JsValidator.assertVariableName(getTaskReferenceName());
        return f("if ( $.%s['iteration'] < $.loopCount ) { true; } else { false; }", getTaskReferenceName());
    }

    /**
     * 设置循环条件表达式(目前仅支持javascript)
     *
     * @param condition
     * @return
     */
    public DoWhile setLoopCondition(String condition) {
        this.loopCondition = Objects.requireNonNull(condition);
        return this;
    }

    public String getLoopCondition() {
        return loopCondition;
    }

    public List<? extends Task> getLoopTasks() {
        return loopTasks;
    }

    @Override
    public void updateWorkflowTask(FlowTask workflowTask) {
        workflowTask.setLoopCondition(loopCondition);

        List<FlowTask> loopWorkflowTasks = new ArrayList<>();
        for (Task task : this.loopTasks) {
            loopWorkflowTasks.addAll(task.getWorkflowDefTasks());
        }
        workflowTask.setLoopOver(loopWorkflowTasks);
    }
}
