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

import cn.feiliu.common.api.utils.MapBuilder;
import cn.feiliu.taskflow.ws.handler.MessageProcessHandler;
import cn.feiliu.taskflow.ws.handler.SimpleMessageHandler;
import cn.feiliu.taskflow.ws.msg.WebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 创建一个简单的自动重连客户端示例
 *
 * @author kevin.luan
 * @since 2025-06-06
 */
public class AutoReconnectClient {

    private static final Logger logger = LoggerFactory.getLogger(AutoReconnectClient.class);
    private static final int MAX_RECONNECT_ATTEMPTS = Integer.MAX_VALUE;
    private static final long RECONNECT_DELAY_MS = 5000;                                              // 5秒重连间隔

    private WebSocketClient client;
    private String serverUrl;
    private String userId;
    private String keyId;
    private String keySecret;
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private volatile boolean shouldReconnect = true;
    private MessageProcessHandler messageProcessHandler;

    // 同步控制：确保同一时间只有一个重连任务
    private final AtomicBoolean reconnecting = new AtomicBoolean(false);
    private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "AutoReconnect-Thread");
        t.setDaemon(true);
        return t;
    });

    // 连接成功后的回调处理器
    private volatile Runnable onConnectedCallback;

    public AutoReconnectClient(String serverUrl, String userId, String keyId, String keySecret,
                               MessageProcessHandler handler) {
        this.serverUrl = serverUrl;
        this.userId = userId;
        this.keyId = keyId;
        this.keySecret = keySecret;
        this.messageProcessHandler = handler;
        this.client = createClient();
    }

    private WebSocketClient createClient() {
        return new WebSocketClient(serverUrl, userId, keyId, keySecret,
                new SimpleMessageHandler() {
                    @Override
                    public void onConnected() {
                        logger.info("🎉 WebSocket连接成功，重置重连计数");
                        reconnectAttempts.set(0);
                        reconnecting.set(false); // 连接成功，重置重连状态
                        shouldReconnect = true;

                        // 执行连接成功回调
                        if (onConnectedCallback != null) {
                            try {
                                onConnectedCallback.run();
                            } catch (Exception e) {
                                logger.error("执行连接成功回调时发生异常", e);
                            }
                        }
                    }

                    @Override
                    public void onDisconnected(int code, String reason) {
                        logger.warn("💔 WebSocket连接断开: code={}, reason={}", code, reason);
                        triggerReconnectIfNeeded();
                    }

                    @Override
                    public void onMessage(WebSocketMessage message) {
                        if (messageProcessHandler != null) {
                            messageProcessHandler.onMessage(message);
                        } else {
                            super.onMessage(message);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        logger.error("❌ WebSocket连接错误", error);
                        triggerReconnectIfNeeded();
                    }
                });
    }

    /**
     * 触发重连（如果需要且未在重连中）
     */
    private void triggerReconnectIfNeeded() {
        if (shouldReconnect && reconnectAttempts.get() < MAX_RECONNECT_ATTEMPTS) {
            scheduleReconnect();
        }
    }

    /**
     * 连接到WebSocket服务器
     *
     * @return
     */
    public CompletableFuture<Void> connect() {
        // 如果正在重连中，则不重复执行初始连接
        if (reconnecting.get()) {
            logger.info("正在重连中，忽略新的连接请求");
            return CompletableFuture.completedFuture(null);
        }

        return client.connect().thenAccept(success -> {
            if (!success && shouldReconnect && reconnectAttempts.get() < MAX_RECONNECT_ATTEMPTS) {
                scheduleReconnect();
            }
        });
    }

    private void scheduleReconnect() {
        // 使用CAS操作确保同一时间只有一个重连任务
        if (!reconnecting.compareAndSet(false, true)) {
            logger.debug("🔄 重连任务已在执行中，忽略本次重连请求");
            return;
        }

        int currentAttempt = reconnectAttempts.incrementAndGet();
        logger.info("🔄 准备第{}次重连，{}秒后开始", currentAttempt, RECONNECT_DELAY_MS / 1000);

        // 使用ScheduledExecutorService替代手动创建线程，更好的资源管理
        reconnectExecutor.schedule(() -> {
            try {
                if (shouldReconnect) {
                    logger.info("🚀 开始第{}次重连", currentAttempt);
                    if (client != null) {// 关闭旧连接
                        client.close();
                    }
                    // 创建新客户端
                    client = createClient();
                    // 尝试连接
                    CompletableFuture<Boolean> connectFuture = client.connect();
                    // 等待连接结果，避免立即触发下一次重连
                    connectFuture.thenAccept(success -> {
                        if (!success && shouldReconnect && reconnectAttempts.get() < MAX_RECONNECT_ATTEMPTS) {
                            logger.warn("🔴 第{}次重连失败，准备下次重连", currentAttempt);
                            // 连接失败，重置重连状态以允许下次重连
                            reconnecting.set(false);
                            scheduleReconnect();
                        }
                        // 连接成功的情况在onConnected回调中处理
                    }).exceptionally(throwable -> {
                        logger.error("💥 重连过程中发生异常", throwable);
                        reconnecting.set(false);
                        if (shouldReconnect && reconnectAttempts.get() < MAX_RECONNECT_ATTEMPTS) {
                            scheduleReconnect();
                        }
                        return null;
                    });
                } else {
                    logger.info("⏹️ 重连已停止");
                    reconnecting.set(false);
                }
            } catch (Exception e) {
                logger.error("💥 重连执行异常", e);
                reconnecting.set(false);
                if (shouldReconnect && reconnectAttempts.get() < MAX_RECONNECT_ATTEMPTS) {
                    scheduleReconnect();
                }
            }
        }, RECONNECT_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        shouldReconnect = false;
        reconnecting.set(false);

        // 关闭重连线程池
        reconnectExecutor.shutdown();
        try {
            if (!reconnectExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                reconnectExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            reconnectExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        if (client != null) {
            client.close();
        }
    }

    /**
     * 发送消息到服务器
     */
    public boolean sendMessage(String type, String message, Map<String, Object> data) {
        return client != null && client.sendMessage(type, message, data);
    }

    /**
     * 发送心跳消息
     */
    public boolean sendPing() {
        return sendMessage(MessageType.PING.getValue(), "心跳检测", null);
    }

    /**
     * 订阅特定类型的消息
     */
    public boolean subTasks(List<String> tasks) {
        Map<String, Object> data = MapBuilder.newBuilder().put("tasks", tasks).build();
        return sendMessage(MessageType.SUB_TASK.getValue(), "订阅消息", data);
    }

    /**
     * 检查连接状态
     */
    public boolean isConnected() {
        return this.client != null && this.client.isConnected();
    }

    /**
     * 设置连接成功后的回调处理器
     *
     * @param callback 连接成功后执行的回调
     * @return 当前AutoReconnectClient实例，支持链式调用
     */
    public AutoReconnectClient setOnConnectedCallback(Runnable callback) {
        this.onConnectedCallback = callback;
        return this;
    }
}