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

import cn.feiliu.common.api.utils.CommonUtils;
import cn.feiliu.taskflow.common.enums.TaskUpdateStatus;
import cn.feiliu.taskflow.common.metadata.workflow.WorkflowDefinition;
import cn.feiliu.taskflow.sdk.workflow.def.tasks.SimpleTask;
import cn.feiliu.taskflow.open.api.IWorkflowService;
import cn.feiliu.taskflow.common.metadata.tasks.ExecutingTask;
import cn.feiliu.taskflow.common.metadata.tasks.TaskDefinition;
import cn.feiliu.taskflow.common.metadata.tasks.TaskLog;
import cn.feiliu.taskflow.common.metadata.workflow.StartWorkflowRequest;
import cn.feiliu.taskflow.common.run.ExecutingWorkflow;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static cn.feiliu.common.api.utils.CommonUtils.f;
import static cn.feiliu.taskflow.client.api.BaseClientApi.*;
import static org.junit.Assert.*;

public class TaskBaseClientTests {

    private static ITaskClient      taskClient;
    private static IWorkflowService workflowClient;

    @BeforeClass
    public static void setup() throws IOException {
        taskClient = getApiClient().getApis().getTaskClient();
        workflowClient = getApiClient().getApis().getWorkflowClient();

    }

    @Test
    public void testTaskLog() throws Exception {
        String workflowName = "sdk-workflow";
        String taskName = "noWorkTask2";
        getApiClient().getApis().getTaskEngine().createIfAbsent(new TaskDefinition(taskName));
        WorkflowDefinition workflowDef = WorkflowDefinition.newBuilder(workflowName, 1)
            .addTask(new SimpleTask(taskName, taskName + "Ref")).build();
        assertTrue(getApiClient().getApis().getWorkflowEngine().registerWorkflow(workflowDef, true));

        String workflowId = workflowClient.startWorkflow(StartWorkflowRequest.of(workflowName, 1));
        System.out.println("Started workflow with id: " + workflowId);
        ExecutingTask task = taskClient.pollTask(taskName, "junit.test", null);
        assertEquals(taskName, task.getTaskDefName());
        taskClient.logMessageForTask(task.getTaskId(), "random message");
        List<TaskLog> logs = taskClient.getTaskLogs(task.getTaskId());
        assertNotNull(logs);
        ExecutingTask details = taskClient.getTaskDetails(task.getTaskId());
        assertEquals(taskName, details.getTaskDefName());
        System.out.println(taskClient.requeuePendingTasksByTaskType(taskName));
        updateWorkflowTask(workflowId);
        pullTaskAndUpdateTask(taskName);
        getApiClient().getApis().getTaskEngine().deleteTaskDef(taskName);
        getApiClient().getApis().getWorkflowEngine().deleteWorkflowDef(workflowName, 1);
    }

    private static void pullTaskAndUpdateTask(String taskName) {
        List<ExecutingTask> tasks = taskClient.batchPollTasksByTaskType(taskName, "junit.test", 10, 300);
        for (ExecutingTask executingTask : tasks) {
            Map<String, Object> map = Map.of("test", "更新方式2");
            taskClient.updateTask(executingTask.getWorkflowInstanceId(), executingTask.getReferenceTaskName(),
                TaskUpdateStatus.COMPLETED, map);
        }
    }

    /***
     * 根据任务名称更新工作任务状态
     * @param workflowId
     */
    private static void updateWorkflowTask(String workflowId) {
        ExecutingWorkflow workflow = workflowClient.getWorkflow(workflowId, true);
        int maxLoop = 10;
        int count = 0;
        while (!workflow.getStatus().isTerminal() && count < maxLoop) {
            for (ExecutingTask executingTask : workflow.getTasks()) {
                if (!executingTask.getStatus().isTerminal()) {
                    String referenceName = executingTask.getReferenceTaskName();
                    Map<String, Object> map = Map.of("hello_world", new Date());
                    System.out.println(f("更新任务 taskId: %s, taskName: %s", executingTask.getTaskId(),
                        executingTask.getTaskDefName()));
                    taskClient.updateTask(workflowId, referenceName, TaskUpdateStatus.COMPLETED, map);
                }
            }
            count++;
            CommonUtils.sleepUninterruptibly(1, TimeUnit.SECONDS);
            workflow = workflowClient.getWorkflow(workflowId, true);
        }
        assertTrue(count < maxLoop);
        workflow = workflowClient.getWorkflow(workflowId, true);
        assertEquals(ExecutingWorkflow.WorkflowStatus.COMPLETED, workflow.getStatus());
    }
}
