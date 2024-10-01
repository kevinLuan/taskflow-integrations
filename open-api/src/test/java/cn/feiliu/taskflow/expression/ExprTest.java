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

import org.junit.Test;

import static cn.feiliu.taskflow.expression.Expr.*;
import static org.junit.Assert.*;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-09-08
 */
public class ExprTest {
    @Test
    public void test() {
        assertEquals("${workflow.input.x}", workflow().input.get("x").getExpression());
        assertEquals("${workflow.input}", workflow().input.get().getExpression());
        assertEquals("${x.input}", task("x").input.getExpression());
        assertEquals("${x.input.x}", task("x").input.get("x").getExpression());
        assertEquals("${x.input.x.y}", task("x").input.get("x").get("y").getExpression());
        assertEquals("${x.input.x.y[0].z}", task("x").input.path("x", "y").index(0).get("z").getExpression());
        assertEquals("${x.output}", task("x").output.getExpression());
        assertEquals("${x.output.x}", task("x").output.get("x").getExpression());
        assertEquals("${x.output.x.y}", task("x").output.path("x", "y").getExpression());
        assertEquals("${x.output.x.y[0].z}", task("x").output.path("x", "y").index(0).get("z").getExpression());
        assertEquals("${x.output.x.y[1].z}", task("x").output.path("x", "y[1]", "z").getExpression());
    }
}
