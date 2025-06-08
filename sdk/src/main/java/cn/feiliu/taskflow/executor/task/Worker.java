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


import cn.feiliu.taskflow.common.dto.tasks.ExecutingTask;
import cn.feiliu.taskflow.common.dto.tasks.TaskExecResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public interface Worker {
    Logger logger = LoggerFactory.getLogger(Worker.class);

    /**
     * 获取工作者当前正在执行的任务定义的名称
     *
     * @return 任务定义的名称
     */
    String getTaskDefName();

    /**
     * 执行任务并返回更新后的任务
     *
     * @param task 要执行的任务
     * @return {@link TaskExecResult} 对象。如果任务尚未完成，返回状态为 IN_PROGRESS
     */
    TaskExecResult execute(ExecutingTask task) throws Throwable;

    /**
     * 当任务协调器无法将任务更新到服务器时调用。客户端应该存储任务ID（在数据库中）
     * 并在稍后重试更新
     *
     * @param task 无法更新回服务器的任务
     */
    default void onErrorUpdate(ExecutingTask task) {
    }

    /**
     * 重写此方法以实现应用程序特定的规则
     *
     * @return 返回工作者实例运行的服务器ID作为标识
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
        logger.debug("设置工作者ID为 {}", serverId);
        return serverId;
    }

    /**
     * 重写此方法以更改轮询间隔
     *
     * @return 服务器应该被轮询工作者任务的时间间隔（毫秒）
     */
    default int getPollingInterval() {
        return (int) TimeUnit.SECONDS.toMillis(1);
    }

    /**
     * 获取输入参数名称
     *
     * @return
     */
    Optional<String[]> getInputNames();

    /**
     * 获取输出名称
     *
     * @return
     */
    Optional<String[]> getOutputNames();

    /**
     * 节点标签名称
     *
     * @return
     */
    String getTag();

    /**
     * 节点描述
     *
     * @return
     */
    String getDescription();

    default String getDomain() {
        return null;
    }

    /**
     * 获取执行线程数量
     *
     * @return
     */
    default int getThreadCount() {
        return 1;
    }

}
