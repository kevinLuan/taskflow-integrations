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
package cn.feiliu.taskflow.serialization;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * 序列化器
 * @author SHOUSHEN.LUAN
 * @since 2024-08-19
 */
public interface Serializer {
    <T> Map<String, T> convertMap(Object val, Type valueType);

    default Map<String, Object> convertMap(Object val) {
        return convertMap(val, Object.class);
    }

    <T> List<T> convertList(Object val, Type elementType);

    default List<Object> convertList(Object val) {
        return convertList(val, Object.class);
    }

    <T> T convert(Object val, Type targetType);

    String writeAsString(Object val);

    <T> List<T> readList(InputStream resource, Type elementType);

    <T> T read(InputStream resource, Class<T> targetType);

    Map<String, Object> readMap(InputStream resourceAsStream);
}
