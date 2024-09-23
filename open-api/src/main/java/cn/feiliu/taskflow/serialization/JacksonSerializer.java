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
package cn.feiliu.taskflow.serialization;

import cn.feiliu.taskflow.serialization.jackson.DateDeserializer;
import cn.feiliu.taskflow.serialization.jackson.DateSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.util.Types;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-08-19
 */
@Slf4j
public class JacksonSerializer implements Serializer {
    final static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        //        objectMapper.setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_NULL,
        //            JsonInclude.Include.NON_EMPTY));
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        objectMapper.registerModule(new JsonProtoModule());

        // 禁用默认的时间戳序列化
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        // 添加自定义模块
        SimpleModule module = new SimpleModule();
        module.addSerializer(Date.class, new DateSerializer());
        module.addDeserializer(Date.class, new DateDeserializer());
        objectMapper.registerModule(module);
    }

    @Override
    public <T> Map<String, T> convertMap(Object val, Type valueType) {
        try {
            return objectMapper.convertValue(val, new TypeReference<Map<String, T>>() {
                @Override
                public Type getType() {
                    return Types.mapOf(String.class, valueType);
                }
            });
        } catch (Exception e) {
            throw new ClassCastException("cannot map to Value type: " + val);
        }
    }

    @Override
    public Map<String, Object> convertMap(Object val) {
        return convertMap(val, Object.class);
    }

    @Override
    public <T> List<T> convertList(Object val, Type elementType) {
        try {

            return objectMapper.convertValue(val, new TypeReference<List<T>>() {
                @Override
                public Type getType() {
                    return Types.listOf(elementType);
                }
            });
        } catch (Exception e) {
            throw new ClassCastException("cannot list to Value type: " + val);
        }
    }

    @Override
    public <T> T convert(Object val, Type targetType) {
        try {
            return objectMapper.convertValue(val, new TypeReference<T>() {
                @Override
                public Type getType() {
                    return targetType;
                }
            });
        } catch (Exception e) {
            String msg = String.format("Error while trying from: '%s' convert  to '%s' error:'%s'", val,
                targetType.getTypeName(), e.getMessage());
            log.error(msg, e);
            throw new ClassCastException(msg);
        }
    }

    @Override
    public String writeAsString(Object val) {
        try {
            return objectMapper.writeValueAsString(val);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> List<T> readList(InputStream resource, Type elementType) {
        try {
            return objectMapper.readValue(resource, new TypeReference<List<T>>() {
                @Override
                public Type getType() {
                    return Types.listOf(elementType);
                }
            });
        } catch (Exception e) {
            throw new ClassCastException("cannot list type:'" + elementType.getTypeName() + "'");
        }
    }

    @Override
    public <T> T read(InputStream resource, Class<T> targetType) {
        try {
            return objectMapper.readValue(resource, targetType);
        } catch (Exception e) {
            throw new ClassCastException("Error reading data and converting type:'" + targetType.getName() + "'");
        }
    }

    @Override
    public Map<String, Object> readMap(InputStream resourceAsStream) {
        try {
            return objectMapper.readValue(resourceAsStream, Map.class);
        } catch (Exception e) {
            throw new ClassCastException("Error reading data and converting Map");
        }
    }

}
