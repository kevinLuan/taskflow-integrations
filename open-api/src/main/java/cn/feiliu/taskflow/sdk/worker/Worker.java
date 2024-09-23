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
package cn.feiliu.taskflow.sdk.worker;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Function;

import cn.feiliu.taskflow.sdk.config.WorkerPropertyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.feiliu.taskflow.common.metadata.tasks.ExecutingTask;
import cn.feiliu.taskflow.common.metadata.tasks.TaskExecResult;

public interface Worker {
    Logger logger = LoggerFactory.getLogger(Worker.class);

    /**
     * Retrieve the name of the task definition the worker is currently working on.
     *
     * @return the name of the task definition.
     */
    String getTaskDefName();

    /**
     * Executes a task and returns the updated task.
     *
     * @param task Task to be executed.
     * @return the {@link TaskExecResult} object If the task is not completed yet, return with the
     * status as IN_PROGRESS.
     */
    TaskExecResult execute(ExecutingTask task)throws Throwable;

    /**
     * Called when the task coordinator fails to update the task to the server. Client should store
     * the task id (in a database) and retry the update later
     *
     * @param task Task which cannot be updated back to the server.
     */
    default void onErrorUpdate(ExecutingTask task) {
    }

    /**
     * Override this method to pause the worker from polling.
     *
     * @return true if the worker is paused and no more tasks should be polled from server.
     */
    default boolean paused() {
        return WorkerPropertyManager.paused(getTaskDefName());
    }

    /**
     * Override this method to app specific rules.
     *
     * @return returns the serverId as the id of the instance that the worker is running.
     */
    default String getIdentity() {
        String serverId;
        try {
            serverId = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            serverId = System.getenv("HOSTNAME");
        }
        if (serverId == null) {
            serverId = System.getProperty("user.name");
        }
        logger.debug("Setting worker id to {}", serverId);
        return serverId;
    }

    /**
     * Override this method to change the interval between polls.
     *
     * @return interval in millisecond at which the server should be polled for worker tasks.
     */
    default int getPollingInterval() {
        return WorkerPropertyManager.getPollingInterval(getTaskDefName());
    }

    default boolean leaseExtendEnabled() {
        return WorkerPropertyManager.leaseExtendEnabled(getTaskDefName());
    }

    default int getBatchPollTimeoutInMS() {
        return WorkerPropertyManager.getBatchPollTimeoutInMS(getTaskDefName());
    }

    static Worker create(String taskType, Function<ExecutingTask, TaskExecResult> executor) {
        return new Worker() {

            @Override
            public String getTaskDefName() {
                return taskType;
            }

            @Override
            public TaskExecResult execute(ExecutingTask task) throws Throwable{
                return executor.apply(task);
            }

            @Override
            public boolean paused() {
                return Worker.super.paused();
            }
        };
    }
}
