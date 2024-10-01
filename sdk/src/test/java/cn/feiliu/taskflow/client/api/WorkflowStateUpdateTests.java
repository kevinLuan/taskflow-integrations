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

import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.common.metadata.tasks.ExecutingTask;
import cn.feiliu.taskflow.common.metadata.workflow.StartWorkflowRequest;
import cn.feiliu.taskflow.common.metadata.workflow.WorkflowDefinition;
import cn.feiliu.taskflow.common.metadata.workflow.FlowTask;
import cn.feiliu.taskflow.common.model.WorkflowRun;
import cn.feiliu.taskflow.common.run.ExecutingWorkflow;
import cn.feiliu.taskflow.open.dto.CreateWorkflowRequest;
import cn.feiliu.taskflow.open.dto.WorkflowProgressUpdate;
import cn.feiliu.taskflow.open.exceptions.ConflictException;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static cn.feiliu.taskflow.client.api.BaseClientApi.*;
import static org.junit.Assert.*;

import java.util.*;
import java.util.stream.Collectors;

public class WorkflowStateUpdateTests {
    private static ApiClient apiClient;
    final String             WORKFLOW_NAME = "test_workflow_status_updates";
    final int                VERSION       = 1;

    @BeforeClass
    public static void init() {
        apiClient = getApiClient();
    }

    @Before
    public void shouldRegisterWorkflow() {
        CreateWorkflowRequest request = new CreateWorkflowRequest();
        request.setName(WORKFLOW_NAME);
        request.setVersion(VERSION);
        request.setOwnerEmail("your_email@abc.com");
        request.setTimeoutSeconds(600);
        request.setTimeoutPolicy(WorkflowDefinition.TimeoutPolicy.TIME_OUT_WF);
        FlowTask workflowTask = new FlowTask();
        workflowTask.setName("wait_task");
        workflowTask.setTaskReferenceName("wait_task_ref");
        request.setTasks(List.of(workflowTask));
        request.setRegisterTask(true);
        apiClient.getWorkflowEngine().registerWorkflow(request, true);
    }

    @SneakyThrows
    private String startWorkflow() {
        return apiClient.getWorkflowEngine().start(StartWorkflowRequest.of(WORKFLOW_NAME, VERSION));
    }

    @Test
    public void test() {
        String workflowId = startWorkflow();
        WorkflowProgressUpdate request = new WorkflowProgressUpdate();
        WorkflowProgressUpdate.TaskRefUpdate taskRefUpdate = new WorkflowProgressUpdate.TaskRefUpdate();
        taskRefUpdate.setTaskReferenceName("wait_task_ref");
        taskRefUpdate.setOutputData(Map.of("hello", "kitty"));
        request.setWorkflowId(workflowId);
        request.setTaskRefUpdate(taskRefUpdate);
        request.setVariables(Map.of("hello", "world"));
        //更新任务一完成状态
        apiClient.getWorkflowClient().updateWorkflow(request);
        //增加等待任务
        request.setWaitUntilTaskRefNames(List.of("wait_task_ref_1", "wait_task_ref_2"));
        request.setWaitForSeconds(6);
        WorkflowRun workflowRun = apiClient.getWorkflowClient().updateWorkflow(request);
        System.out.println(workflowRun.getTasks()
                .stream()
                .map(task -> task.getReferenceTaskName() + ":" + task.getStatus())
                .collect(Collectors.toList()));
        //更新任务二完成状态
        taskRefUpdate.setTaskReferenceName("wait_task_ref_2");
        request.setTaskRefUpdate(taskRefUpdate);
        workflowRun = apiClient.getWorkflowClient().updateWorkflow(request);

        assertEquals(ExecutingWorkflow.WorkflowStatus.COMPLETED, workflowRun.getStatus());
        Set<ExecutingTask.Status> allTaskStatus = workflowRun.getTasks()
                .stream()
                .map(t -> t.getStatus())
                .collect(Collectors.toSet());
        assertEquals(1, allTaskStatus.size());
        assertEquals(ExecutingTask.Status.COMPLETED, allTaskStatus.iterator().next());

        System.out.println(workflowRun.getStatus());
        System.out.println(workflowRun.getTasks()
                .stream()
                .map(task -> task.getReferenceTaskName() + ":" + task.getStatus())
                .collect(Collectors.toList()));

    }

    @Test
    public void testIdempotency() {
        StartWorkflowRequest request = StartWorkflowRequest.newBuilder().name(WORKFLOW_NAME).version(VERSION)
            .idempotencyKey(UUID.randomUUID().toString())
            .idempotencyStrategy(StartWorkflowRequest.IdempotencyStrategy.FAIL).build();
        String workflowId = apiClient.getWorkflowClient().startWorkflow(request);
        //返回已存在的工作流ID
        request.setIdempotencyStrategy(StartWorkflowRequest.IdempotencyStrategy.RETURN_EXISTING);
        String workflowId2 = apiClient.getWorkflowClient().startWorkflow(request);
        assertEquals(workflowId, workflowId2);
        //重复提交工作流抛出异常
        request.setIdempotencyStrategy(StartWorkflowRequest.IdempotencyStrategy.FAIL);
        try {
            apiClient.getWorkflowClient().startWorkflow(request);
            fail("未出现逾期结果");
        } catch (ConflictException ce) {
            assertTrue(true);
        }
    }
}
