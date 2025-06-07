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
package cn.feiliu.taskflow.automator;

import cn.feiliu.taskflow.automator.scheduling.PollStatus;
import cn.feiliu.taskflow.executor.task.Worker;
import io.netty.util.TimerTask;

/**
 * @author kevin.luan
 * @since 2025-06-07
 */
public abstract class WorkerProcess {
    /**
     * 任务处理器
     *
     * @param timerTask
     * @param worker
     * @return
     */
    public abstract PollStatus process(TimerTask timerTask, Worker worker);
}
