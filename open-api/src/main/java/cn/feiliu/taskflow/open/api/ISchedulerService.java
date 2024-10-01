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
package cn.feiliu.taskflow.open.api;

import cn.feiliu.taskflow.open.dto.SaveScheduleRequest;
import cn.feiliu.taskflow.open.dto.WorkflowSchedule;
import cn.feiliu.taskflow.open.dto.WorkflowScheduleExecution;

import java.util.List;

public interface ISchedulerService {
    /**
     * 根据名称删除调度计划
     *
     * @param name
     */
    Boolean deleteSchedule(String name);

    /**
     * 根据工作流名称获取调度计划
     *
     * @param workflowName
     * @return
     */
    List<WorkflowSchedule> getAllSchedules(String workflowName);

    /**
     * 获取接下来的几个调度时间表
     *
     * @param cronExpression
     * @param scheduleStartTime
     * @param scheduleEndTime
     * @param limit
     * @return
     */
    List<Long> getNextFewSchedules(String cronExpression, Long scheduleStartTime, Long scheduleEndTime, Integer limit);

    /**
     * 根据调度名称获取调度计划
     *
     * @param name
     * @return
     */
    WorkflowSchedule getSchedule(String name);

    /**
     * 暂停所有调度计划
     */
    Integer pauseAllSchedules();

    /**
     * 暂定调度计划
     *
     * @param name 调度名称
     */
    Boolean pauseSchedule(String name);

    /**
     * 灰度调度计划
     *
     * @param name 调度名称
     */
    Boolean resumeSchedule(String name);

    /**
     * 恢复所有调度计划
     */
    Integer resumeAllSchedules();

    /**
     * 创建获更新调度计划
     *
     * @param saveScheduleRequest
     */
    Boolean saveSchedule(SaveScheduleRequest saveScheduleRequest);

    /**
     * 获取所有执行记录
     */
    List<WorkflowScheduleExecution> getAllExecutionRecords(Long start, Integer size);
}
