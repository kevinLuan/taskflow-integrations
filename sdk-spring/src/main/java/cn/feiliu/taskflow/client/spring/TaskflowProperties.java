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
package cn.feiliu.taskflow.client.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TaskFlow配置属性类
 * 
 * @author kevin.luan
 * @since 2025-01-08
 */
@ConfigurationProperties(prefix = "taskflow")
public class TaskflowProperties {

    /**
     * 是否启用TaskFlow功能
     */
    private boolean enabled        = true;

    /**
     * 飞流云平台 host
     */
    private String  baseUrl        = "https://developer.taskflow.cn/api";

    /**
     * 开发者key
     * 注意：此配置项为必需项，请在配置文件中设置taskflow.key-id
     */
    private String  keyId;

    /**
     * 开发者秘钥
     * 注意：此配置项为必需项，请在配置文件中设置taskflow.key-secret
     */
    private String  keySecret;

    /**
     * 自动注册
     */
    private Boolean autoRegister   = true;

    /**
     * 存在则更新
     */
    private Boolean updateExisting = true;

    /**
     * web socket url
     */
    private String  webSocketUrl   = "wss://developer.taskflow.cn";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getKeySecret() {
        return keySecret;
    }

    public void setKeySecret(String keySecret) {
        this.keySecret = keySecret;
    }

    public Boolean getAutoRegister() {
        return autoRegister;
    }

    public void setAutoRegister(Boolean autoRegister) {
        this.autoRegister = autoRegister;
    }

    public Boolean getUpdateExisting() {
        return updateExisting;
    }

    public void setUpdateExisting(Boolean updateExisting) {
        this.updateExisting = updateExisting;
    }

    public String getWebSocketUrl() {
        return webSocketUrl;
    }

    public void setWebSocketUrl(String webSocketUrl) {
        this.webSocketUrl = webSocketUrl;
    }

    public boolean isSupportWebsocket() {
        return webSocketUrl != null;
    }

    public boolean isAutoRegister() {
        return autoRegister != null && autoRegister;
    }

    public boolean isUpdateExisting() {
        return updateExisting != null && updateExisting;
    }
}