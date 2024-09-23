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
package cn.feiliu.taskflow.client.http.api;

import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.open.ApiResponse;
import cn.feiliu.taskflow.client.http.*;
import cn.feiliu.taskflow.client.http.types.TypeFactory;
import cn.feiliu.taskflow.open.dto.SaveScheduleRequest;
import cn.feiliu.taskflow.open.dto.WorkflowSchedule;
import cn.feiliu.taskflow.client.utils.Assertion;
import cn.feiliu.taskflow.client.utils.HttpHelper;
import cn.feiliu.taskflow.client.utils.SdkHelper;
import cn.feiliu.taskflow.open.dto.WorkflowScheduleExecution;
import cn.feiliu.taskflow.open.exceptions.ApiException;
import com.google.common.collect.Lists;
import com.squareup.okhttp.Call;

import java.util.ArrayList;
import java.util.List;

public class SchedulerResourceApi {
    private ApiClient apiClient;

    public SchedulerResourceApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 按名称删除调度计划
     */
    public Boolean deleteSchedule(String name) throws ApiException {
        String path = String.format("/scheduler/schedules/%s", SdkHelper.escapeString(name));
        Call call = apiClient.buildDeleteCall(path);
        ApiResponse<Boolean> resp = apiClient.execute(call, Boolean.class);
        return resp.getData();
    }

    /**
     * 根据工作流名称获取所有的调度计划
     *
     * @param workflowName 工作流名称
     */
    public List<WorkflowSchedule> getAllSchedules(String workflowName) throws ApiException {
        Assertion.assertNotNull(workflowName, "workflowName");
        String path = "/scheduler/schedules";
        List<Pair> queryParams = Lists.newArrayList(HttpHelper.parameterToPair("workflowName", workflowName));
        Call call = apiClient.buildGetCall(path, queryParams);
        ApiResponse<List<WorkflowSchedule>> resp = apiClient
            .doExecute(call, TypeFactory.ofList(WorkflowSchedule.class));
        return resp.getData();
    }

    /**
     * 获取调度程序的下一个x次(默认3次，最多5次)执行时间的列表
     *
     * @param cronExpression    (必填)
     * @param scheduleStartTime (可选)
     * @param scheduleEndTime   (可选)
     * @param limit             (可选, 默认 3)
     */
    public List<Long> getNextFewSchedules(String cronExpression, Long scheduleStartTime, Long scheduleEndTime,
                                          Integer limit) throws ApiException {
        Assertion.assertNotNull(cronExpression, "cronExpression");
        String localVarPath = "/scheduler/nextFewSchedules";
        List<Pair> queryParams = new ArrayList<Pair>();
        if (cronExpression != null)
            queryParams.addAll(HttpHelper.parameterToPair("cronExpression", cronExpression));
        if (scheduleStartTime != null)
            queryParams.addAll(HttpHelper.parameterToPair("scheduleStartTime", scheduleStartTime));
        if (scheduleEndTime != null)
            queryParams.addAll(HttpHelper.parameterToPair("scheduleEndTime", scheduleEndTime));
        if (limit != null)
            queryParams.addAll(HttpHelper.parameterToPair("limit", limit));

        Call call = apiClient.buildGetCall(localVarPath, queryParams);
        ApiResponse<List<Long>> resp = apiClient.doExecute(call, TypeFactory.ofList(Long.class));
        return resp.getData();
    }

    /**
     * 按名称获取现有工作流计划
     */
    public WorkflowSchedule getSchedule(String name) throws ApiException {
        String localVarPath = String.format("/scheduler/schedules/%s", SdkHelper.escapeString(name));
        Call call = apiClient.buildGetCall(localVarPath, new ArrayList<>());
        ApiResponse<WorkflowSchedule> resp = apiClient.execute(call, WorkflowSchedule.class);
        return resp.getData();
    }

    /**
     * 按名称暂停现有计划
     *
     * @param name 调度名称
     */
    public Boolean pauseSchedule(String name) throws ApiException {
        Assertion.assertNotNull(name, "name");
        String path = String.format("/scheduler/schedules/%s/pause", name);
        Call call = apiClient.buildGetCall(path, new ArrayList<>());
        ApiResponse<Boolean> response = apiClient.execute(call, Boolean.class);
        return response.getData();
    }

    /**
     * Resume a paused schedule by name
     *
     * @param name (required)
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
     *                      response body
     */
    public Boolean resumeSchedule(String name) throws ApiException {
        Assertion.assertNotNull(name, "name");
        String localVarPath = String.format("/scheduler/schedules/%s/resume", name);
        Call call = apiClient.buildGetCall(localVarPath, new ArrayList<>());
        ApiResponse<Boolean> response = apiClient.execute(call, Boolean.class);
        return response.getData();
    }

    /**
     * 使用相应的启动工作流请求为指定工作流创建或更新计划
     */
    public Boolean saveSchedule(SaveScheduleRequest saveScheduleRequest) throws ApiException {
        Assertion.assertNotNull(saveScheduleRequest, "saveScheduleRequest");
        String path = "/scheduler/schedules/save";
        List<Pair> queryParams = new ArrayList<>();
        Call call = apiClient.buildPostCall(path, saveScheduleRequest, queryParams);
        ApiResponse<Boolean> response = apiClient.execute(call, Boolean.class);
        return response.getData();
    }

    /**
     * 暂停所有调度计划
     */
    public Integer pauseAllSchedules() {
        String path = "/scheduler/schedules/pauseAll";
        Call call = apiClient.buildGetCall(path, new ArrayList<>());
        ApiResponse<Integer> response = apiClient.execute(call, Integer.class);
        return response.getData();
    }

    public Integer resumeAllSchedules() {
        String path = "/scheduler/schedules/resumeAll";
        Call call = apiClient.buildGetCall(path, new ArrayList<>());
        ApiResponse<Integer> response = apiClient.execute(call, Integer.class);
        return response.getData();
    }

    public List<WorkflowScheduleExecution> getAllExecutionRecords(Long start, Integer size) {
        String path = "/scheduler/executionRecords";
        List<Pair> queryParams = Lists.newArrayList(new Pair("start", start.toString()));
        if (size != null) {
            queryParams.add(new Pair("size", size.toString()));
        }
        Call call = apiClient.buildGetCall(path, queryParams);
        ApiResponse<List<WorkflowScheduleExecution>> resp = apiClient.doExecute(call,
            TypeFactory.ofList(WorkflowScheduleExecution.class));
        return resp.getData();
    }
}
