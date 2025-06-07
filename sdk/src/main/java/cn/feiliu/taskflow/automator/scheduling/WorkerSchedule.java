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
package cn.feiliu.taskflow.automator.scheduling;

import cn.feiliu.taskflow.executor.task.Worker;
import io.netty.util.Timeout;

/**
 * @author kevin.luan
 * @since 2025-06-07
 */
class WorkerSchedule {
    private Worker  worker;
    private Timeout timeout;

    private WorkerSchedule(Worker worker, Timeout timeout) {
        this.worker = worker;
        this.timeout = timeout;
    }

    public static WorkerSchedule of(Worker worker, Timeout timeout) {
        return new WorkerSchedule(worker, timeout);
    }

    public Worker getWorker() {
        return worker;
    }

    /**
     * 触发执行
     *
     * @throws Exception
     */
    public void triggerExecute() throws Exception {
        timeout.cancel();
        timeout.task().run(timeout);
    }
}
