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
package cn.feiliu.taskflow.client.sdk;

import static cn.feiliu.taskflow.client.api.BaseClientApi.*;

import cn.feiliu.common.api.utils.MapBuilder;
import cn.feiliu.taskflow.client.api.BaseClientApi;
import cn.feiliu.taskflow.client.core.TaskEngine;
import cn.feiliu.taskflow.core.def.tasks.DoWhile;
import cn.feiliu.taskflow.core.def.tasks.For;
import cn.feiliu.taskflow.core.def.tasks.ForkFor;
import cn.feiliu.taskflow.core.def.tasks.SimpleTask;
import cn.feiliu.taskflow.core.executor.task.Worker;
import cn.feiliu.taskflow.core.task.InputParam;
import cn.feiliu.taskflow.core.task.WorkerTask;
import cn.feiliu.taskflow.dto.run.ExecutingWorkflow;
import cn.feiliu.taskflow.dto.tasks.TaskDefinition;
import cn.feiliu.taskflow.dto.workflow.WorkflowDefinition;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-08-20
 */
public class AnalysisTest {
    final static String      testRandomItems = "testRandomItems";
    final static String      testEchoAny     = "testEchoAny";
    private static MyWorkers myWorkers       = new MyWorkers();

    private static void addWorkerAndPollingTasks() {
        TaskEngine workerExecutor = getApiClient().getApis().getTaskEngine().addWorkers(myWorkers).initWorkerTasks()
            .startRunningTasks();
        for (Worker worker : workerExecutor.getWorkers()) {
            System.out.println("worker:" + worker.getTaskDefName());
        }

    }

    @BeforeClass
    public static void testRegisterTasks() {
        addWorkerAndPollingTasks();
        String[] taskNames = { testRandomItems, testEchoAny };
        for (String name : taskNames) {
            TaskDefinition taskDef = new TaskDefinition();
            taskDef.setName(name);
            taskDef.setRetryCount(3);
            taskDef.setPollTimeoutSeconds(15);
            taskDef.setDescription("单元测试任务");
            getTaskEngine().createIfAbsent(taskDef);
            System.out.println("Register taskDef: " + name);
        }
    }

    @Test
    @SneakyThrows
    public void testAdvancedMultiLoopOver() {
        WorkflowDefinition workflowDef = WorkflowDefinition
            .newBuilder("test_advanced_multi_loop_over_workflow", 2)
            //
            .addTask(
                new ForkFor("forkForRef", "${workflow.input.items}")//
                    .childTask(new SimpleTask(testEchoAny, "simpleTask0Ref")//
                        .input("msg", "${forkForRef.output.element}"))//
                    .childTask(new SimpleTask(testRandomItems, "randomItems1Ref")//
                        .input("status", true))//
                    .childTask(
                        new For("for1Ref", "${randomItems1Ref.output.result}")//
                            .childTask(new SimpleTask(testEchoAny, "simple1Ref")//
                                .input("msg", "${for1Ref.output.element}"))//
                            .childTask(new SimpleTask(testRandomItems, "randomItems2Ref")//
                                .input("status", false))//
                            .childTask(
                                new For("for2Ref", "${randomItems2Ref.output.result}")//
                                    .childTask(new SimpleTask(testEchoAny, "simple2Ref")//
                                        .input("msg", "${for2Ref.output.element}"))//
                                    .childTask(
                                        new DoWhile("doWhileRef", "${workflow.input.loopCount}")//
                                            .childTask(new SimpleTask(testEchoAny, "simple3Ref").input("msg",
                                                "${workflow.input.taskflow}")))))//
            ).build();
        Assert.assertTrue(getApiClient().getApis().getWorkflowEngine().registerWorkflow(workflowDef, true));
        Map<String, Object> reqData = MapBuilder.newBuilder().put("items", new Integer[] { 1, 2 }).put("loopCount", 2)
            .put("taskflow", "任务云平台 --> http://www.taskflow.cn").build();
        doExecute(workflowDef, reqData, 120);
        Assert.assertTrue(getWorkflowEngine().deleteWorkflowDef(workflowDef.getName(), workflowDef.getVersion()));
    }

    @AfterClass
    public static void destroy() {
        String[] taskNames = { testRandomItems, testEchoAny };
        for (String taskName : taskNames) {
            getTaskEngine().deleteTaskDef(taskName);
        }
    }

    @SneakyThrows
    private void doExecute(WorkflowDefinition workflowDef, Map<String, Object> dataMap, int timeout) {
        String workflowId = getApiClient().getApis().getWorkflowEngine().start(workflowDef, dataMap);
        ExecutingWorkflow executingWorkflow = BaseClientApi.waitForTerminal(workflowId, timeout);
        System.out.println("workflowId: " + executingWorkflow.getWorkflowId());
        Assert.assertEquals(ExecutingWorkflow.WorkflowStatus.COMPLETED, executingWorkflow.getStatus());
    }

    public static class MyWorkers {
        @WorkerTask(value = testEchoAny, threadCount = 10)
        public String echoAny(@InputParam("msg") Object msg) {
            System.out.println(String.format("echoAny: `%s`", msg));
            return "echoAny: " + msg;
        }

        @WorkerTask(value = testRandomItems, threadCount = 10)
        public List<String> randomItems(@InputParam("status") Boolean status) {
            if (status) {
                return Lists.newArrayList("AA", "BB");
            } else {
                return Lists.newArrayList("任务云", "平台");
            }
        }
    }

}
