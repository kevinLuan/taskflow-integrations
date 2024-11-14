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
import static org.junit.Assert.assertTrue;

import cn.feiliu.taskflow.common.metadata.tasks.TaskDefinition;
import cn.feiliu.taskflow.common.metadata.workflow.WorkflowDefinition;
import cn.feiliu.taskflow.common.run.ExecutingWorkflow;
import cn.feiliu.taskflow.sdk.worker.Worker;
import cn.feiliu.taskflow.sdk.workflow.def.tasks.*;
import cn.feiliu.taskflow.sdk.workflow.task.InputParam;
import cn.feiliu.taskflow.sdk.workflow.task.WorkerTask;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.junit.*;

import java.util.*;

public class WorkflowSDKTests {
    final static String SIMPLE_TASK = "simple_task", RANDOM_ITEMS = "random_items";

    @BeforeClass
    public static void registerTasks() {
        System.out.println("------------Register tasks start------------");
        String[] taskNames = { SIMPLE_TASK, RANDOM_ITEMS };
        for (String name : taskNames) {
            TaskDefinition taskDef = new TaskDefinition();
            taskDef.setName(name);
            taskDef.setRetryCount(3);
            taskDef.setPollTimeoutSeconds(15);
            taskDef.setDescription("单元测试任务");
            getTaskEngine().createIfAbsent(taskDef);
        }
        System.out.println("------------Register tasks done------------");
        getTaskEngine().addWorkers(new MyWorkers());
        getTaskEngine().initWorkerTasks();
        //dump log
        for (Worker worker : getTaskEngine().getWorkers()) {
            System.out.println("worker:" + worker.getTaskDefName());
        }
        getTaskEngine().startRunningTasks();
    }

    @Test
    public void testForkFor() {
        WorkflowDefinition workflowDef = WorkflowDefinition.newBuilder("sdk_fork_for_workflow", 1)
            .addTask(new SetVariable("setVar").input("items", Lists.newArrayList("AB", "CD")))
            //
            .addTask(new SimpleTask(SIMPLE_TASK, "simpleTaskRef").input("name", "第一个任务"))
            //
            .addTask(new ForkFor("forkFor1Ref", "${setVar.input.items}")//
                .childTask(new SimpleTask(SIMPLE_TASK, "loopSimpleTaskRef")//
                    .input("name", "${forkFor1Ref.output.element}")))
            //
            .addTask(new SimpleTask(SIMPLE_TASK, "simpleTaskRef3").input("name", "我三个任务"))
            .addTask(new ForkFor("forkFor2Ref", "${workflow.input.elements}")//
                .childTask(new SimpleTask(SIMPLE_TASK, "forkForLoopSimpleRef")//
                    .input("name", "${forkFor2Ref.output.element}"))).build();
        assertTrue(getApiClient().getApis().getWorkflowEngine().registerWorkflow(workflowDef, true));
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("name", "欢迎访问任务云平台 http://www.taskflow.cn");
        dataMap.put("elements", Lists.newArrayList("欢迎访问", "任务云平台"));
        doExecute(workflowDef, dataMap, 15);
        deleteWorkflowDef(workflowDef);
    }

    private void deleteWorkflowDef(WorkflowDefinition workflowDef) {
        assertTrue(getWorkflowEngine().deleteWorkflowDef(workflowDef.getName(), workflowDef.getVersion()));
    }

    @Test
    public void testMultiForkFor() {
        WorkflowDefinition workflowDef = WorkflowDefinition
            .newBuilder("sdk_multi_fork_for_workflow", 2)
            .addTask(
                new ForkFor("forkFor1Ref", "${workflow.input.items1}")//
                    .childTask(new SimpleTask(SIMPLE_TASK, "simple1Ref")//
                        .input("name", "${forkFor1Ref.output.element}")).childTask(
                        new ForkFor("forkFor2Ref", "${workflow.input.items2}")//
                            .childTask(new SimpleTask(SIMPLE_TASK, "simple2Ref")//
                                .input("name", "${forkFor2Ref.output.element}"))))//
            .build();
        assertTrue(getApiClient().getApis().getWorkflowEngine().registerWorkflow(workflowDef, true));
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("items1", Lists.newArrayList("A"));
        dataMap.put("items2", Lists.newArrayList("欢迎访问任务云平台"));
        doExecute(workflowDef, dataMap, 15);
        deleteWorkflowDef(workflowDef);
    }

    /**
     * 验证多层嵌套ForkFor场景，ForkFor参数引用来自父任务
     */
    @Test
    public void testMultiForkForTaskRef() {
        WorkflowDefinition workflowDef = WorkflowDefinition
            .newBuilder("sdk_multi_fork_for_taskRef_workflow", 2)
            .addTask(
                new ForkFor("forkFor0Ref", "${workflow.input.items}")//
                    .childTask(
                        new SimpleTask(SIMPLE_TASK, "simpleTask0Ref").input("name", "${forkFor0Ref.output.element}"))//
                    .childTask(new SimpleTask(RANDOM_ITEMS, "randomItems1Ref").input("status", true))
                    //
                    .childTask(
                        new ForkFor("forkFor1Ref", "${randomItems1Ref.output.result}")
                            //
                            .childTask(
                                new SimpleTask(SIMPLE_TASK, "simple1Ref")
                                    .input("name", "${forkFor1Ref.output.element}"))//
                            .childTask(new SimpleTask(RANDOM_ITEMS, "randomItems2Ref").input("status", false))//
                            .childTask(
                                new ForkFor("forkFor2Ref", "${randomItems2Ref.output.result}")//
                                    .childTask(new SimpleTask(SIMPLE_TASK, "simple2Ref").input("name",
                                        "${forkFor2Ref.output.element}")))))//
            .build();
        assertTrue(getApiClient().getApis().getWorkflowEngine().registerWorkflow(workflowDef, true));
        doExecute(workflowDef, Map.of("items", new Integer[] { 123 }), 30);
        deleteWorkflowDef(workflowDef);
    }

    /**
     * 测试多层循环嵌套
     */
    @Test
    public void testMultiFor() {
        WorkflowDefinition workflowDef = WorkflowDefinition.newBuilder("sdk_multi_for_workflow", 1)
            .addTask(new For("for1Ref", "${workflow.input.items1}")//
                .childTask(new SimpleTask(SIMPLE_TASK, "forSimple1Ref")//
                    .input("name", "${for1Ref.output.element}"))//
                .childTask(new For("for2Ref", "${workflow.input.items2}")//
                    .childTask(new SimpleTask(SIMPLE_TASK, "forSimple2Ref")//
                        .input("name", "${for2Ref.output.element}"))))//
            .build();
        assertTrue(getApiClient().getApis().getWorkflowEngine().registerWorkflow(workflowDef, true));
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("name", "欢迎访问任务云平台 http://www.taskflow.cn");
        dataMap.put("items1", Lists.newArrayList("飞流", "数据"));
        dataMap.put("items2", Lists.newArrayList("任务云平台"));
        doExecute(workflowDef, dataMap, 15);
        deleteWorkflowDef(workflowDef);
    }

    @Test
    public void testMultiForTaskRef() {
        WorkflowDefinition workflowDef = WorkflowDefinition.newBuilder("sdk_multi_for2_workflow", 1)//
            .addTask(new SimpleTask(RANDOM_ITEMS, "random_items_1_ref").input("status", true))//
            .addTask(new SimpleTask(RANDOM_ITEMS, "random_items_2_ref").input("status", false))
            //
            .addTask(
                new For("for1Ref", "${random_items_1_ref.output.result}")//
                    .childTask(new SimpleTask(SIMPLE_TASK, "forSimple1Ref").input("name", "${for1Ref.output.element}"))//
                    .childTask(
                        new For("for2Ref", "${random_items_2_ref.output.result}").childTask(new SimpleTask(SIMPLE_TASK,
                            "forSimple2Ref")//
                            .input("name", "${for2Ref.output.element}"))))//
            .build();
        assertTrue(getApiClient().getApis().getWorkflowEngine().registerWorkflow(workflowDef, true));
        doExecute(workflowDef, new HashMap<>(), 15);
        deleteWorkflowDef(workflowDef);
    }

    @Test
    public void testSimpleFor() {
        WorkflowDefinition workflowDef = WorkflowDefinition.newBuilder("sdk_simple_for_workflow", 1)//
            .addTask(new SimpleTask(SIMPLE_TASK, "simple1Ref").input("name", "任务一"))//
            .addTask(new DoWhile("do_while_ref", 3)//
                .childTask(new SimpleTask(SIMPLE_TASK, "simple2Ref")//
                    .input("name", "doWhile-任务")))//
            .addTask(new For("forRef", "${workflow.input.x}")//
                .childTask(new SimpleTask(SIMPLE_TASK, "simple3Ref")//
                    .input("name", "for-任务"))).addTask(new DoWhile("dowhileRef", "${workflow.input.loopCount}")//
                .childTask(new SimpleTask(SIMPLE_TASK, "dowhileRef_simpleTask")//
                    .input("name", "do-while 循环，应该执行一次")))//
            .addTask(new SimpleTask(SIMPLE_TASK, "simple4Ref").input("name", "任务四"))//
            .build();
        assertTrue(getApiClient().getApis().getWorkflowEngine().registerWorkflow(workflowDef, true));
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("loopCount", 1);
        doExecute(workflowDef, inputData, 15);
        deleteWorkflowDef(workflowDef);
    }

    /**
     * 测试FOR循环遍历数据不合法问题
     *
     * @throws Exception
     */
    @Test
    public void testForFail() throws Exception {
        WorkflowDefinition workflowDef = WorkflowDefinition.newBuilder("sdk_for_fail_workflow", 1)//
            .addTask(new SimpleTask(SIMPLE_TASK, "simpleRef").input("name", "hello kitty"))//
            .addTask(new For("forRef", "${workflow.input.items}")//
                .childTask(new SimpleTask(SIMPLE_TASK, "simple3Ref")//
                    .input("name", "测试")))//
            .build();
        assertTrue(getApiClient().getApis().getWorkflowEngine().registerWorkflow(workflowDef, true));
        String workflowId = getApiClient().getApis().getWorkflowEngine().start(workflowDef, Map.of("items", "xxx"));
        ExecutingWorkflow workflow = waitForTerminal(workflowId, 60);
        System.out.println("workflowId:" + workflow.getWorkflowId());
        Assert.assertEquals(ExecutingWorkflow.WorkflowStatus.FAILED, workflow.getStatus());
        deleteWorkflowDef(workflowDef);
    }

    @SneakyThrows
    @Test
    public void testCreateWorkflow() {
        WorkflowDefinition workflowDef = WorkflowDefinition.newBuilder("sdk_workflow", 1)
            .addTask(new SimpleTask(SIMPLE_TASK, "simpleTaskRef").input("name", "${workflow.input.name}"))
            .addTask(new For("eachRef", "${workflow.input.elements}")//
                .childTask(new SimpleTask(SIMPLE_TASK, "loopSimpleTaskRef")//
                    .input("name", "${eachRef.output.element}")))//
            .build();
        assertTrue(getApiClient().getApis().getWorkflowEngine().registerWorkflow(workflowDef, true));
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("name", "欢迎访问任务云平台 http://www.taskflow.cn");
        dataMap.put("elements", Lists.newArrayList("欢迎访问", "任务云平台"));
        doExecute(workflowDef, dataMap, 10);
        deleteWorkflowDef(workflowDef);
    }

    @SneakyThrows
    private void doExecute(WorkflowDefinition workflowDef, Map<String, Object> dataMap, int timeout) {
        String workflowId = getApiClient().getApis().getWorkflowEngine().start(workflowDef, dataMap);
        ExecutingWorkflow executedWorkflow = waitForTerminal(workflowId, timeout);
        System.out.println("workflowId:" + executedWorkflow.getWorkflowId());
        Assert.assertNotNull(executedWorkflow);
        Assert.assertEquals(ExecutingWorkflow.WorkflowStatus.COMPLETED, executedWorkflow.getStatus());
    }

    public static class MyWorkers {
        @WorkerTask(value = SIMPLE_TASK, title = "单元测试任务")
        public String sdkTask(@InputParam("name") Object name) {
            System.out.println("自定义工作节点:::" + name);
            return "Hello " + name;
        }

        @WorkerTask(value = RANDOM_ITEMS, title = "random-items", threadCount = 3)
        public List<String> randomItems(@InputParam("status") Boolean status) {
            if (status) {
                return Lists.newArrayList("A", "B");
            } else {
                return Lists.newArrayList("任务云");
            }
        }
    }
}
