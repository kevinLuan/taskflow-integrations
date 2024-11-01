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
import cn.feiliu.taskflow.common.enums.DayOfWeek;
import cn.feiliu.taskflow.common.enums.RepeatFrequency;
import cn.feiliu.taskflow.common.utils.Assertions;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * 自定义规则
 * <pre>
 * 一次性触发: 年-月-日 十:分 执行一次 <p/>
 * 每小时重复:（规则：每小时的第 00 分钟 触发  -- 本次触发时间：2024/11/28, 周四, 12:00）<p/>
 * 每天重复:（规则：每天 12:00 触发 -- 本次触发时间：2024/11/28, 周四, 12:00）<p/>
 * 每周重复:（规则：每周四, 12:00 触发 -- 本次触发时间：2024/11/28, 周四, 12:00）<p/>
 * 每月重复: (规则：每月的 28 日 , 12:00 触发 -- 本次触发时间：2024/11/28, 周四, 12:00)<p/>
 * 每年重复: (规则：每年的 11 月 28 日, 12:00 触发 -- 本次触发时间：2024/11/28, 周四, 12:00)<p/>
 * 每个工作日: (规则：每个工作日（周一到周五）, 12:00 触发 -- 本次触发时间：2024/11/28, 周四, 12:00)<p/>
 * --------------------------
 * 自定义重复:
 * </pre>
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-10-04
 */
@Getter
@Setter
@ToString
public class FixedIntervalTrigger implements ITrigger {
    /**
     * 重复频率：每小时重复、每天、每周、每月、每年、每个工作日（周一~周五）
     * 除了自定义重复频率场景外无需关注一下字段
     */
    @NotNull(message = "Repeat frequency is required")
    private RepeatFrequency  repeatFrequency;
    /**
     * 仅在自定义重复频率场景下是，这个各取值范围[1~30] 需要结合自定义单位来确定时间
     */
    @Min(value = 1, message = "Custom repeat interval must be at least 1")
    @Max(value = 30, message = "Custom repeat interval must be at most 30")
    private Integer          customRepeatInterval;
    /***
     * 当重复频率设置为自定义频率时，对于自定义重复单位：[小时、天、年] 仅结合customRepeatInterval(自定义间隔时间)来确定执行频率
     */
    private CustomRepeatUnit customRepeatUnit;
    /**
     * 当自定义重复单位为周的时候，需要补充具体执行日期为星期几,取值范围:[周一 ~ 周五]可选多天
     * 当 customRepeatUnit 为周的时候，这里不能为空
     */
    private Set<DayOfWeek>   dayOfWeeks  = new HashSet<>();
    /**
     * 当自定义重复单位为月的时候，需要补充具体为该月份的几号
     * 例如：规则：每 x 个月的 1 日 3 日 5 日, 12:00 触发
     */
    private Set<Integer>     dayOfMonths = new HashSet<>();

    /*跳过周六日*/
    private boolean          skipWeekends;
    /*跳过节假日*/
    private boolean          skipHolidays;

    /**
     * 每月的第几周
     */
    public FixedIntervalTrigger() {

    }

    private FixedIntervalTrigger(RepeatFrequency repeatFrequency) {
        this.repeatFrequency = repeatFrequency;
    }

    /**
     * 创建一个不重复的定时任务
     *
     * @return
     */
    public static FixedIntervalTrigger newNoRepeat() {
        FixedIntervalTrigger timerTaskTrigger = new FixedIntervalTrigger();
        timerTaskTrigger.setRepeatFrequency(RepeatFrequency.NO_REPEAT);
        return timerTaskTrigger;
    }

    /**
     * 创建一个简单触发类型
     *
     * @param repeatFrequency
     * @return
     */
    public static FixedIntervalTrigger newSimpleType(RepeatFrequency repeatFrequency) {
        if (repeatFrequency.isSimple()) {
            return new FixedIntervalTrigger(repeatFrequency);
        }
        throw new IllegalArgumentException("repeatFrequency only for simple types");
    }

    /**
     * 自定义间隔时间
     *
     * @param customRepeatInterval 取值范围：1 ~ 30
     * @param customRepeatUnit     间隔单位
     * @return
     */
    public static FixedIntervalTrigger newCustomRepeat(Integer customRepeatInterval, CustomRepeatUnit customRepeatUnit) {
        Objects.requireNonNull(customRepeatUnit);
        Assertions.assertCustomRepeatInterval(customRepeatInterval);
        FixedIntervalTrigger timerTaskTrigger = new FixedIntervalTrigger();
        timerTaskTrigger.setRepeatFrequency(RepeatFrequency.CUSTOM);
        timerTaskTrigger.setCustomRepeatInterval(customRepeatInterval);
        timerTaskTrigger.setCustomRepeatUnit(customRepeatUnit);
        return timerTaskTrigger;
    }

    /**
     * 创建X周重试触发器
     *
     * @param customRepeatInterval 自定义间隔周数量,取值范围:[1 ~ 30]
     * @param dayOfWeeks           [周一 ~ 周五]
     * @return
     */
    public static FixedIntervalTrigger newWeekRepeat(Integer customRepeatInterval, DayOfWeek... dayOfWeeks) {
        Assertions.assertCustomRepeatInterval(customRepeatInterval);
        Assertions.assertDayOfWeeks(dayOfWeeks);
        Objects.requireNonNull(dayOfWeeks);
        FixedIntervalTrigger timerTaskTrigger = new FixedIntervalTrigger();
        timerTaskTrigger.setRepeatFrequency(RepeatFrequency.CUSTOM);
        timerTaskTrigger.setCustomRepeatUnit(CustomRepeatUnit.WEEK);
        timerTaskTrigger.setCustomRepeatInterval(customRepeatInterval);
        timerTaskTrigger.setDayOfWeeks(Set.of(dayOfWeeks));
        return timerTaskTrigger;
    }

    /**
     * 创建X月重试触发器
     *
     * @param customRepeatInterval 自定义间隔周数量,取值范围:[1 ~ 30]
     * @param dayOfMonths          [周一 ~ 周五]
     * @return
     */
    public static FixedIntervalTrigger newMonthRepeat(Integer customRepeatInterval, Integer... dayOfMonths) {
        Assertions.assertCustomRepeatInterval(customRepeatInterval);
        Assertions.assertDayOfMonths(dayOfMonths);
        FixedIntervalTrigger timerTaskTrigger = new FixedIntervalTrigger();
        timerTaskTrigger.setRepeatFrequency(RepeatFrequency.CUSTOM);
        timerTaskTrigger.setCustomRepeatUnit(CustomRepeatUnit.MONTH);
        timerTaskTrigger.setCustomRepeatInterval(customRepeatInterval);
        timerTaskTrigger.setDayOfMonths(Set.of(dayOfMonths));
        return timerTaskTrigger;
    }
}
