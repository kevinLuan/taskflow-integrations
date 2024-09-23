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
package cn.feiliu.taskflow.client.sdk;

import static cn.feiliu.taskflow.client.api.BaseClientApi.*;

import cn.feiliu.taskflow.common.metadata.tasks.TaskDefinition;
import cn.feiliu.taskflow.sdk.workflow.def.tasks.DoWhile;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-08-24
 */
public class TaskDefTests {
    @Test
    public void testInvalidVariableName() {
        String[] invalidNames = { "a-b_c", "12332", "2.sd", "323" };
        for (String name : invalidNames) {
            try {
                new DoWhile(name, 1);
                fail("未出现逾期异常");
            } catch (IllegalArgumentException e) {
                assertEquals("The taskReferenceName: '" + name + "' parameter is invalid", e.getMessage());
            }
        }
    }

    @Test
    public void testDeleteTaskDef() {
        TaskDefinition taskDef = new TaskDefinition();
        taskDef.setName("task_hello_kitty_" + ThreadLocalRandom.current().nextInt(1000));
        assertTrue(getTaskEngine().createIfAbsent(taskDef));
        getTaskEngine().publishTaskDef(taskDef.getName());
        assertTrue(getTaskEngine().deleteTaskDef(taskDef.getName()));
        assertTrue(getTaskEngine().getTaskDef(taskDef.getName()).isEmpty());
    }
}
