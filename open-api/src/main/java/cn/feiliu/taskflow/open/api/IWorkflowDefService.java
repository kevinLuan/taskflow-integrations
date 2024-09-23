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
package cn.feiliu.taskflow.open.api;

import cn.feiliu.taskflow.common.metadata.workflow.WorkflowDefinition;

/**
 * 工作流定义服务
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-07-25
 */
public interface IWorkflowDefService {
    /**
     * create a workflow definition with the server
     *
     * @param workflowDef the workflow definition
     */
    boolean createIfAbsent(WorkflowDefinition workflowDef);

    /**
     * Updates an existing workflow definitions
     *
     * @param workflowDef workflow definitions to be updated
     */
    boolean updateWorkflowDef(WorkflowDefinition workflowDef);

    /**
     * Retrieve the workflow definition
     *
     * @param name    the name of the workflow
     * @param version the version of the workflow def
     * @return Workflow definition for the given workflow and version
     */
    WorkflowDefinition getWorkflowDef(String name, Integer version);

    /**
     * Publishing the workflow definition to the scheduling engine server
     *
     * @param name
     * @param version
     * @param overwrite 调度引擎是否覆盖更新，缺省值:false
     * @return
     */
    boolean publishWorkflowDef(String name, Integer version, Boolean overwrite);

    /**
     * Removes the workflow definition of a workflow from the taskflow server. It does not remove
     * associated workflows. Use with caution.
     *
     * @param name    Name of the workflow to be unregistered.
     * @param version Version of the workflow definition to be unregistered.
     */
    boolean deleteWorkflowDef(String name, Integer version);

    /**
     * 注册工作流定义,并同步注册任务定义
     *
     * @param workflowDef
     * @return
     */
    boolean registerWorkflow(WorkflowDefinition workflowDef, boolean overwrite);
}
