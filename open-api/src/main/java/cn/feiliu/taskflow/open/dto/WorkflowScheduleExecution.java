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
package cn.feiliu.taskflow.open.dto;

import cn.feiliu.taskflow.common.enums.WorkflowScheduleExecutionState;
import cn.feiliu.taskflow.common.metadata.workflow.StartWorkflowRequest;
import lombok.*;

import java.util.Objects;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-07-03
 */
@Getter
@Setter
@ToString
public class WorkflowScheduleExecution {
    /*执行ID*/
    private Long                           id;
    /*调度名称*/
    private String                         scheduleName;
    /*调度时间*/
    private Long                           scheduledTime;
    /*执行时间*/
    private Long                           executionTime;
    /*执行原因*/
    private String                         reason;
    /*跟踪栈*/
    private String                         stackTrace;
    /*工作流请求*/
    private StartWorkflowRequest           startWorkflowRequest;
    /*执行状态*/
    private WorkflowScheduleExecutionState state;
    /*工作流运行实例ID*/
    private String                         workflowId;
    /*工作流定义名称*/
    private String                         workflowName;

    public static class Builder {
        private Long                           id;
        private String                         scheduleName;
        private Long                           scheduledTime;
        private Long                           executionTime;
        private String                         reason;
        private String                         stackTrace;
        private StartWorkflowRequest           startWorkflowRequest;
        private WorkflowScheduleExecutionState state;
        private String                         workflowId;
        private String                         workflowName;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder scheduleName(String scheduleName) {
            this.scheduleName = scheduleName;
            return this;
        }

        public Builder scheduledTime(Long scheduledTime) {
            this.scheduledTime = scheduledTime;
            return this;
        }

        public Builder executionTime(Long executionTime) {
            this.executionTime = executionTime;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder stackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
            return this;
        }

        public Builder startWorkflowRequest(StartWorkflowRequest startWorkflowRequest) {
            this.startWorkflowRequest = startWorkflowRequest;
            return this;
        }

        public Builder state(WorkflowScheduleExecutionState state) {
            this.state = state;
            return this;
        }

        public Builder workflowId(String workflowId) {
            this.workflowId = workflowId;
            return this;
        }

        public Builder workflowName(String workflowName) {
            this.workflowName = workflowName;
            return this;
        }

        public WorkflowScheduleExecution build() {
            WorkflowScheduleExecution execution = new WorkflowScheduleExecution();
            execution.setId(this.id);
            execution.setScheduleName(this.scheduleName);
            execution.setScheduledTime(this.scheduledTime);
            execution.setExecutionTime(this.executionTime);
            execution.setReason(this.reason);
            execution.setStackTrace(this.stackTrace);
            execution.setStartWorkflowRequest(this.startWorkflowRequest);
            execution.setState(this.state);
            execution.setWorkflowId(this.workflowId);
            execution.setWorkflowName(this.workflowName);
            return execution;
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;
        WorkflowScheduleExecution that = (WorkflowScheduleExecution) object;
        return Objects.equals(id, that.id) && Objects.equals(scheduleName, that.scheduleName)
               && Objects.equals(scheduledTime, that.scheduledTime)
               && Objects.equals(executionTime, that.executionTime) && Objects.equals(reason, that.reason)
               && Objects.equals(stackTrace, that.stackTrace)
               && Objects.equals(startWorkflowRequest, that.startWorkflowRequest) && state == that.state
               && Objects.equals(workflowId, that.workflowId) && Objects.equals(workflowName, that.workflowName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, scheduleName, scheduledTime, executionTime, reason, stackTrace, startWorkflowRequest,
            state, workflowId, workflowName);
    }
}
