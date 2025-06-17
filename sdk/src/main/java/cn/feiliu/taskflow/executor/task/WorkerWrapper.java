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
package cn.feiliu.taskflow.executor.task;

import cn.feiliu.taskflow.annotations.WorkerTask;

import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * @author kevin.luan
 * @since 2025-06-08
 */
public class WorkerWrapper implements WorkerTask {
    private final WorkerTask workerTask;
    private final Worker     worker;

    private WorkerWrapper(WorkerTask workerTask, Worker worker) {
        this.workerTask = workerTask;
        this.worker = worker;
    }

    public static WorkerWrapper of(WorkerTask workerTask) {
        Objects.requireNonNull(workerTask, "workerTask is null");
        return new WorkerWrapper(workerTask, null);
    }

    public static WorkerWrapper of(Worker worker) {
        Objects.requireNonNull(worker, "worker is null");
        return new WorkerWrapper(null, worker);
    }

    @Override
    public String value() {
        if (workerTask != null) {
            return workerTask.value();
        } else {
            return worker.getTaskDefName();
        }
    }

    @Override
    public String tag() {
        if (workerTask != null) {
            return workerTask.tag();
        } else {
            return worker.getTag();
        }
    }

    @Override
    public String description() {
        if (workerTask != null) {
            return workerTask.description();
        } else {
            return worker.getDescription();
        }
    }

    @Override
    public int threadCount() {
        if (workerTask != null) {
            return Math.max(workerTask.threadCount(), 1);
        } else {
            return Math.max(worker.getThreadCount(), 1);
        }
    }

    @Override
    public int pollingInterval() {
        if (workerTask != null) {
            return Math.max(workerTask.pollingInterval(), 100);
        } else {
            return Math.max(worker.getPollingInterval(), 100);
        }
    }

    @Override
    public boolean open() {
        if (workerTask != null) {
            return workerTask.open();
        } else {
            return worker.isOpen();
        }
    }

    @Override
    public String domain() {
        if (workerTask != null) {
            return workerTask.domain();
        } else {
            return worker.getDomain();
        }
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return WorkerTask.class;
    }
}
