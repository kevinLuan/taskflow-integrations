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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * SaveScheduleRequest
 */
@Setter
@Getter
@EqualsAndHashCode
@ToString
public class SaveScheduleRequest {
    /*调度名称*/
    @NotNull(message = "name cannot be empty")
    private String               name;
    /*调度计划描述*/
    private String               description;
    /*是否暂停*/
    @NotNull(message = "paused cannot be empty")
    private Boolean              paused                      = false;
    /**
     * 参数用于控制调度程序是否应该执行错过的任务实例。具体含义如下：
     * <p>
     * (是否运行补偿任务实例)：此参数决定当调度程序重新启动或重新调度时，是否要运行在调度程序停止期间错过的任务实例。
     * true：如果设置为 true，调度程序将在重新启动或重新调度时执行所有错过的任务实例。这确保了所有计划的任务都能被执行，即使调度程序在某些时段未运行。
     * false：如果设置为 false，调度程序将不会执行错过的任务实例，只会执行从重新启动或重新调度时间点开始的任务实例。这可以防止在调度程序停止期间积累的任务突然在恢复后全部执行，从而避免过载。
     * 这个参数的设置取决于你的任务调度需求以及系统的容错能力。
     * 如果需要确保每一个任务实例都被执行，那么应设置为 true。如果系统无法处理在调度程序停止期间积累的所有任务实例，或这些任务实例可以被安全地忽略，则应设置为 false。
     */
    @NotNull(message = "runCatchupScheduleInstances cannot be empty")
    private Boolean              runCatchupScheduleInstances = false;
    /*调度开始时间*/
    @NotNull(message = "startTime cannot be empty")
    private Long                 startTime;
    /**
     * 调度结束时间
     */
    @NotNull(message = "endTime cannot be empty")
    private Long                 endTime;
    /*时区*/
    private String               timeZone                    = "Asia/Shanghai";
    /*调度类型*/
    private TriggerType          triggerType;
    /*定时任务类型：触发配置*/
    private FixedIntervalTrigger fixedIntervalTrigger;
    /*cron表达式：触发配置*/
    private CronTrigger          cronTrigger;
    /**
     * 调度执行时用于启动工作流请求
     */
    @NotNull(message = "startWorkflowRequest cannot be empty")
    @Valid
    private StartWorkflowRequest startWorkflowRequest;
    /**
     * 当开启覆盖更新时，若存在相同名称的调度，则更新调度，否则创建调度。
     * <p>注意：</p>
     * <ul>
     *     <li>当覆盖更新时，需要确保调度计划处于暂停或未开始状态(系统尚未生成调度实例)，否则系统会拒绝执行覆盖更新操作，并抛出异常。</li>
     *     <li>若需要执行你应该先调用：pauseSchedule() 将调度计task_kevin_flow_2024_04_30
     *     划暂停后再执行</li>
     * </ul>
     */
    private boolean              overwrite                   = false;

}
