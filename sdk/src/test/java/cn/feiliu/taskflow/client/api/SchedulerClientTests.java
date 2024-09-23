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
package cn.feiliu.taskflow.client.api;

import cn.feiliu.taskflow.common.metadata.workflow.StartWorkflowRequest;
import cn.feiliu.taskflow.open.dto.SaveScheduleRequest;
import cn.feiliu.taskflow.open.dto.WorkflowSchedule;
import cn.feiliu.taskflow.open.dto.WorkflowScheduleExecution;
import cn.feiliu.taskflow.open.exceptions.ApiException;
import org.apache.commons.lang3.time.DateUtils;

import static cn.feiliu.taskflow.client.api.BaseClientApi.*;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class SchedulerClientTests {
    private final String NAME            = "test_java_sdk_scheduler";
    private final String WORKFLOW_NAME   = "test-scheduler-workflow";
    private final String CRON_EXPRESSION = "0 0/15 * * * ?";

    @Test
    void testMethods() {
        getSchedulerClient().deleteSchedule(NAME);
        assertTrue(getSchedulerClient().getNextFewSchedules(CRON_EXPRESSION, 0L, 0L, 5).isEmpty());
        Date now = new Date();
        Date endTime = DateUtils.addHours(now, 1);
        List<Date> dates = getSchedulerClient().getNextFewSchedules(CRON_EXPRESSION, now, endTime, 5);
        assertEquals(4, dates.size());
        for (Date date : dates) {
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
        }
        getSchedulerClient().saveSchedule(getSaveScheduleRequest(false));
        assertEquals(1, getSchedulerClient().getAllSchedules(WORKFLOW_NAME).size());
        WorkflowSchedule workflowSchedule = getSchedulerClient().getSchedule(NAME);
        assertEquals(NAME, workflowSchedule.getName());
        assertEquals(CRON_EXPRESSION, workflowSchedule.getCronExpression());
        getSchedulerClient().pauseSchedule(NAME);
        workflowSchedule = getSchedulerClient().getSchedule(NAME);
        assertTrue(workflowSchedule.isPaused());
        getSchedulerClient().resumeSchedule(NAME);
        workflowSchedule = getSchedulerClient().getSchedule(NAME);
        assertFalse(workflowSchedule.isPaused());
        getSchedulerClient().deleteSchedule(NAME);
    }

    @Test
    void testConflictException() {
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
    void testAllMethods() {
        getSchedulerClient().saveSchedule(getSaveScheduleRequest(true));
        getSchedulerClient().pauseAllSchedules();
        getSchedulerClient().resumeAllSchedules();
        List<WorkflowScheduleExecution> records = getSchedulerClient().getAllExecutionRecords(0L, 100);
        System.out.println(records.size());
        getSchedulerClient().deleteSchedule(NAME);

    }

    SaveScheduleRequest getSaveScheduleRequest(boolean overwrite) {
        SaveScheduleRequest request = new SaveScheduleRequest();
        request.setName(NAME);
        request.setCronExpression(CRON_EXPRESSION);
        request.setStartWorkflowRequest(StartWorkflowRequest.of(WORKFLOW_NAME, 1));
        request.setScheduleStartTime(new Date());
        request.setScheduleEndTime(DateUtils.addDays(new Date(), 1));
        request.setOverwrite(overwrite);
        return request;
    }
}
