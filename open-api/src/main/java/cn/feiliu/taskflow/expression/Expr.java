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
package cn.feiliu.taskflow.expression;

/**
 * 表达式封装
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-09-08
 */
public class Expr {
    /**
     * 创建任务引用表达式
     *
     * @param taskRefName 任务引用名称
     * @return
     */
    public static TaskExpression task(String taskRefName) {
        return new TaskExpression(taskRefName);
    }

    /**
     * 创建工作流输入表达式
     *
     * @return
     */
    public static WorkflowExpression workflow() {
        return new WorkflowExpression();
    }
}
