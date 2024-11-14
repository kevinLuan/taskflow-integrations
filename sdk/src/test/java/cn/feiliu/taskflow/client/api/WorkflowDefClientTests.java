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
package cn.feiliu.taskflow.client.api;

import static cn.feiliu.taskflow.client.api.BaseClientApi.*;
import cn.feiliu.taskflow.common.metadata.tasks.TaskDefinition;
import cn.feiliu.taskflow.common.metadata.workflow.FlowTask;
import cn.feiliu.taskflow.common.metadata.workflow.WorkflowDefinition;

import static org.junit.Assert.*;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-07-25
 */
public class WorkflowDefClientTests {
    private WorkflowDefinition getWorkflowDef() {
        WorkflowDefinition workflowDef = new WorkflowDefinition();
        workflowDef.setName("test_123_workflow");
        workflowDef.setVersion(1);
        workflowDef.setOwnerEmail("your_email@abc.com");
        workflowDef.setTimeoutSeconds(600);
        workflowDef.setTimeoutPolicy(WorkflowDefinition.TimeoutPolicy.TIME_OUT_WF);
        FlowTask workflowTask = new FlowTask();
        workflowTask.setName("test_task");
        workflowTask.setTaskReferenceName("testTaskRef");
        workflowDef.setTasks(List.of(workflowTask));
        return workflowDef;
    }

    @Test
    public void test() {
        WorkflowDefinition workflowDef = getWorkflowDef();
        getWorkflowEngine().deleteWorkflowDef(workflowDef.getName(), workflowDef.getVersion());
        registerTaskDef(workflowDef);
        assertTrue(getWorkflowEngine().createIfAbsent(workflowDef));
        assertTrue(getWorkflowEngine().updateWorkflowDef(workflowDef));
        assertTrue(getWorkflowEngine().publishWorkflowDef(workflowDef.getName(), workflowDef.getVersion(), true));
        WorkflowDefinition workflowDefinition = getWorkflowEngine().getWorkflowDef(workflowDef.getName(),
            workflowDef.getVersion());
        assertNotNull(workflowDefinition);
        unregisterTaskDef(workflowDef);
        Assert.assertTrue(getWorkflowEngine().deleteWorkflowDef(workflowDef.getName(), workflowDef.getVersion()));
    }

    private void registerTaskDef(WorkflowDefinition workflowDef) {
        for (FlowTask task : workflowDef.getTasks()) {
            TaskDefinition taskDefinition = new TaskDefinition();
            taskDefinition.setName(task.getName());
            getTaskEngine().registerTaskDefs(Lists.newArrayList(taskDefinition));
        }
    }

    private void unregisterTaskDef(WorkflowDefinition workflowDef) {
        for (FlowTask task : workflowDef.getTasks()) {
            getTaskEngine().deleteTaskDef(task.getName());
        }
    }
}
