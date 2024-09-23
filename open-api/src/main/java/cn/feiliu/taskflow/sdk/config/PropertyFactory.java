/*
 * Copyright 2024 taskflow, Inc.
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
package cn.feiliu.taskflow.sdk.config;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Used to configure the Taskflow workers using properties.
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-09-01
 */
public class PropertyFactory {
    private static volatile boolean                                isSpringEnv          = false;
    private static final String                                    PROPERTY_PREFIX      = "taskflow.worker";
    private static final ConcurrentHashMap<String, PropertyReader> PROPERTY_FACTORY_MAP = new ConcurrentHashMap<>();

    public static void enabledSpringEnv() {
        PropertyFactory.isSpringEnv = true;
    }

    private static PropertyReader getPropertyReader(String workerName, String propName) {
        String key = propName + "." + workerName;
        return PROPERTY_FACTORY_MAP.computeIfAbsent(key, t -> {
            if (isSpringEnv) {
                return new SpringPropertyReader(PROPERTY_PREFIX, workerName, propName);
            } else {
                return new ApplicationPropertyReader(PROPERTY_PREFIX, workerName, propName);
            }
        });
    }

    public static Integer getInteger(String workerName, String property, Integer defaultValue) {
        return getPropertyReader(workerName, property).getInteger(defaultValue);
    }

    public static Boolean getBoolean(String workerName, String property, Boolean defaultValue) {
        return getPropertyReader(workerName, property).getBoolean(defaultValue);
    }

    public static String getString(String workerName, String property, String defaultValue) {
        return getPropertyReader(workerName, property).getString(defaultValue);
    }

    /**
     * 获取指定属性的布尔值，支持回退值。
     * 首先尝试从指定的 workerName 中获取属性值，如果未找到，则使用回退名称进行尝试。
     * 如果仍然未找到，则返回默认值。
     *
     * @param workerName    指定的 worker 名称
     * @param property      要获取的属性名称
     * @param fallbackName  回退使用的名称
     * @param defaultValue  默认值，如果所有尝试都失败则返回此值
     * @return 属性对应的布尔值
     */
    public static boolean getBooleanWithFallback(String workerName, String property, String fallbackName,
                                                 boolean defaultValue) {
        Boolean discoveryOverride = PropertyFactory.getBoolean(workerName, property, null);
        if (discoveryOverride == null) {
            return PropertyFactory.getBoolean(fallbackName, property, defaultValue);
        }
        return discoveryOverride;
    }

    public static String getStringWithFallback(String workerName, String property, String fallbackName,
                                               String defaultValue) {

        String domain = PropertyFactory.getString(workerName, property, null);
        if (domain == null) {
            return PropertyFactory.getString(fallbackName, property, defaultValue);
        }
        return domain;
    }

}
