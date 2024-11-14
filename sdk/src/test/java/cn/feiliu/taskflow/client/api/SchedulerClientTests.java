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
package cn.feiliu.taskflow.client.api;

import cn.feiliu.taskflow.common.DateTimeOps;
import cn.feiliu.taskflow.common.enums.TriggerType;
import cn.feiliu.taskflow.common.metadata.workflow.StartWorkflowRequest;
import cn.feiliu.taskflow.open.dto.SaveScheduleRequest;
import cn.feiliu.taskflow.open.dto.WorkflowSchedule;
import cn.feiliu.taskflow.open.dto.WorkflowScheduleExecution;
import cn.feiliu.taskflow.open.dto.trigger.CronTrigger;
import cn.feiliu.taskflow.open.exceptions.ApiException;
import lombok.SneakyThrows;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import static cn.feiliu.taskflow.client.api.BaseClientApi.*;

import org.junit.Assert;
import org.junit.Test;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class SchedulerClientTests {
    private final String NAME            = "test_java_sdk_scheduler";
    private final String WORKFLOW_NAME   = "test-scheduler-workflow";
    private final String CRON_EXPRESSION = "0 0/15 * * * ?";

    @Test
    public void testMethods() {
        getSchedulerClient().deleteSchedule(NAME);
        assertTrue(getSchedulerClient().getNextFewSchedules(CRON_EXPRESSION, 0L, 0L, 5).isEmpty());
        Long start = System.currentTimeMillis();
        Long endTime = start + TimeUnit.HOURS.toMillis(1);
        List<Long> dates = getSchedulerClient().getNextFewSchedules(CRON_EXPRESSION, start, endTime, 5);
        assertEquals(4, dates.size());
        for (Long date : dates) {
            System.out.println(DateTimeOps.ofSys(date).toString());
        }
        SaveScheduleRequest request = getSaveScheduleRequest(false);
        getSchedulerClient().saveSchedule(request);
        assertFalse(getSchedulerClient().getAllSchedules(WORKFLOW_NAME).isEmpty());
        WorkflowSchedule workflowSchedule = getSchedulerClient().getSchedule(NAME);
        assertEquals(NAME, workflowSchedule.getName());

        assertEquals(CRON_EXPRESSION, workflowSchedule.getCronTrigger().getCronExpr());
        getSchedulerClient().pauseSchedule(NAME);
        workflowSchedule = getSchedulerClient().getSchedule(NAME);
        assertTrue(workflowSchedule.isPaused());
        getSchedulerClient().resumeSchedule(NAME);
        workflowSchedule = getSchedulerClient().getSchedule(NAME);
        assertFalse(workflowSchedule.isPaused());
        getSchedulerClient().deleteSchedule(NAME);
    }

    @Test
    public void testConflictException() {
        try {
            getSchedulerClient().saveSchedule(getSaveScheduleRequest(true));
            getSchedulerClient().saveSchedule(getSaveScheduleRequest(false));
            Assert.fail("未出现预期结果");
        } catch (ApiException e) {
            System.out.println(e.getMessage());
            Assert.assertEquals(409, e.getStatusCode());
        }
    }

    @Test
    public void testAllMethods() {
        SaveScheduleRequest req = getSaveScheduleRequest(true);
        getSchedulerClient().saveSchedule(req);
        getSchedulerClient().pauseAllSchedules();
        getSchedulerClient().resumeAllSchedules();
        List<WorkflowScheduleExecution> records = getSchedulerClient().getAllExecutionRecords(0L, 100);
        getSchedulerClient().deleteSchedule(NAME);

    }

    @Test
    public void testNextFewSchedules() {
        //使用纽约时间：10:30 ~ 12:30
        SaveScheduleRequest req = getSaveScheduleRequest(true);
        String cron = req.getCronTrigger().getCronExpr();
        long start = req.getStartTime();
        long end = req.getEndTime();
        List<Long> exeTimes = getSchedulerClient().getNextFewSchedules(cron, start, end, 10);
        System.out.println("cronExpr: " + cron);
        System.out.println("开始时间: " + DateTimeOps.of(req.getTimeZone(), start));
        System.out.println("结束时间: " + DateTimeOps.of(req.getTimeZone(), end));
        System.out.println("===============");
        for (Long exeTime : exeTimes) {
            System.out.println("执行时间: "
                               + DateTimeOps.of(req.getTimeZone(), exeTime).format(DateTimeOps.WITHOUT_ZONE_FMT));
        }
        Assert.assertEquals(8, exeTimes.size());
    }

    SaveScheduleRequest getSaveScheduleRequest(boolean overwrite) {
        ZoneId zoneId = ZoneId.of("America/New_York");
        SaveScheduleRequest request = new SaveScheduleRequest();
        request.setName(NAME);
        request.setTimeZone(zoneId.getId());
        request.setTriggerType(TriggerType.CRON);
        request.setCronTrigger(new CronTrigger(CRON_EXPRESSION));
        request.setStartWorkflowRequest(StartWorkflowRequest.of(WORKFLOW_NAME, 1));
        DateTimeOps dateTimeOps = getStartTime(zoneId);
        request.setStartTime(dateTimeOps.getMillis());
        request.setEndTime(dateTimeOps.addHours(2).getMillis());
        request.setOverwrite(overwrite);
        return request;
    }

    @SneakyThrows
    private DateTimeOps getStartTime(ZoneId zoneId) {
        //使用本地日期的10点30分作为开始时间
        String originalTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd") + " 10:30:00";
        return DateTimeOps.parse(zoneId, DateTimeOps.WITHOUT_ZONE_FMT, originalTime);
    }
}
