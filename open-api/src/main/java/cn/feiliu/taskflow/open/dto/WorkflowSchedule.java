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

import cn.feiliu.taskflow.common.enums.TriggerType;
import cn.feiliu.taskflow.common.metadata.workflow.StartWorkflowRequest;
import cn.feiliu.taskflow.open.dto.trigger.CronTrigger;
import cn.feiliu.taskflow.open.dto.trigger.FixedIntervalTrigger;
import cn.feiliu.taskflow.open.dto.trigger.WebhookTrigger;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * WorkflowSchedule
 */
@Getter
@Setter
@ToString
public class WorkflowSchedule {
    /*调度名称*/
    private String               name;
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
    /*时区*/
    private String               timeZone;
    private TriggerType          triggerType;
    private CronTrigger          cronTrigger;
    private FixedIntervalTrigger fixedIntervalTrigger;
    private WebhookTrigger       webhookTrigger;

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private WorkflowSchedule value;

        public Builder() {
            value = new WorkflowSchedule();
        }

        public Builder name(String name) {
            value.name = name;
            return this;
        }

        public Builder paused(Boolean paused) {
            value.paused = paused;
            return this;
        }

        public Builder runCatchupScheduleInstances(Boolean runCatchupScheduleInstances) {
            value.runCatchupScheduleInstances = runCatchupScheduleInstances;
            return this;
        }

        public Builder scheduleEndTime(Long scheduleEndTime) {
            value.scheduleEndTime = scheduleEndTime;
            return this;
        }

        public Builder scheduleStartTime(Long scheduleStartTime) {
            value.scheduleStartTime = scheduleStartTime;
            return this;
        }

        public Builder startWorkflowRequest(StartWorkflowRequest startWorkflowRequest) {
            value.startWorkflowRequest = startWorkflowRequest;
            return this;
        }

        public Builder triggerType(TriggerType triggerType) {
            value.triggerType = triggerType;
            return this;
        }

        public Builder cronTrigger(CronTrigger cronTrigger) {
            value.cronTrigger = cronTrigger;
            return this;
        }

        public Builder fixedIntervalTrigger(FixedIntervalTrigger timerTaskTrigger) {
            value.fixedIntervalTrigger = timerTaskTrigger;
            return this;
        }

        public Builder webhookTrigger(WebhookTrigger webhookTrigger) {
            value.webhookTrigger = webhookTrigger;
            return this;
        }

        public WorkflowSchedule build() {
            return value;
        }
    }
}
