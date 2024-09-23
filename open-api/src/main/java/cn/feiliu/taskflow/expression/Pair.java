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
package cn.feiliu.taskflow.expression;

import lombok.Getter;

import java.util.Objects;

/**
 * 参数对
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-09-08
 */
@Getter
public class Pair {
    /*参数名称*/
    private final String   name;
    /*参数value表达式*/
    private PathExpression value;

    private Pair(String taskParameterName) {
        this.name = Objects.requireNonNull(taskParameterName);
    }

    /**
     * @param taskParameterName 参数名称
     * @return
     */
    public static Pair of(String taskParameterName) {
        return new Pair(taskParameterName);
    }

    /**
     * 获取工作流输入原始参数的表达式
     *
     * @return
     */
    public String[] fromWorkflow() {
        this.value = Expr.workflow().input();
        return getResult();
    }

    /**
     * 获取工作流输入参数的表达式
     *
     * @return
     */
    public String[] fromWorkflow(String... paths) {
        this.value = Expr.workflow().input.path(paths);
        return getResult();
    }

    /**
     * 获取任务输出的表达式
     *
     * @param taskRefName 任务引用名称
     * @param paths    字段名称
     * @return
     */
    public String[] fromTaskOutput(String taskRefName, String... paths) {
        this.value = Expr.task(taskRefName).output.path(paths);
        return getResult();
    }

    /**
     * 获取任务输出的表达式
     *
     * @param taskRefName 任务引用名称
     * @param resultName  任务返回名称
     * @return
     */
    public String[] fromTaskOutput(String taskRefName, String resultName) {
        this.value = Expr.task(taskRefName).output.get(resultName);
        return getResult();
    }

    /**
     * 从任务输出获取整个对象的表达式
     *
     * @param taskRefName 任务引用名称
     * @return
     */
    public String[] fromTaskOutput(String taskRefName) {
        this.value = Expr.task(taskRefName).output;
        return getResult();
    }

    /**
     * 从任务输入获取整个对象的表达式
     *
     * @param taskRefName 任务引用名称
     * @return
     */
    public String[] fromTaskInput(String taskRefName) {
        this.value = Expr.task(taskRefName).input;
        return getResult();
    }

    /**
     * 从任务输入获取整个对象的表达式
     *
     * @param taskRefName 任务引用名称
     * @param fieldName 任务字段名称(变量名称)
     * @return
     */
    public String[] fromTaskInput(String taskRefName, String fieldName) {
        this.value = Expr.task(taskRefName).input.get(fieldName);
        return getResult();
    }

    /**
     * 从任务输入获取数据的表达式
     *
     * @param taskRefName 任务引用名称
     * @param paths    字段名称
     * @return
     */
    public String[] fromTaskInput(String taskRefName, String... paths) {
        this.value = Expr.task(taskRefName).input.path(paths);
        return getResult();
    }

    private String[] getResult() {
        return new String[] { name, value.getExpression() };
    }
}
