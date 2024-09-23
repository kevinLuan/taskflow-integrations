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
package cn.feiliu.taskflow.sdk.workflow.def.tasks;

import cn.feiliu.taskflow.common.metadata.tasks.TaskDefinition;
import cn.feiliu.taskflow.common.metadata.tasks.TaskType;
import cn.feiliu.taskflow.common.metadata.workflow.FlowTask;

/**
 * Workflow task executed by a worker
 * 自定义工作节点
 */
public class WorkTask extends Task<WorkTask> {

    private TaskDefinition taskDef;

    public WorkTask(String taskDefName, String taskReferenceName) {
        super(taskReferenceName, TaskType.SIMPLE);
        super.name(taskDefName);
    }

    WorkTask(FlowTask workflowTask) {
        super(workflowTask);
        this.taskDef = workflowTask.getTaskDefinition();
    }

    public TaskDefinition getTaskDef() {
        return taskDef;
    }

    public WorkTask setTaskDef(TaskDefinition taskDef) {
        this.taskDef = taskDef;
        return this;
    }

    @Override
    protected void updateWorkflowTask(FlowTask workflowTask) {
        workflowTask.setTaskDefinition(taskDef);
    }
}
