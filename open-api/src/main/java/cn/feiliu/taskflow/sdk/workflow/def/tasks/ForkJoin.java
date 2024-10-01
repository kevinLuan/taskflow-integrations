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
package cn.feiliu.taskflow.sdk.workflow.def.tasks;

import cn.feiliu.taskflow.common.metadata.tasks.TaskType;
import cn.feiliu.taskflow.common.metadata.workflow.FlowTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ForkJoin task 数据定义示例：
 * <pre>
 * [
 *      [task-1, task-2],
 *      [task-3, task-4],
 *      [task-5]
 * ]
 * </pre>
 * <p>
 * 执行示例：
 * <pre>
 * -----------------------------
 * |          fork-join        |
 * -----------------------------
 * |             |             |
 * task-1        task-3        task-5
 * |             |             |
 * task-2        task-4        |
 * |             |             |
 * -----------------------------
 * |           join            |
 * -----------------------------
 * </pre>
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-09-08
 */
public class ForkJoin extends Task<ForkJoin> {

    private Join     join;

    private Task[][] forkedTasks;

    /**
     * execute task specified in the forkedTasks parameter in parallel.
     *
     * <p>forkedTask is a two-dimensional list that executes the outermost list in parallel and list
     * within that is executed sequentially.
     *
     * <p>e.g. [[task1, task2],[task3, task4],[task5]] are executed as:
     *
     * <pre>
     *                    ---------------
     *                    |     fork    |
     *                    ---------------
     *                    |       |     |
     *                    |       |     |
     *                  task1  task3  task5
     *                  task2  task4    |
     *                    |      |      |
     *                 ---------------------
     *                 |       join        |
     *                 ---------------------
     * </pre>
     *
     * <p>This method automatically adds a join that waits for all the *last* tasks in the fork
     * (e.g. task2, task4 and task5 in the above example) to be completed.*
     *
     * <p>Use join method @see {@link ForkJoin#joinOn(String...)} to override this behavior (note:
     * not a common scenario)
     *
     * @param taskReferenceName unique task reference name
     * @param forkedTasks List of tasks to be executed in parallel
     */
    public ForkJoin(String taskReferenceName, Task<?>[]... forkedTasks) {
        super(taskReferenceName, TaskType.FORK_JOIN);
        this.forkedTasks = forkedTasks;
    }

    ForkJoin(FlowTask workflowTask) {
        super(workflowTask);
        int size = workflowTask.getForkTasks().size();
        this.forkedTasks = new Task[size][];
        int i = 0;
        for (List<FlowTask> forkTasks : workflowTask.getForkTasks()) {
            Task[] tasks = new Task[forkTasks.size()];
            for (int j = 0; j < forkTasks.size(); j++) {
                FlowTask forkWorkflowTask = forkTasks.get(j);
                Task task = TaskRegistry.getTask(forkWorkflowTask);
                tasks[j] = task;
            }
            this.forkedTasks[i++] = tasks;
        }
    }

    public ForkJoin joinOn(String... joinOn) {
        this.join = new Join(getTaskReferenceName() + "_join", joinOn);
        return this;
    }

    @Override
    protected List<FlowTask> getChildrenTasks() {
        FlowTask fork = toWorkflowTask();

        FlowTask joinWorkflowTask = null;
        if (this.join != null) {
            List<FlowTask> joinTasks = this.join.getWorkflowDefTasks();
            joinWorkflowTask = joinTasks.get(0);
        } else {
            joinWorkflowTask = new FlowTask();
            joinWorkflowTask.setType(TaskType.JOIN);
            joinWorkflowTask.setTaskReferenceName(getTaskReferenceName() + "_join");
            joinWorkflowTask.setName(joinWorkflowTask.getTaskReferenceName());
            joinWorkflowTask.setJoinOn(fork.getJoinOn());
        }
        return Arrays.asList(joinWorkflowTask);
    }

    @Override
    public void updateWorkflowTask(FlowTask fork) {
        List<String> joinOnTaskRefNames = new ArrayList<>();
        List<List<FlowTask>> forkTasks = new ArrayList<>();

        for (Task<?>[] forkedTaskList : forkedTasks) {
            List<FlowTask> forkedWorkflowTasks = new ArrayList<>();
            for (Task<?> baseWorkflowTask : forkedTaskList) {
                forkedWorkflowTasks.addAll(baseWorkflowTask.getWorkflowDefTasks());
            }
            forkTasks.add(forkedWorkflowTasks);
            joinOnTaskRefNames.add(forkedWorkflowTasks.get(forkedWorkflowTasks.size() - 1).getTaskReferenceName());
        }
        if (this.join != null) {
            fork.setJoinOn(List.of(this.join.getJoinOn()));
        } else {
            fork.setJoinOn(joinOnTaskRefNames);
        }

        fork.setForkTasks(forkTasks);
    }

    public Task[][] getForkedTasks() {
        return forkedTasks;
    }
}
