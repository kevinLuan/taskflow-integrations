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

import cn.feiliu.taskflow.client.api.ISchedulerClient;
import cn.feiliu.taskflow.open.dto.WorkflowScheduleExecution;
import cn.feiliu.taskflow.open.exceptions.ApiException;
import cn.feiliu.taskflow.client.http.api.SchedulerResourceApi;
import cn.feiliu.taskflow.open.dto.SaveScheduleRequest;
import cn.feiliu.taskflow.open.dto.WorkflowSchedule;

import java.util.List;

public class SchedulerClient implements ISchedulerClient {

    private SchedulerResourceApi schedulerResourceApi;

    public SchedulerClient(ApiClient apiClient) {
        this.schedulerResourceApi = new SchedulerResourceApi(apiClient);
    }

    /**
     * 按名称删除现有工作流计划
     *
     * @param name
     * @throws ApiException
     */
    @Override
    public Boolean deleteSchedule(String name) throws ApiException {
        return schedulerResourceApi.deleteSchedule(name);
    }

    /**
     * 获取所有现有工作流计划，并可选择按工作流名称进行筛选
     *
     * @param workflowName
     * @return
     * @throws ApiException
     */
    @Override
    public List<WorkflowSchedule> getAllSchedules(String workflowName) throws ApiException {
        return schedulerResourceApi.getAllSchedules(workflowName);
    }

    /**
     * 获取调度程序的下一个x次(默认3次，最多5次)执行时间的列表
     *
     * @param cronExpression
     * @param scheduleStartTime
     * @param scheduleEndTime
     * @param limit
     * @return
     * @throws ApiException
     */
    @Override
    public List<Long> getNextFewSchedules(String cronExpression, Long scheduleStartTime, Long scheduleEndTime,
                                          Integer limit) throws ApiException {
        return schedulerResourceApi.getNextFewSchedules(cronExpression, scheduleStartTime, scheduleEndTime, limit);
    }

    /**
     * 按名称获取现有工作流计划
     *
     * @param name
     * @return
     * @throws ApiException
     */
    @Override
    public WorkflowSchedule getSchedule(String name) throws ApiException {
        return schedulerResourceApi.getSchedule(name);
    }

    @Override
    public Integer pauseAllSchedules() {
        return schedulerResourceApi.pauseAllSchedules();
    }

    /**
     * 按名称暂停现有计划
     *
     * @param name
     * @throws ApiException
     */
    @Override
    public Boolean pauseSchedule(String name) throws ApiException {
        return schedulerResourceApi.pauseSchedule(name);
    }

    /**
     * 按名称恢复已暂停的计划
     *
     * @param name
     * @throws ApiException
     */
    @Override
    public Boolean resumeSchedule(String name) throws ApiException {
        return schedulerResourceApi.resumeSchedule(name);
    }

    @Override
    public Integer resumeAllSchedules() {
        return schedulerResourceApi.resumeAllSchedules();
    }

    /**
     * 创建或更新工作流调度器
     *
     * @param request
     * @throws ApiException
     */
    @Override
    public Boolean saveSchedule(SaveScheduleRequest request) throws ApiException {
        return schedulerResourceApi.saveSchedule(request);
    }

    @Override
    public List<WorkflowScheduleExecution> getAllExecutionRecords(Long start, Integer size) {
        return schedulerResourceApi.getAllExecutionRecords(start, size);
    }
}
