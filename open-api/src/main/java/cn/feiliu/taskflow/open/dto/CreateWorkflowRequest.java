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
package cn.feiliu.taskflow.open.dto;

import cn.feiliu.taskflow.common.metadata.workflow.WorkflowDefinition;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

/**
 * 创建工作流请求
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-05-29
 */
@Getter
@Setter
@ToString
public class CreateWorkflowRequest extends WorkflowDefinition {
    /*自动注册任务*/
    private boolean registerTask = false;
    /**
     * 重写工作流定义
     */
    private boolean overwrite    = false;

    public static CreateWorkflowRequest.Builder newBuilder() {
        return new CreateWorkflowRequest.Builder();
    }

    public static class Builder extends WorkflowDefinition.Builder {
        private boolean registerTask = false;
        private boolean overwrite    = false;

        public Builder registerTask(boolean registerTask) {
            this.registerTask = registerTask;
            return this;
        }

        public Builder overwrite(boolean overwrite) {
            this.overwrite = overwrite;
            return this;
        }

        @Override
        public CreateWorkflowRequest build() {
            CreateWorkflowRequest request = new CreateWorkflowRequest();
            request.setRegisterTask(this.registerTask);
            request.setOverwrite(this.overwrite);
            // 设置父类属性
            WorkflowDefinition def = super.build();
            request.setName(def.getName());
            request.setDescription(def.getDescription());
            request.setVersion(def.getVersion());
            request.setTasks(def.getTasks());
            request.setInputParameters(def.getInputParameters());
            request.setOutputParameters(def.getOutputParameters());
            request.setFailureWorkflow(def.getFailureWorkflow());
            request.setRestartable(def.isRestartable());
            request.setWorkflowStatusListenerEnabled(def.isWorkflowStatusListenerEnabled());
            request.setOwnerEmail(def.getOwnerEmail());
            request.setTimeoutPolicy(def.getTimeoutPolicy());
            request.setTimeoutSeconds(def.getTimeoutSeconds());
            request.setVariables(def.getVariables());
            request.setInputTemplate(def.getInputTemplate());
            return request;
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;
        if (!super.equals(object))
            return false;
        CreateWorkflowRequest that = (CreateWorkflowRequest) object;
        return registerTask == that.registerTask && overwrite == that.overwrite;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), registerTask, overwrite);
    }
}
