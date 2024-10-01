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

import cn.feiliu.taskflow.common.metadata.workflow.StartWorkflowRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

/**
 * WorkflowSchedule
 */
@Getter
@Setter
@ToString
public class WorkflowSchedule {
    /*调度名称*/
    private String               name;
    /*cron表达式*/
    private String               cronExpression;
    /*是否暂停*/
    private boolean              paused                      = false;
    /*是否运行补偿调度执行实例*/
    private boolean              runCatchupScheduleInstances = false;
    /*调度结束时间*/
    private Long                 scheduleEndTime;
    /*调度开始时间*/
    private Long                 scheduleStartTime;
    /*运行工作流参数*/
    private StartWorkflowRequest startWorkflowRequest;
    /*创建时间*/
    private Long                 createTime;
    /*创建人*/
    private String               createdBy;
    /*修改人*/
    private String               updatedBy;
    /*修改时间*/
    private Long                 updatedTime;

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String               name;
        private String               cronExpression;
        private boolean              paused                      = false;
        private boolean              runCatchupScheduleInstances = false;
        private Long                 scheduleEndTime;
        private Long                 scheduleStartTime;
        private StartWorkflowRequest startWorkflowRequest;
        private Long                 createTime;
        private String               createdBy;
        private String               updatedBy;
        private Long                 updatedTime;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder cronExpression(String cronExpression) {
            this.cronExpression = cronExpression;
            return this;
        }

        public Builder paused(Boolean paused) {
            this.paused = paused;
            return this;
        }

        public Builder runCatchupScheduleInstances(Boolean runCatchupScheduleInstances) {
            this.runCatchupScheduleInstances = runCatchupScheduleInstances;
            return this;
        }

        public Builder scheduleEndTime(Long scheduleEndTime) {
            this.scheduleEndTime = scheduleEndTime;
            return this;
        }

        public Builder scheduleStartTime(Long scheduleStartTime) {
            this.scheduleStartTime = scheduleStartTime;
            return this;
        }

        public Builder startWorkflowRequest(StartWorkflowRequest startWorkflowRequest) {
            this.startWorkflowRequest = startWorkflowRequest;
            return this;
        }

        public Builder createTime(Long createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public Builder updatedTime(Long updatedTime) {
            this.updatedTime = updatedTime;
            return this;
        }

        public WorkflowSchedule build() {
            WorkflowSchedule workflowSchedule = new WorkflowSchedule();
            workflowSchedule.name = this.name;
            workflowSchedule.cronExpression = this.cronExpression;
            workflowSchedule.paused = this.paused;
            workflowSchedule.runCatchupScheduleInstances = this.runCatchupScheduleInstances;
            workflowSchedule.scheduleEndTime = this.scheduleEndTime;
            workflowSchedule.scheduleStartTime = this.scheduleStartTime;
            workflowSchedule.startWorkflowRequest = this.startWorkflowRequest;
            workflowSchedule.createTime = this.createTime;
            workflowSchedule.createdBy = this.createdBy;
            workflowSchedule.updatedBy = this.updatedBy;
            workflowSchedule.updatedTime = this.updatedTime;
            return workflowSchedule;
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;
        WorkflowSchedule that = (WorkflowSchedule) object;
        return Objects.equals(name, that.name) && Objects.equals(cronExpression, that.cronExpression)
               && Objects.equals(paused, that.paused)
               && Objects.equals(runCatchupScheduleInstances, that.runCatchupScheduleInstances)
               && Objects.equals(scheduleEndTime, that.scheduleEndTime)
               && Objects.equals(scheduleStartTime, that.scheduleStartTime)
               && Objects.equals(startWorkflowRequest, that.startWorkflowRequest)
               && Objects.equals(createTime, that.createTime) && Objects.equals(createdBy, that.createdBy)
               && Objects.equals(updatedBy, that.updatedBy) && Objects.equals(updatedTime, that.updatedTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, cronExpression, paused, runCatchupScheduleInstances, scheduleEndTime,
            scheduleStartTime, startWorkflowRequest, createTime, createdBy, updatedBy, updatedTime);
    }
}
