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

import cn.feiliu.taskflow.open.api.ISchedulerService;
import cn.feiliu.taskflow.open.exceptions.ApiException;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-06-30
 */
public interface ISchedulerClient extends ISchedulerService,AutoCloseable {

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
    default List<Date> getNextFewSchedules(String cronExpression, Date scheduleStartTime, Date scheduleEndTime,
                                           Integer limit) throws ApiException {
        List<Long> list = getNextFewSchedules(cronExpression, scheduleStartTime.getTime(), scheduleEndTime.getTime(), limit);
        return list.stream().map(Date::new).collect(Collectors.toList());
    }
}
