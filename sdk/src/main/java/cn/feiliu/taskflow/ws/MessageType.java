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
package cn.feiliu.taskflow.ws;

import java.util.Optional;

/**
 * WebSocket消息类型枚举
 */
public enum MessageType {
    /**
     * 连接建立确认
     */
    CONNECTION("connection"),
    /**
     * 心跳检测
     */
    PING("ping"),
    /**
     * 心跳响应
     */
    PONG("pong"),
    /**
     * 任务状态更新通知
     */
    SUB_TASK("sub_task"),
    /**
     * 错误消息
     */
    ERROR("error");

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * 根据字符串值获取对应的枚举
     *
     * @param value 字符串值
     * @return 对应的枚举，如果不存在则返回null
     */
    public static Optional<MessageType> fromValue(String value) {
        if (value != null) {
            for (MessageType type : values()) {
                if (type.value.equalsIgnoreCase(value)) {
                    return Optional.of(type);
                }

            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return value;
    }
}