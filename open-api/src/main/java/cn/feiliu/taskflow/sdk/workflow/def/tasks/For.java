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

/**
 * for 循环遍历表达式: ${workflow.input.elements}
 * 循环体获取元素: ${eachNameRef.output.element}
 * 循环体获取索引下标: ${eachNameRef.output.index}
 * <pre>
 * -------------------------------------
 * | for(int i=0;i<$elements.size;i++) |
 * -------------------------------------
 * |循环调用任务节点处理                  |
 * task1($elements[i],$index)          |
 * |                                   |
 * task2($elements[i],$index)          |
 * |                                   |
 * |-----------------------------------|
 * |                END                |
 * |-----------------------------------|
 * </pre>
 */

public class For extends Task<For> implements ILoopTask<For> {
    public static final String  ELEMENTS  = "elements";
    private String              loopCondition;
    private final List<Task<?>> loopTasks = new ArrayList<>();

    /**
     * Execute tasks in a loop determined by the condition set using condition parameter. The loop
     * will continue till the condition is true
     *
     * @param taskReferenceName
     * @param eachExpression    根据循环类型确定参数名称及数据类型
     *                          例如:
     *                          elements: ${workflow.input.elements} -- 接收数组元素
     */
    public For(String taskReferenceName, String eachExpression) {
        super(taskReferenceName, TaskType.FOR_EACH);
        JsValidator.assertVariableName(taskReferenceName);
        if (Validator.isExpression(eachExpression)) {
            this.input(ELEMENTS, eachExpression);
            this.loopCondition = String.format("if($.%s['iteration'] < $.loopCount){ true; }else{ false; }",
                getTaskReferenceName());
        } else {
            throw new IllegalArgumentException("expression should be a valid expression");
        }
    }

    public For(String taskReferenceName, PathExpression expression) {
        this(taskReferenceName, expression.getExpression());
    }

    For(FlowTask workflowTask) {
        super(workflowTask);
        this.loopCondition = workflowTask.getLoopCondition();
        for (FlowTask task : workflowTask.getLoopOver()) {
            Task<?> loopTask = TaskRegistry.getTask(task);
            this.loopTasks.add(loopTask);
        }
    }

    public For childTask(Task<?> task) {
        this.loopTasks.add(task);
        return this;
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
