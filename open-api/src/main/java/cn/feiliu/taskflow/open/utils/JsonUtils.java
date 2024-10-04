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
package cn.feiliu.taskflow.open.utils;

import cn.feiliu.taskflow.serialization.JacksonSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-05-20
 */
//TODO 应该要被废弃了
@Deprecated
public class JsonUtils {
    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new ProtobufModule());
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        //过滤掉List属性为空的序列化属性
        mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        //--------------------------
        mapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, false);
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
    }

    /**
     * 将参数对象输出为 JSON 字符串
     */
    public static String serialize(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取 JSON 字符串返回对应的 Java 对象
     */
    public static <T> T deserialize(String json, Class<T> clazz) throws IOException {
        return mapper.readValue(json, clazz);
    }

    /**
     * 读取 JSON 字符串返回对应的 Java 对象列表
     *
     * @param json  JSON 数组字符串
     * @param clazz 数组中单个元素的类型
     */
    public static <T> List<T> fromJsonArray(String json, Class<T> clazz) throws IOException {
        return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }

    /**
     * 将参数对象输出为 JSON 字符串并写入到 writer
     *
     * @throws IOException
     */
    public static void writeValue(Writer writer, Object object) throws IOException {
        mapper.writeValue(writer, object);
    }

    public static <T> T deserialize(String json, Type type) {
        try {
            return mapper.readerFor(new TypeReference<Object>() {
                @Override
                public Type getType() {
                    return type;
                }
            }).readValue(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        JacksonSerializer jacksonSerializer = new JacksonSerializer();
        System.out.println(jacksonSerializer.writeAsString(new MyUser()));
    }

    public static class MyUser {
        Date date = new Date();
    }
}
