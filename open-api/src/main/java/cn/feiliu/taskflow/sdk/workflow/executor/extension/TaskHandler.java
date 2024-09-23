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
package cn.feiliu.taskflow.sdk.workflow.executor.extension;

import cn.feiliu.taskflow.sdk.workflow.task.WorkerTask;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 任务处理处理
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-02-29
 */
public class TaskHandler {
    private final WorkerTask worker;
    private final Object     bean;
    private final Method     workerMethod;

    public TaskHandler(WorkerTask worker, Object bean, Method workerMethod) {
        this.worker = Objects.requireNonNull(worker, "worker is null");
        this.bean = Objects.requireNonNull(bean, "bean is null");
        this.workerMethod = Objects.requireNonNull(workerMethod, "workerMethod is null");
    }

    public WorkerTask getWorker() {
        return worker;
    }

    public Object getBean() {
        return bean;
    }

    public Method getWorkerMethod() {
        return workerMethod;
    }

    /**
     * 是否有返回值
     *
     * @return
     */
    public boolean hasReturn() {
        return !workerMethod.getReturnType().equals(Void.TYPE);
    }
}
