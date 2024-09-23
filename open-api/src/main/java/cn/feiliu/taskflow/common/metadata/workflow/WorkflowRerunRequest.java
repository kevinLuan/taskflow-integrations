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
package cn.feiliu.taskflow.common.metadata.workflow;

import lombok.Data;

import java.util.Map;

/**
 * 重特定任务重新运行工作流请求
 */
@Data
public class WorkflowRerunRequest {
    /*工作流实例ID*/
    private String              reRunFromWorkflowId;
    /*工作流输入*/
    private Map<String, Object> workflowInput;
    /*重新运行任务ID*/
    private String              reRunFromTaskId;
    /*任务输入*/
    private Map<String, Object> taskInput;
    /*自定义关联业务ID*/
    private String              correlationId;

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String              reRunFromWorkflowId;
        private Map<String, Object> workflowInput;
        private String              reRunFromTaskId;
        private Map<String, Object> taskInput;
        private String              correlationId;

        public Builder reRunFromWorkflowId(String reRunFromWorkflowId) {
            this.reRunFromWorkflowId = reRunFromWorkflowId;
            return this;
        }

        public Builder workflowInput(Map<String, Object> workflowInput) {
            this.workflowInput = workflowInput;
            return this;
        }

        public Builder reRunFromTaskId(String reRunFromTaskId) {
            this.reRunFromTaskId = reRunFromTaskId;
            return this;
        }

        public Builder taskInput(Map<String, Object> taskInput) {
            this.taskInput = taskInput;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public WorkflowRerunRequest build() {
            WorkflowRerunRequest request = new WorkflowRerunRequest();
            request.reRunFromWorkflowId = this.reRunFromWorkflowId;
            request.workflowInput = this.workflowInput;
            request.reRunFromTaskId = this.reRunFromTaskId;
            request.taskInput = this.taskInput;
            request.correlationId = this.correlationId;
            return request;
        }
    }
}
