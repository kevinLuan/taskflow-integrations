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
package cn.feiliu.taskflow.ws.handler;

import cn.feiliu.taskflow.ws.WebSocketMessageHandler;
import cn.feiliu.taskflow.ws.msg.WebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简单的消息处理器实现
 */
public class SimpleMessageHandler implements WebSocketMessageHandler {
    static final Logger logger = LoggerFactory.getLogger(SimpleMessageHandler.class);

    @Override
    public void onConnected() {
        logger.info("WebSocket已连接");
    }

    @Override
    public void onDisconnected(int code, String reason) {
        logger.info("WebSocket已断开: code={}, reason={}", code, reason);
    }

    @Override
    public void onDisconnecting(int code, String reason) {
        logger.info("WebSocket正在断开: code={}, reason={}", code, reason);
    }

    @Override
    public void onMessage(WebSocketMessage message) {
        logger.info("收到消息: type={}, message={}", message.getType(), message.getDescription());
    }

    @Override
    public void onError(Throwable error) {
        logger.error("WebSocket错误", error);
    }
}