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
package cn.feiliu.taskflow.client;

import cn.feiliu.taskflow.client.api.IWorkflowDefClient;
import cn.feiliu.taskflow.client.http.api.WorkflowDefResourceApi;
import cn.feiliu.taskflow.core.def.ValidationException;
import cn.feiliu.taskflow.dto.workflow.WorkflowDefinition;
import cn.feiliu.taskflow.utils.FeiliuValidator;

import java.util.List;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-07-25
 */
public class WorkflowDefClient implements IWorkflowDefClient {
    private WorkflowDefResourceApi workflowDefResourceApi;

    public WorkflowDefClient(ApiClient apiClient) {
        this.workflowDefResourceApi = new WorkflowDefResourceApi(apiClient);
    }

    @Override
    public boolean createIfAbsent(WorkflowDefinition workflowDef) {
        verify(workflowDef);
        return workflowDefResourceApi.createIfAbsent(workflowDef);
    }

    @Override
    public boolean updateWorkflowDef(WorkflowDefinition workflowDef) {
        verify(workflowDef);
        return workflowDefResourceApi.updateWorkflowDef(workflowDef);
    }

    @Override
    public WorkflowDefinition getWorkflowDef(String name, Integer version) {
        return workflowDefResourceApi.getWorkflowDef(name, version);
    }

    @Override
    public boolean publishWorkflowDef(String name, Integer version, Boolean overwrite) {
        return workflowDefResourceApi.publishWorkflowDef(name, version, overwrite);
    }

    @Override
    public boolean deleteWorkflowDef(String name, Integer version) {
        return workflowDefResourceApi.deleteWorkflowDef(name, version);
    }

    @Override
    public boolean registerWorkflow(WorkflowDefinition workflowDef, boolean overwrite) {
        verify(workflowDef);
        return workflowDefResourceApi.registerWorkflow(workflowDef, overwrite);
    }

    private void verify(WorkflowDefinition workflowDef) {
        List<String> errors = FeiliuValidator.verifyWorkflowDef(workflowDef);
        if (errors.size() > 0) {
            throw new ValidationException("Errors in workflow definition.\n" + String.join("\n", errors))
                .addErrors(errors);
        }
    }

    @Override
    public void close() throws Exception {

    }
}
