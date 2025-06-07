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

import cn.feiliu.common.api.encoder.EncoderFactory;
import cn.feiliu.common.api.encoder.JsonEncoder;
import cn.feiliu.common.api.utils.AuthTokenUtil;
import cn.feiliu.taskflow.common.utils.StringUtils;
import cn.feiliu.taskflow.ws.msg.WebSocketMessage;
import okhttp3.*;
import okio.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Taskflow WebSocket 客户端 SDK
 * 基于OkHttp实现的WebSocket客户端
 *
 * @author taskflow
 */
public class WebSocketClient {

    private static final Logger           logger            = LoggerFactory.getLogger(WebSocketClient.class);

    private final OkHttpClient            client;
    private final String                  serverUrl;
    private final String                  userId;
    private final String                  keyId;
    private final String                  keySecret;
    private final String                  authToken;
    private final WebSocketMessageHandler messageHandler;

    private WebSocket                     webSocket;
    private boolean                       connected         = false;
    private boolean                       disconnectHandled = false;                                         // 防止重复处理断开事件
    private JsonEncoder                   jsonEncoder       = EncoderFactory.getJsonEncoder();

    /**
     * keyId/keySecret认证构造函数
     */
    public WebSocketClient(String serverUrl, String userId, String keyId, String keySecret,
                           WebSocketMessageHandler messageHandler) {
        this(serverUrl, userId, keyId, keySecret, null, messageHandler);
    }

    /**
     * 完整构造函数（支持多种认证方式）
     */
    public WebSocketClient(String serverUrl, String userId, String keyId, String keySecret, String authToken,
                           WebSocketMessageHandler messageHandler) {
        this.serverUrl = serverUrl;
        this.userId = userId;
        this.keyId = keyId;
        this.keySecret = keySecret;
        this.authToken = authToken;
        this.messageHandler = messageHandler;

        // 配置OkHttp客户端
        this.client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS).pingInterval(30, TimeUnit.SECONDS) // 心跳间隔
            .build();
    }

    /**
     * 连接到WebSocket服务器
     */
    public CompletableFuture<Boolean> connect() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // 重置断开处理标志
        disconnectHandled = false;

        try {
            // 构建WebSocket URL，包含用户ID等参数
            String wsUrl = buildWebSocketUrl();
            Request.Builder requestBuilder = new Request.Builder().url(wsUrl);

            // 添加认证信息
            addAuthenticationHeaders(requestBuilder);

            Request request = requestBuilder.build();

            WebSocketListener listener = new WebSocketListener() {
                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    logger.info("WebSocket连接建立成功");
                    WebSocketClient.this.webSocket = webSocket;
                    connected = true;
                    disconnectHandled = false; // 连接成功时重置标志
                    future.complete(true);
                    if (messageHandler != null) {
                        messageHandler.onConnected();
                    }
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    logger.debug("收到WebSocket文本消息: {}", text);
                    try {
                        WebSocketMessage message = jsonEncoder.decode(text, WebSocketMessage.class);
                        if (messageHandler != null) {
                            messageHandler.onMessage(message);
                        }
                    } catch (Exception e) {
                        logger.error("解析WebSocket消息失败: {}", text, e);
                    }
                }

                @Override
                public void onMessage(WebSocket webSocket, ByteString bytes) {
                    logger.debug("收到WebSocket二进制消息");
                    // 如果需要处理二进制消息，可以在这里实现
                }

                @Override
                public void onClosing(WebSocket webSocket, int code, String reason) {
                    logger.info("WebSocket连接正在关闭: code={}, reason={}", code, reason);
                    connected = false;
                    if (messageHandler != null) {
                        messageHandler.onDisconnecting(code, reason);
                        // 在 onClosing 中触发 onDisconnected，确保重连逻辑被执行
                        handleDisconnection(code, reason);
                    }
                }

                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    logger.info("WebSocket连接已关闭: code={}, reason={}", code, reason);
                    connected = false;
                    // 确保断开事件被处理（如果在 onClosing 中未处理）
                    if (messageHandler != null) {
                        handleDisconnection(code, reason);
                    }
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    logger.error("WebSocket连接失败", t);
                    connected = false;
                    if (!future.isDone()) {
                        future.complete(false);
                    }
                    if (messageHandler != null) {
                        messageHandler.onError(t);
                    }
                }
            };

            webSocket = client.newWebSocket(request, listener);

        } catch (Exception e) {
            logger.error("创建WebSocket连接失败", e);
            future.complete(false);
        }

        return future;
    }

    /**
     * 发送消息到服务器
     */
    public boolean sendMessage(String type, String message, Map<String, Object> data) {
        if (!connected || webSocket == null) {
            logger.warn("WebSocket未连接，无法发送消息");
            return false;
        }
        try {
            WebSocketMessage wsMessage = new WebSocketMessage(type, message, data);
            String json = jsonEncoder.encode(wsMessage);
            return webSocket.send(json);
        } catch (Exception e) {
            logger.error("发送WebSocket消息失败", e);
            return false;
        }
    }

    /**
     * 发送心跳消息
     */
    public boolean sendPing() {
        return sendMessage("ping", "心跳检测", null);
    }

    /**
     * 订阅特定类型的消息
     */
    public boolean subTask(List<String> tasks) {
        Map<String, Object> data = new HashMap<>();
        data.put("tasks", tasks);
        return sendMessage("subscribe", "订阅消息", data);
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "客户端主动断开");
            webSocket = null;
        }
        connected = false;
    }

    /**
     * 检查连接状态
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * 关闭客户端
     */
    public void close() {
        disconnect();
        client.dispatcher().executorService().shutdown();
    }

    private String buildWebSocketUrl() {
        StringBuilder urlBuilder = new StringBuilder(serverUrl);
        if (!serverUrl.endsWith("/")) {
            urlBuilder.append("/");
        }
        urlBuilder.append("ws/taskflow");

        // 添加查询参数
        urlBuilder.append("?userId=").append(userId);
        urlBuilder.append("&clientType=java-sdk");
        urlBuilder.append("&version=1.0.0");

        return urlBuilder.toString();
    }

    private void addAuthenticationHeaders(Request.Builder requestBuilder) {
        // 优先使用keyId/keySecret认证（与PartnerApp机制一致）
        if (StringUtils.isNotBlank(keyId) && StringUtils.isNotBlank(keySecret)) {
            String token = AuthTokenUtil.constructCredentials(keyId, keySecret);
            requestBuilder.addHeader("Authorization", token);
            logger.debug("添加Authorization头: {}...", keyId + ":" + keySecret);
        } else if (authToken != null && !authToken.trim().isEmpty()) {// 备用：Bearer Token认证
            String authHeader = authToken.startsWith("Bearer ") ? authToken : "Bearer " + authToken;
            requestBuilder.addHeader("Authorization", authHeader);
            logger.debug("添加Authorization头: {}", authHeader.substring(0, Math.min(20, authHeader.length())) + "...");
        } else {
            throw new IllegalArgumentException("请配置认证参数");
        }
    }

    /**
     * 创建Builder实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * TaskflowWebSocketClient Builder类
     */
    public static class Builder {
        private String                  serverUrl;
        private String                  userId;
        private String                  keyId;
        private String                  keySecret;
        private String                  authToken;
        private WebSocketMessageHandler messageHandler;

        private Builder() {
        }

        /**
         * 设置服务器地址
         */
        public Builder serverUrl(String serverUrl) {
            this.serverUrl = serverUrl;
            return this;
        }

        /**
         * 设置用户ID
         */
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        /**
         * 设置keyId
         */
        public Builder keyId(String keyId) {
            this.keyId = keyId;
            return this;
        }

        /**
         * 设置keySecret
         */
        public Builder keySecret(String keySecret) {
            this.keySecret = keySecret;
            return this;
        }

        /**
         * 设置JWT认证Token
         */
        public Builder authToken(String authToken) {
            this.authToken = authToken;
            return this;
        }

        /**
         * 设置消息处理器
         */
        public Builder messageHandler(WebSocketMessageHandler messageHandler) {
            this.messageHandler = messageHandler;
            return this;
        }

        /**
         * 构建TaskflowWebSocketClient实例
         */
        public WebSocketClient build() {
            // 验证必填字段
            Objects.requireNonNull(serverUrl, "serverUrl不能为空");
            Objects.requireNonNull(userId, "userId不能为空");
            Objects.requireNonNull(messageHandler, "messageHandler不能为空");

            // 验证URL格式
            if (!serverUrl.startsWith("ws://") && !serverUrl.startsWith("wss://")) {
                throw new IllegalArgumentException("serverUrl必须以ws://或wss://开头");
            }

            return new WebSocketClient(serverUrl, userId, keyId, keySecret, authToken, messageHandler);
        }
    }

    /**
     * 为多实例场景生成唯一的userId
     * 格式: keyId_instanceId
     *
     * @param keyId 应用的keyId
     * @return 唯一的userId
     */
    public static String generateUniqueUserId(String keyId) {
        try {
            // 方式1: 使用主机名 + 进程ID
            String hostName = InetAddress.getLocalHost().getHostName();
            String processId = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
            return keyId + "_" + hostName + "_" + processId;
        } catch (Exception e) {
            // 方式2: 备用方案，使用随机UUID
            String instanceId = java.util.UUID.randomUUID().toString().substring(0, 8);
            return keyId + "_" + instanceId;
        }
    }

    /**
     * 从keyId中提取应用标识（去掉实例标识）
     *
     * @param userId 完整的userId
     * @return 应用的keyId
     */
    public static String extractKeyIdFromUserId(String userId) {
        if (userId == null) {
            return null;
        }
        // 提取第一个下划线之前的部分
        int index = userId.indexOf('_');
        return index > 0 ? userId.substring(0, index) : userId;
    }

    /**
     * 处理断开连接事件，避免重复触发
     */
    private void handleDisconnection(int code, String reason) {
        if (!disconnectHandled) {
            disconnectHandled = true;
            messageHandler.onDisconnected(code, reason);
        }
    }
}