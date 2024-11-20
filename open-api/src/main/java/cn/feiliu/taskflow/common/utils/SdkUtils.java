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

import cn.feiliu.taskflow.sdk.worker.Worker;
import lombok.extern.slf4j.Slf4j;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-09-04
 */
@Slf4j
public class SdkUtils {
    /**
     * 获取拉取任务超时时间，最小100ms，最大为1000ms
     *
     * @param worker
     * @return
     */
    public static int getReasonableTimeout(Worker worker) {
        int timeout = worker.getBatchPollTimeoutInMS();
        if (timeout <= 100) {
            return 100;
        } else if (timeout > 1000) {
            return 1000;
        } else {
            return timeout;
        }
    }
}
