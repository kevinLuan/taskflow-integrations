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
package cn.feiliu.taskflow.open.dto.trigger;

import cn.feiliu.taskflow.common.enums.CustomRepeatUnit;
import cn.feiliu.taskflow.common.enums.RepeatFrequency;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.DayOfWeek;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-10-04
 */
@Getter
@Setter
@ToString
public class TimerTaskTrigger implements ITrigger {
    @NotNull(message = "Repeat frequency is required")
    private RepeatFrequency  repeatFrequency;

    @Min(value = 1, message = "Custom repeat interval must be at least 1")
    @Max(value = 30, message = "Custom repeat interval must be at most 30")
    private Integer          customRepeatInterval;
    /*自定义重复单位(备注：该字段需要结合：customRepeatInterval 和 RepeatFrequency.CUSTOM)*/
    private CustomRepeatUnit customRepeatUnit;
    /*跳过周六日*/
    private boolean          skipWeekends;
    /*跳过节假日*/
    private boolean          skipHolidays;
    /**
     * 每月的第几周
     */
    @Min(value = 1, message = "Week of month must be between 1 and 5")
    @Max(value = 5, message = "Week of month must be between 1 and 5")
    private Integer          weekOfMonth;
    /*每月的第几周的星期几*/
    private DayOfWeek        dayOfWeek;

    public TimerTaskTrigger() {
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private final TimerTaskTrigger conf = new TimerTaskTrigger();

        public Builder repeatFrequency(RepeatFrequency repeatFrequency) {
            conf.repeatFrequency = repeatFrequency;
            return this;
        }

        public Builder customRepeatInterval(Integer customRepeatInterval) {
            conf.customRepeatInterval = customRepeatInterval;
            return this;
        }

        public Builder customRepeatUnit(CustomRepeatUnit customRepeatUnit) {
            conf.customRepeatUnit = customRepeatUnit;
            return this;
        }

        public Builder skipWeekends(boolean skipWeekends) {
            conf.skipWeekends = skipWeekends;
            return this;
        }

        public Builder skipHolidays(boolean skipHolidays) {
            conf.skipHolidays = skipHolidays;
            return this;
        }

        public Builder weekOfMonth(Integer weekOfMonth) {
            conf.weekOfMonth = weekOfMonth;
            return this;
        }

        public Builder dayOfWeek(DayOfWeek dayOfWeek) {
            conf.dayOfWeek = dayOfWeek;
            return this;
        }

        public TimerTaskTrigger build() {
            return conf;
        }
    }
}
