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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

/**
 * WebSocket重连功能测试
 * 用于验证服务端重启时客户端的自动重连行为
 *
 * @author kevin.luan
 * @since 2025-06-06
 */
public class ReconnectTest {

    private static final Logger logger = LoggerFactory.getLogger(ReconnectTest.class);

    public static void main(String[] args) throws InterruptedException {
        logger.info("开始WebSocket重连测试");
        // 服务器配置
        String serverUrl = "ws://localhost:8082";
        String keyId = "197300d502f";
        String keySecret = "0c0fb021b6f04bbeaae4d3d414423b4a";
//        keyId="19748a08697";//本地
//        keySecret="625ed138e32f4a639daa0c82567eebb9";
        String userId = WebSocketClient.generateUniqueUserId(keyId);

        // 创建自动重连客户端
        AutoReconnectClient client = new AutoReconnectClient(serverUrl, userId, keyId, keySecret,(message)->{
            logger.info("收到服务器消息: type={}, message={}, data={}",
                    message.getType(), message.getDescription(), message.getData());
            Optional<MessageType> optional = MessageType.fromValue(message.getType());
            if (optional.isPresent()) {
                if (optional.get() == MessageType.CONNECTION) {
                    logger.info("✅ 连接建立确认: {}", message.getDescription());
                } else if (optional.get() == MessageType.PONG) {
                    logger.debug("❤️ 收到心跳响应");
                } else if (optional.get() == MessageType.SUB_TASK) {
                    SubTaskPayload payload = message.getData(SubTaskPayload.class);
                    logger.info("您有新的任务待处理:{}", payload.getTaskNames());
                }
            } else {
                logger.error("未支持的消息类型:{}", message.getType());
            }
        });

        // 设置连接成功回调
        client.setOnConnectedCallback(() -> {
            logger.info("🎉 WebSocket连接成功建立");
            // 订阅消息
            client.subTasks(Arrays.asList(MessageType.SUB_TASK.getValue()));
            // 发送心跳
            client.sendPing();
        });

        // 启动连接
        logger.info("正在连接到服务器: {}", serverUrl);
        client.connect();

        // 运行10分钟，期间可以重启服务端测试重连
        logger.info("客户端已启动，将运行10分钟");
        logger.info("现在可以重启服务端来测试自动重连功能");
        Thread.sleep(600000); // 10分钟

        // 清理资源
        logger.info("测试结束，关闭客户端");
        client.stop();
    }
}