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
package cn.feiliu.taskflow.open.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-06-28
 */
@Data
public class Application {
    /**
     * 开放平台应用ID
     */
    private Long    id;

    /**
     * 应用名称
     */
    private String  name;

    /**
     * 开放平台应用key
     */
    private String  appKey;

    /**
     * 租户ID
     */
    private Long    tid;

    /**
     * 应用类型:1自用型
     */
    private Integer type;

    /**
     * 状态：1有效
     */
    private Integer status;

    private Date    createTime;
}
