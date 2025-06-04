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

import cn.feiliu.common.api.exception.NotFoundException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * 基础属性文件读取器，适用于标准 Java 项目
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-09-30
 */
public class PropertiesReader {
    protected final Properties properties;
    protected final String     filename;

    /**
     * 构造函数，加载类路径中的属性文件
     *
     * @param filename 类路径中的属性文件名
     * @throws IOException 如果文件读取失败
     */
    public PropertiesReader(String filename) throws IOException {
        this.filename = Objects.requireNonNull(filename, "文件名不能为空");
        this.properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (input == null) {
                throw new NotFoundException("无法找到属性文件: " + filename);
            }
            properties.load(input);
        }
    }

    /**
     * 获取属性值
     *
     * @param key 属性键
     * @return 属性值
     * @throws NotFoundException 如果属性未找到
     */
    public String getProperty(String key) {
        Objects.requireNonNull(key, "属性键不能为空");
        String value = properties.getProperty(key);
        if (value == null) {
            throw new NotFoundException(String.format("未找到配置键: '%s' 在 %s 中", key, filename));
        }
        return value;
    }

    /**
     * 获取属性值，带默认值
     *
     * @param key          属性键
     * @param defaultValue 属性未找到时的默认值
     * @return 属性值或默认值
     */
    public String getProperty(String key, String defaultValue) {
        Objects.requireNonNull(key, "属性键不能为空");
        return properties.getProperty(key, defaultValue);
    }

    /**
     * 获取布尔型属性值
     *
     * @param key 属性键
     * @return 布尔值，若未找到或无效则返回 null
     */
    public Boolean getBoolean(String key) {
        return getParsedValue(key, value -> {
            if (StringUtils.isNotBlank(value)) {
                return Boolean.parseBoolean(value);
            }
            return null;
        });
    }

    /**
     * 获取长整型属性值
     *
     * @param key 属性键
     * @return 长整型值，若未找到或无效则返回 null
     */
    public Long getLong(String key) {
        return getParsedValue(key, value -> {
            if (StringUtils.isNotBlank(value)) {
                try {
                    return Long.parseLong(value);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });
    }

    /**
     * 获取整型属性值
     *
     * @param key 属性键
     * @return 整型值，若未找到或无效则返回 null
     */
    public Integer getInt(String key) {
        return getParsedValue(key, value -> {
            if (StringUtils.isNotBlank(value)) {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });
    }

    /**
     * 通用的属性值解析方法，包含错误处理
     *
     * @param key    属性键
     * @param parser 解析字符串值的函数
     * @param <T>    解析值的类型
     * @return 解析后的值，若未找到或无效则返回 null
     */
    protected <T> T getParsedValue(String key, Parser<T> parser) {
        try {
            String value = getProperty(key);
            return parser.parse(value);
        } catch (NotFoundException e) {
            return null;
        }
    }

    /**
     * 用于解析属性值的函数式接口
     *
     * @param <T> 解析值的类型
     */
    @FunctionalInterface
    protected interface Parser<T> {
        T parse(String value);
    }

    /**
     * 检查属性是否存在
     *
     * @param key 属性键
     * @return 如果属性存在则返回 true
     */
    public boolean containsProperty(String key) {
        Objects.requireNonNull(key, "属性键不能为空");
        return properties.containsKey(key);
    }

    /**
     * 获取所有属性
     *
     * @return 包含所有加载属性的 Properties 对象
     */
    public Properties getAllProperties() {
        return new Properties(properties);
    }

    public TaskflowConfig toConfig() {
        TaskflowConfig config = new TaskflowConfig();
        config.setBaseUrl(getProperty("taskflow.base-url"));
        config.setClientKey(getProperty("taskflow.client-key"));
        config.setClientSecret(getProperty("taskflow.client-secret"));
        config.setAutoRegister(getBoolean("taskflow.auto-register"));
        config.setUpdateExisting(getBoolean("taskflow.update-existing"));
        return config;
    }
}