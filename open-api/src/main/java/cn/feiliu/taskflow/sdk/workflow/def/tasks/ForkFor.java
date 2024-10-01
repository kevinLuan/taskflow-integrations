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
import cn.feiliu.taskflow.common.utils.JsValidator;
import cn.feiliu.taskflow.common.utils.Validator;
import cn.feiliu.taskflow.expression.PathExpression;
import cn.feiliu.taskflow.sdk.workflow.def.ILoopTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 示例数据:elements: [A,B,C]
 * 遍历数组原数，并行调用任务
 * <pre>
 * -----------------------------
 * |          fork-for         |
 * -----------------------------
 * |             |             |
 * task-1('A')   task-1('B')   task-1('C')
 * |             |             |
 * task-2('A')   task-2('B')   task-2('C')
 * |             |             |
 * -----------------------------
 * |           END            |
 * -----------------------------
 * </pre>
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-09-08
 */
public class ForkFor extends Task<ForkFor> implements ILoopTask<ForkFor> {
    public static final String  ELEMENTS  = "elements";
    private final List<Task<?>> loopTasks = new ArrayList<>();

    /**
     * Execute tasks in a loop determined by the condition set using condition parameter. The loop
     * will continue till the condition is true
     *
     * @param taskReferenceName
     * @param eachExpression    根据循环类型确定参数名称及数据类型
     *                          例如:
     *                          elements: ${workflow.input.elements} -- 遍历数组
     */
    public ForkFor(String taskReferenceName, String eachExpression) {
        super(taskReferenceName, TaskType.FORK_FOR_EACH);
        JsValidator.assertVariableName(taskReferenceName);
        if (Validator.isExpression(eachExpression)) {
            this.input(ELEMENTS, eachExpression);
        } else {
            throw new IllegalArgumentException("expression should be a valid expression");
        }
    }

    public ForkFor(String taskReferenceName, PathExpression eachExpression) {
        this(taskReferenceName, eachExpression.getExpression());
    }

    ForkFor(FlowTask workflowTask) {
        super(workflowTask);
        for (FlowTask task : workflowTask.getLoopOver()) {
            Task<?> loopTask = TaskRegistry.getTask(task);
            this.loopTasks.add(loopTask);
        }
    }

    public ForkFor childTask(Task<?> task) {
        this.loopTasks.add(task);
        return this;
    }

    @Override
    public void updateWorkflowTask(FlowTask workflowTask) {
        List<FlowTask> loopWorkflowTasks = new ArrayList<>();
        for (Task task : this.loopTasks) {
            loopWorkflowTasks.addAll(task.getWorkflowDefTasks());
        }
        workflowTask.setLoopOver(loopWorkflowTasks);
    }
}
