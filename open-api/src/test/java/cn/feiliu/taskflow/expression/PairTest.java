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

import cn.feiliu.taskflow.common.metadata.tasks.TaskType;
import cn.feiliu.taskflow.sdk.workflow.def.tasks.DoWhile;
import cn.feiliu.taskflow.sdk.workflow.def.tasks.WorkTask;
import cn.feiliu.taskflow.sdk.workflow.def.tasks.Task;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-09-08
 */
public class PairTest {
    @Test
    public void testExpression() {
        assertEquals("${workflow.input.a.b}", Expr.workflow().input.path("a", "b").getExpression());
        assertEquals("${myTaskRef.input}", Expr.task("myTaskRef").input.getExpression());
        assertEquals("${x.input.a}", Expr.task("x").input.get("a").getExpression());
        assertEquals("${x.output}", Expr.task("x").output.getExpression());
        assertEquals("${x.output.z}", Expr.task("x").output.get("z").getExpression());

        String[] kv = Pair.of("x").fromTaskInput("c", "s");
        assertEquals("x", kv[0]);
        assertEquals("${c.input.s}", kv[1]);
    }

    @Test
    public void testWorkflow() {
        Task<?> task = new WorkTask("add", "addRef")//
            .input(Pair.of("a").fromWorkflow())//
            .input(Pair.of("b").fromWorkflow("b"));
        assertEquals("${workflow.input}", task.getInput().get("a"));
        assertEquals("${workflow.input.b}", task.getInput().get("b"));
    }

    @Test
    public void testInputAndOut() {
        Task<?> task = new WorkTask("subtract", "subtract1Ref")//
            .input(Pair.of("a").fromTaskOutput("addRef", "sum"))//
            .input(Pair.of("b").fromTaskInput("x", "B"))//
            .input(Pair.of("c").fromTaskInput("x"))//
            .input(Pair.of("d").fromTaskOutput("x"))//
            .input("x", 1);
        assertEquals("${addRef.output.sum}", task.getInput().get("a"));
        assertEquals("${x.input.B}", task.getInput().get("b"));
        assertEquals("${x.input}", task.getInput().get("c"));
        assertEquals("${x.output}", task.getInput().get("d"));
    }

    @Test
    public void test() {
        List<Task<?>> tasks = new ArrayList<>();
        tasks.add(new WorkTask("add", "addRef")//
            .input(Pair.of("a").fromWorkflow("a"))//
            .input(Pair.of("b").fromWorkflow("b")));
        tasks.add(new DoWhile("doWhile1Ref", 1)//
            .childTask(new WorkTask("subtract", "subtract1Ref")//
                .input(Pair.of("a").fromTaskOutput("addRef", "sum"))//
                .input(Pair.of("b").fromTaskInput("x", "B"))//
                .input("b", 1))//
            .childTask(new WorkTask("multiply", "multiply1Ref")//
                .input(Pair.of("a").fromTaskOutput("subtract1Ref", "result"))//
                .input("b", 2)));
        tasks.add(new DoWhile("doWhile2Ref", "${workflow.input.loopCount}")//
            .childTask(new WorkTask("subtract", "subtract2Ref")//
                .input(Pair.of("a").fromTaskOutput("addRef", "sum"))//
                .input(Pair.of("b").fromWorkflow("b")))//
            .childTask(new WorkTask("multiply", "multiply2Ref")//
                .input(Pair.of("a").fromTaskOutput("subtract2Ref", "result"))//
                .input("b", 2)));
        tasks.add(new WorkTask("divide", "divideRef")//
            .input(Pair.of("a").fromTaskOutput("addRef", "sum"))//
            .input("b", 2));

        for (Task<?> task : tasks) {
            System.out.println(task.getName() + "\t" + task.getInput());
            if (task.getType() == TaskType.DO_WHILE) {
                for (Task loopTask : ((DoWhile) task).getLoopTasks()) {
                    System.out.println(loopTask.getName() + "\t\t" + loopTask.getInput());
                }
            }
        }
    }
}
