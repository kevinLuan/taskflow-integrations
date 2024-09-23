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
package cn.feiliu.taskflow.client.api;

import cn.feiliu.taskflow.common.metadata.tasks.TaskDefinition;
import static cn.feiliu.taskflow.client.api.BaseClientApi.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-07-24
 */
public class TaskDefClientTests {
    @Test
    public void test() {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setName("my_test_task");
        taskDefinition.setTitle("测试自定义任务");
        getTaskEngine().deleteTaskDef(taskDefinition.getName());
        Assert.assertTrue(getTaskEngine().createIfAbsent(taskDefinition));
        taskDefinition.setDescription("自定义任务描述");
        getTaskEngine().updateTaskDef(taskDefinition);
        TaskDefinition old = getTaskEngine().getTaskDef(taskDefinition.getName()).get();
        Assert.assertEquals(taskDefinition.getName(), old.getName());
        Assert.assertEquals(taskDefinition.getTitle(), old.getTitle());
        List<TaskDefinition> tasks = getTaskEngine().getTaskDefs();
        System.out.println("TaskDefSize:" + tasks.size());
        Assert.assertTrue(tasks.size() > 1);
        for (TaskDefinition task : tasks) {
            System.out.println("taskName: " + task.getName() + ", title:" + task.getTitle());
        }
        Assert.assertTrue(getTaskEngine().publishTaskDef(taskDefinition.getName()));
    }
}
