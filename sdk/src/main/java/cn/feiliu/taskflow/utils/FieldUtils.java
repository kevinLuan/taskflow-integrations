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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 工具类，用于获取 POJO 类型的字段名称
 */
public class FieldUtils {
    /**
     * 获取当前 POJO 类型所有的字段名称，仅返回具有公共 Getter 方法的字段
     *
     * @param type 要检查的 POJO 类
     * @return 字段名称数组，如果没有符合条件的字段，返回空数组
     */
    public static String[] getJavaFieldNames(Class<?> type) {
        if (type == null) {
            return new String[0];
        }
        List<String> fieldNames = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            // 检查是否存在对应的公共 Getter 方法
            if (hasPublicGetter(type, field)) {
                fieldNames.add(field.getName());
            }
        }
        return fieldNames.toArray(new String[0]);
    }

    /**
     * 检查字段是否具有公共 Getter 方法
     *
     * @param type  类
     * @param field 字段
     * @return 如果存在公共 Getter 方法，返回 true；否则返回 false
     */
    private static boolean hasPublicGetter(Class<?> type, Field field) {
        String fieldName = field.getName();
        String capitalized = capitalize(fieldName);
        // 可能的 Getter 方法名前缀
        String[] prefixes = field.getType() == boolean.class || field.getType() == Boolean.class ? new String[] { "is",
                "get" } : new String[] { "get" };

        for (String prefix : prefixes) {
            try {
                String getterName = prefix + capitalized;
                Method getter = type.getMethod(getterName);
                // 检查方法是否为公共的且返回类型匹配
                if (Modifier.isPublic(getter.getModifiers()) && isCompatibleReturnType(getter, field)) {
                    return true;
                }
            } catch (NoSuchMethodException ignored) {
                // 继续尝试其他前缀
            }
        }
        return false;
    }

    /**
     * 检查 Getter 方法的返回类型是否与字段类型兼容
     *
     * @param getter Getter 方法
     * @param field  字段
     * @return 如果返回类型兼容，返回 true；否则返回 false
     */
    private static boolean isCompatibleReturnType(Method getter, Field field) {
        Class<?> fieldType = field.getType();
        Class<?> returnType = getter.getReturnType();
        // 基本类型和其包装类的兼容性检查
        if (fieldType.isPrimitive()) {
            if (fieldType == boolean.class && returnType == Boolean.class)
                return true;
            if (fieldType == int.class && returnType == Integer.class)
                return true;
            if (fieldType == double.class && returnType == Double.class)
                return true;
            if (fieldType == float.class && returnType == Float.class)
                return true;
            if (fieldType == long.class && returnType == Long.class)
                return true;
            if (fieldType == short.class && returnType == Short.class)
                return true;
            if (fieldType == byte.class && returnType == Byte.class)
                return true;
            if (fieldType == char.class && returnType == Character.class)
                return true;
            if (fieldType == void.class && returnType == Void.class)
                return true;
        }
        return fieldType.isAssignableFrom(returnType);
    }

    /**
     * 将字段名首字母大写，用于构造 Getter 方法名
     *
     * @param fieldName 字段名称
     * @return 首字母大写的字段名称
     */
    private static String capitalize(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return fieldName;
        }
        return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }
}