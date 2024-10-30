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
package cn.feiliu.taskflow.common.utils;

import java.time.DayOfWeek;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-10-26
 */
public class Assertions {
    /**
     * 断言自定义间隔时间
     *
     * @param customRepeatInterval
     */
    public static void assertCustomRepeatInterval(int customRepeatInterval) {
        if (!Validator.isValidDayOfMonth30(customRepeatInterval)) {
            throw new IllegalArgumentException("The customRepeatInterval parameter ranges from 1 to 30");
        }
    }

    public static void assertDayOfMonths(Integer... dayOfMonths) {
        if (dayOfMonths.length >= 1 && dayOfMonths.length <= 31) {
            for (int dayOfMonth : dayOfMonths) {
                if (!Validator.isValidDayOfMonth(dayOfMonth)) {
                    throw new IllegalArgumentException("The dayOfMonth parameter ranges from 1 to 31");
                }
            }
        } else {
            throw new IllegalArgumentException("dayOfMonths Invalid parameter");
        }

    }

    public static void assertDayOfWeeks(DayOfWeek... dayOfWeeks) {
        if (dayOfWeeks == null || dayOfWeeks.length < 0 || dayOfWeeks.length > 7) {
            throw new IllegalArgumentException("dayOfWeeks Invalid parameter");
        }
    }
}
