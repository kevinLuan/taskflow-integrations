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
package cn.feiliu.taskflow.sdk.workflow.def;

import cn.feiliu.taskflow.expression.Expr;
import cn.feiliu.taskflow.sdk.workflow.def.tasks.Task;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-09-17
 */
public interface ILoopTask<LoopTask extends ILoopTask> {
    /**
     * 添加子任务
     *
     * @param task
     * @return
     */
    LoopTask childTask(Task<?> task);

    /**
     * 批量添加子任务
     *
     * @param tasks
     * @return
     */
    default LoopTask childTasks(Task<?>... tasks) {
        for (Task<?> task : tasks) {
            childTask(task);
        }
        return (LoopTask) this;
    }

    /**
     * 获取任务引用名称
     *
     * @return
     */
    String getTaskReferenceName();

    /**
     * 获取循环节点element表达式
     *
     * @return
     */
    default String getElementExpression() {
        return Expr.task(getTaskReferenceName()).output.get("element").getExpression();
    }

    /**
     * 获取index表达式
     *
     * @return
     */
    default String getIndexExpression() {
        return Expr.task(getTaskReferenceName()).output.get("index").getExpression();
    }

}
