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

import cn.feiliu.taskflow.utils.PropertiesReader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * Spring 项目的属性文件读取器，扩展基础读取器以支持 Spring Environment
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-09-30
 */
public class SpringPropertiesReader extends PropertiesReader {
    private final Environment springEnv;

    /**
     * 构造函数，支持 Spring Environment 和属性文件
     *
     * @param filename   类路径中的属性文件名
     * @param springEnv  Spring Environment
     * @throws IOException 如果文件读取失败
     */
    public SpringPropertiesReader(String filename, Environment springEnv) throws IOException {
        super(filename);
        this.springEnv = Objects.requireNonNull(springEnv, "Spring Environment 不能为空");
        // 加载属性文件到 Properties 对象
        Resource resource = new ClassPathResource(filename);
        if (resource.exists()) {
            try {
                PropertiesLoaderUtils.fillProperties(properties, resource);
            } catch (IOException e) {
                throw new IOException("无法加载属性文件 " + filename, e);
            }
        }
    }

    /**
     * 获取属性值，优先级：Spring Environment > 属性文件
     *
     * @param key 属性键
     * @return 属性值
     */
    @Override
    public String getProperty(String key) {
        Objects.requireNonNull(key, "属性键不能为空");
        // 优先检查 Spring Environment
        String value = springEnv.getProperty(key);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        // 回退到属性文件
        return super.getProperty(key);
    }

    /**
     * 获取属性值，带默认值
     *
     * @param key          属性键
     * @param defaultValue 属性未找到时的默认值
     * @return 属性值或默认值
     */
    @Override
    public String getProperty(String key, String defaultValue) {
        Objects.requireNonNull(key, "属性键不能为空");
        return springEnv.getProperty(key, super.getProperty(key, defaultValue));
    }

    /**
     * 检查属性是否存在
     *
     * @param key 属性键
     * @return 如果属性存在则返回 true
     */
    @Override
    public boolean containsProperty(String key) {
        Objects.requireNonNull(key, "属性键不能为空");
        return springEnv.containsProperty(key) || super.containsProperty(key);
    }
}