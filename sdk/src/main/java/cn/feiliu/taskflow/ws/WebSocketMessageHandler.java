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

import cn.feiliu.taskflow.ws.msg.WebSocketMessage;

/**
 * WebSocket消息处理器接口
 */
public interface WebSocketMessageHandler {

    /**
     * 连接建立时调用
     */
    void onConnected();

    /**
     * 连接断开时调用
     */
    void onDisconnected(int code, String reason);

    /**
     * 连接正在断开时调用
     */
    void onDisconnecting(int code, String reason);

    /**
     * 收到消息时调用
     */
    void onMessage(WebSocketMessage message);

    /**
     * 发生错误时调用
     */
    void onError(Throwable error);
}
