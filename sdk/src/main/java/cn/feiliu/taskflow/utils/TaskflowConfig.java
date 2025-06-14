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
package cn.feiliu.taskflow.utils;

import lombok.Data;

/**
 * @author kevin.luan
 * @since 2025-06-04
 */
@Data
public class TaskflowConfig {
    /*飞流云平台 host */
    private String  baseUrl        = "https://developer.taskflow.cn/api";
    /*开发者key*/
    private String  keyId;
    /*开发者秘钥*/
    private String  keySecret;
    /*自动注册*/
    private Boolean autoRegister   = true;
    /*存在则更新*/
    private Boolean updateExisting = true;
    /*web socket url*/
    private String  webSocketUrl   = "wss://developer.taskflow.cn";

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
