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
import cn.feiliu.taskflow.common.metadata.workflow.StartWorkflowRequest;
import cn.feiliu.taskflow.common.metadata.workflow.WorkflowDefinition;
import cn.feiliu.taskflow.common.run.ExecutingWorkflow;
import cn.feiliu.taskflow.open.dto.CorrelationIdsSearchRequest;
import cn.feiliu.taskflow.sdk.workflow.def.tasks.Http;
import cn.feiliu.taskflow.sdk.workflow.def.tasks.SetVariable;
import cn.feiliu.taskflow.sdk.workflow.def.tasks.Wait;
import cn.feiliu.taskflow.sdk.workflow.def.tasks.WorkTask;
import com.google.common.util.concurrent.Uninterruptibles;
import cn.feiliu.taskflow.client.util.TestUtil;
import org.junit.jupiter.api.Test;

import static cn.feiliu.taskflow.client.api.BaseClientApi.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class WorkflowBaseClientTests {
    @Test
    public void startWorkflow() {
        String name = "test-001-workflow";
        int version = 1;
        WorkflowDefinition workflowDefinition = WorkflowDefinition.newBuilder(name, version)
            .addTask(new SetVariable("x")).build();
        getApiClient().getWorkflowEngine().registerWorkflow(workflowDefinition, false);
        String workflowId = getWorkflowClient().startWorkflow(StartWorkflowRequest.of(name, version));
        ExecutingWorkflow executingWorkflow = getWorkflowClient().getWorkflow(workflowId, false);
        assertTrue(executingWorkflow.getWorkflowName().equals(name));
    }

    @Test
    public void testSearchByCorrelationIds() {
        List<String> correlationIds = new ArrayList<>();
        Set<String> workflowNames = new HashSet<>();
        Map<String, Set<String>> correlationIdToWorkflows = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            String correlationId = UUID.randomUUID().toString();
            correlationIds.add(correlationId);
            for (int j = 0; j < 5; j++) {
                WorkflowDefinition workflowDef = WorkflowDefinition.newBuilder("simple_workflow_" + j, 1)
                        .addTask(new Http("http").url("https://www.baidu.com"))
                        .build();
                getApiClient().getWorkflowEngine().registerWorkflow(workflowDef, false);
                StartWorkflowRequest request = new StartWorkflowRequest();
                workflowNames.add(workflowDef.getName());
                request.setName(workflowDef.getName());
                request.setCorrelationId(correlationId);
                String id = getWorkflowClient().startWorkflow(request);
                System.out.println("started workflowId:" + id + "   correlationId:" + correlationId);
                Set<String> ids = correlationIdToWorkflows.getOrDefault(correlationId, new HashSet<>());
                ids.add(id);
                correlationIdToWorkflows.put(correlationId, ids);
            }
        }
        // Let's give couple of seconds for indexing to complete
        Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
        CorrelationIdsSearchRequest request = new CorrelationIdsSearchRequest();
        request.setCorrelationIds(correlationIds);
        request.setWorkflowNames(new ArrayList<>(workflowNames));
        Map<String, List<ExecutingWorkflow>> result = getWorkflowClient().getWorkflowsByNamesAndCorrelationIds(true, false, request);
        assertNotNull(result);
        assertEquals(correlationIds.size(), result.size());
        for (String correlationId : correlationIds) {
            assertEquals(5, result.get(correlationId).size());
            Set<String> ids = result.get(correlationId).stream().map(ExecutingWorkflow::getWorkflowId)
                    .collect(Collectors.toSet());
            assertEquals(correlationIdToWorkflows.get(correlationId), ids);
        }
    }

    @Test
    public void testWorkflowTerminate() {
        String name = "test-003-workflow";
        int version = 1;
        WorkflowDefinition workflowDef = WorkflowDefinition.newBuilder(name, version)
            .addTask(new Wait("manual_approval_task_ref")).build();
        getApiClient().getWorkflowEngine().registerWorkflow(workflowDef, false);
        String workflowId = getWorkflowClient().startWorkflow(StartWorkflowRequest.of(name, version));
        System.out.println("testWorkflowTerminate 工作流ID: " + workflowId);
        getWorkflowClient().terminateWorkflow(workflowId, "testing out some stuff");
        ExecutingWorkflow executingWorkflow = getWorkflowClient().getWorkflow(workflowId, true);
        assertEquals(workflowDef.getTasks().get(0).getTaskReferenceName(), executingWorkflow.getTasks().get(0)
            .getReferenceTaskName());
        assertEquals(ExecutingWorkflow.WorkflowStatus.TERMINATED, executingWorkflow.getStatus());
        assertTrue(getWorkflowEngine().deleteWorkflowDef(name, version));
    }

    @Test
    public void testSkipTaskFromWorkflow() throws Exception {
        var workflowName = "random_workflow_name_a";
        var taskName1 = "random_task_name_a";
        var taskName2 = "random_task_name_b";

        var taskDef1 = new TaskDefinition(taskName1);
        taskDef1.setRetryCount(0);
        taskDef1.setOwnerEmail("your_email@abc.com");
        var taskDef2 = new TaskDefinition(taskName2);
        taskDef2.setRetryCount(0);
        taskDef2.setOwnerEmail("your_email@abc.com");

        TestUtil.retryMethodCall(
                () -> getTaskEngine().registerTaskDefs(List.of(taskDef1, taskDef2)));
        WorkflowDefinition workflowDef = WorkflowDefinition.newBuilder(workflowName,1)
                .addTask(new WorkTask(taskName1, taskName1))
                .addTask(new WorkTask(taskName2, taskName2))
                .build();
        getApiClient().getWorkflowEngine().registerWorkflow(workflowDef,true);
        StartWorkflowRequest startWorkflowRequest = new StartWorkflowRequest();
        startWorkflowRequest.setName(workflowName);
        startWorkflowRequest.setVersion(1);
        startWorkflowRequest.setInput(new HashMap<>());
        var workflowId = (String) TestUtil.retryMethodCall(
                () -> getWorkflowClient().startWorkflow(startWorkflowRequest));
        System.out.println("workflowId: " + workflowId);

        TestUtil.retryMethodCall(
                () -> getWorkflowClient().skipTaskFromWorkflow(workflowId, taskName2));
        TestUtil.retryMethodCall(
                () -> getWorkflowClient().terminateWorkflows(List.of(workflowId), "xxx"));
    }

    @Test
    public void testUpdateVariables() {
        WorkflowDefinition workflowDef = WorkflowDefinition.newBuilder("update_variable_test", 1)
            .addTask(new WorkTask("simple_task", "simple_task_ref"))
            .timeoutPolicy(WorkflowDefinition.TimeoutPolicy.TIME_OUT_WF).timeoutSeconds(60).build();
        getApiClient().getWorkflowEngine().registerWorkflow(workflowDef, false);
        StartWorkflowRequest request = new StartWorkflowRequest();
        request.setName(workflowDef.getName());
        request.setVersion(workflowDef.getVersion());
        request.setInput(Map.of());
        String workflowId = getWorkflowClient().startWorkflow(request);
        assertNotNull(workflowId);

        ExecutingWorkflow execution = getWorkflowClient().getWorkflow(workflowId, false);
        assertNotNull(execution);
        assertTrue(execution.getVariables().isEmpty());

        Map<String, Object> variables = Map.of("k1", "v1", "k2", 42, "k3", Arrays.asList(3, 4, 5));
        execution = getWorkflowClient().updateVariables(workflowId, variables);
        assertNotNull(execution);
        assertFalse(execution.getVariables().isEmpty());
        assertEquals(variables.get("k1"), execution.getVariables().get("k1"));
        assertEquals(variables.get("k2").toString(), execution.getVariables().get("k2").toString());
        assertEquals(variables.get("k3").toString(), execution.getVariables().get("k3").toString());

        Map<String, Object> map = new HashMap<>();
        map.put("k1", null);
        map.put("v1", "xyz");
        execution = getWorkflowClient().updateVariables(workflowId, map);
        assertNotNull(execution);
        assertFalse(execution.getVariables().isEmpty());
        assertEquals(null, execution.getVariables().get("k1"));
        assertEquals(variables.get("k2").toString(), execution.getVariables().get("k2").toString());
        assertEquals(variables.get("k3").toString(), execution.getVariables().get("k3").toString());
        assertEquals("xyz", execution.getVariables().get("v1").toString());
    }
}
