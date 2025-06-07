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

import cn.feiliu.taskflow.ws.msg.SubTaskPayload;
import cn.feiliu.taskflow.ws.msg.WebSocketMessage;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * @author kevin.luan
 * @since 2025-06-06
 */
public class MyTest {
    static Logger logger = LoggerFactory.getLogger(MyTest.class);

    private static void handleTaskStatusUpdate(WebSocketMessage message) {
        List<String> taskNames = message.getData(SubTaskPayload.class).getTaskNames();
        logger.info("处理任务状态更新: {}", taskNames);
        // 在这里添加具体的任务状态更新处理逻辑
        // 例如：更新本地缓存、刷新UI等
    }

    public static void main(String[] args) throws InterruptedException {
        // 服务器地址和用户ID
        String serverUrl = "ws://localhost:8082"; // 根据实际服务器地址修改
        String keyId = "197300d502f";
        String keySecret = "0c0fb021b6f04bbeaae4d3d414423b4a";
        String userId = WebSocketClient.generateUniqueUserId(keyId);
        AutoReconnectClient client = new AutoReconnectClient(serverUrl, userId, keyId, keySecret, (message) -> {
            logger.info("收到服务器消息: type={}, message={}, data={}",
                    message.getType(), message.getDescription(),
                    message.getData());
            Optional<MessageType> optional = MessageType.fromValue(message
                    .getType());
            if (optional.isPresent()) {
                switch (optional.get()) {
                    case SUB_TASK:
                        handleTaskStatusUpdate(message);
                        break;
                    case CONNECTION:
                        logger.info("连接建立确认: {}", message.getDescription());
                        break;
                    case PONG:
                        logger.debug("收到心跳响应");
                        break;
                    case ERROR:
                        logger.error("收到服务器错误消息: {}",
                                message.getDescription());
                        break;
                    default:
                        logger.debug("未实现类型消息: {}", optional.get());
                        break;
                }
            } else {
                logger.warn("收到未知的消息类型: {}, 已忽略", message.getType());
            }
        });
        client.setOnConnectedCallback(() -> {
            logger.info("WebSocket连接成功建立");
            // 订阅特定类型的消息
            List<String> tasks = Lists.newArrayList("task1", "task2", "task3");
            client.subTasks(tasks);
            // 发送心跳消息
            client.sendPing();
        });
        client.connect();
        Thread.sleep(600000);
        client.stop();
    }
}
