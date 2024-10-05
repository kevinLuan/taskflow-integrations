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
import java.util.Objects;

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

    private TimerTaskTrigger(RepeatFrequency repeatFrequency) {
        this.repeatFrequency = repeatFrequency;
    }

    /**
     * 创建一个不重复的定时任务
     *
     * @return
     */
    public static TimerTaskTrigger newNoRepeat() {
        TimerTaskTrigger timerTaskTrigger = new TimerTaskTrigger();
        timerTaskTrigger.setRepeatFrequency(RepeatFrequency.NO_REPEAT);
        return timerTaskTrigger;
    }

    /**
     * 创建一个简单触发类型
     *
     * @param repeatFrequency
     * @return
     */
    public static TimerTaskTrigger newSimpleType(RepeatFrequency repeatFrequency) {
        if (repeatFrequency.isSimple()) {
            return new TimerTaskTrigger(repeatFrequency);
        }
        throw new IllegalArgumentException("repeatFrequency only for simple types");
    }

    /**
     * 每月的第几周的星期几
     *
     * @param weekOfMonth
     * @param dayOfWeek
     * @return
     */
    public static TimerTaskTrigger newMonthlyRelativeRepeat(Integer weekOfMonth, DayOfWeek dayOfWeek) {
        Objects.requireNonNull(dayOfWeek);
        if (weekOfMonth != null && weekOfMonth >= 1 && weekOfMonth <= 4) {
            TimerTaskTrigger timerTaskTrigger = new TimerTaskTrigger();
            timerTaskTrigger.setRepeatFrequency(RepeatFrequency.MONTHLY_RELATIVE);
            timerTaskTrigger.setWeekOfMonth(weekOfMonth);
            timerTaskTrigger.setDayOfWeek(dayOfWeek);
            return timerTaskTrigger;
        } else {
            throw new IllegalArgumentException("The weekOfMonth parameter ranges from 1 to 4");
        }
    }

    /**
     * 自定义间隔时间
     *
     * @param customRepeatInterval 取值范围：1 ~ 30
     * @param customRepeatUnit     间隔单位
     * @return
     */
    public static TimerTaskTrigger newCustomRepeat(Integer customRepeatInterval, CustomRepeatUnit customRepeatUnit) {
        Objects.requireNonNull(customRepeatUnit);
        if (customRepeatInterval != null && customRepeatInterval >= 1 && customRepeatInterval <= 30) {
            TimerTaskTrigger timerTaskTrigger = new TimerTaskTrigger();
            timerTaskTrigger.setRepeatFrequency(RepeatFrequency.CUSTOM);
            timerTaskTrigger.setCustomRepeatInterval(customRepeatInterval);
            timerTaskTrigger.setCustomRepeatUnit(customRepeatUnit);
            return timerTaskTrigger;
        } else {
            throw new IllegalArgumentException("The customRepeatInterval parameter ranges from 1 to 30");
        }
    }
}
