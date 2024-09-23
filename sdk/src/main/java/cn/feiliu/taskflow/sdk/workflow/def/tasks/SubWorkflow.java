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

import cn.feiliu.taskflow.common.metadata.tasks.TaskType;
import cn.feiliu.taskflow.common.metadata.workflow.SubFlowParams;
import cn.feiliu.taskflow.common.metadata.workflow.FlowTask;
import cn.feiliu.taskflow.common.metadata.workflow.WorkflowDefinition;

public class SubWorkflow extends Task<SubWorkflow> {

    private WorkflowDefinition workflowDef;

    private String             workflowName;

    private Integer            workflowVersion;

    /**
     * Start a workflow as a sub-workflow
     *
     * @param taskReferenceName
     * @param workflowName
     * @param workflowVersion
     */
    public SubWorkflow(String taskReferenceName, String workflowName, Integer workflowVersion) {
        super(taskReferenceName, TaskType.SUB_WORKFLOW);
        this.workflowName = workflowName;
        this.workflowVersion = workflowVersion;
    }

    /**
     * Start a workflow as a sub-workflow
     *
     * @param taskReferenceName
     * @param workflowDef
     */
    public SubWorkflow(String taskReferenceName, WorkflowDefinition workflowDef) {
        super(taskReferenceName, TaskType.SUB_WORKFLOW);
        this.workflowDef = workflowDef;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public int getWorkflowVersion() {
        return workflowVersion;
    }

    @Override
    protected void updateWorkflowTask(FlowTask workflowTask) {
        SubFlowParams subWorkflowParam = new SubFlowParams();
        if (workflowDef != null) {
            subWorkflowParam.setWorkflowDefinition(workflowDef);
        } else {
            subWorkflowParam.setName(workflowName);
            subWorkflowParam.setVersion(workflowVersion);
        }
        workflowTask.setSubWorkflowParam(subWorkflowParam);
    }
}
