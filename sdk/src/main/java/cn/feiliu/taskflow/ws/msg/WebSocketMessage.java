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
package cn.feiliu.taskflow.ws.msg;

import cn.feiliu.common.api.encoder.EncoderFactory;
import cn.feiliu.taskflow.ws.MessageType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * WebSocket消息DTO (客户端版本)
 */
@Builder
@Data
public class WebSocketMessage {
    private String              type;
    private String              description;
    private Map<String, Object> data;

    public WebSocketMessage() {
    }

    public WebSocketMessage(String type, String message, Map<String, Object> data) {
        this.type = type;
        this.description = message;
        this.data = data;
    }

    public boolean isMessageType(MessageType messageType) {
        return messageType.getValue().equals(this.type);
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getData() {
        return data;
    }

    /**
     * 转换到目标类型
     *
     * @param type
     * @param <T>
     * @return
     */
    public <T> T getData(Class<T> type) {
        return EncoderFactory.getJsonEncoder().convert(data, type);
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
