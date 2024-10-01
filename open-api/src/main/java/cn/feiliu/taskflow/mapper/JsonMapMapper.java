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
package cn.feiliu.taskflow.mapper;

import cn.feiliu.taskflow.proto.JsonMapPb;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.google.protobuf.NullValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class JsonMapMapper extends BaseMapper {

    public static final JsonMapMapper INSTANCE = new JsonMapMapper();

    public static JsonMapMapper getInstance() {
        return new JsonMapMapper();
    }

    public Map<String, Object> convertToJavaMap(JsonMapPb.JsonMap inputDataMap) {
        Map<String, Object> data = new HashMap<>();
        inputDataMap.getFieldsMap().forEach((k, jv) -> data.put(k, fromProtoJsonValue(jv)));
        return data;
    }

    public JsonMapPb.JsonMap convertToJsonMap(Map<String, Object> map) {
        if (map == null) {
            return JsonMapPb.JsonMap.newBuilder().build();
        }
        JsonMapPb.JsonMap.Builder jsonMap = JsonMapPb.JsonMap.newBuilder();
        map.forEach((k, v) -> jsonMap.putFields(k, toProtoJsonValue(v)));
        return jsonMap.build();
    }

    protected Object fromProtoJsonValue(JsonMapPb.JsonValue any) {
        switch (any.getKindCase()) {
            case NULL_VALUE:
                return null;
            case INT_VALUE:
                return any.getIntValue();
            case LONG_VALUE:
                return any.getLongValue();
            case DOUBLE_VALUE:
                return any.getDoubleValue();
            case STRING_VALUE:
                return any.getStringValue();
            case BOOL_VALUE:
                return any.getBoolValue();
            case STRUCT_VALUE:
                return convertToJavaMap(any.getStructValue());
            case LIST_VALUE:
                List<Object> list = new ArrayList<>();
                for (JsonMapPb.JsonValue val : any.getListValue().getValuesList()) {
                    list.add(fromProtoJsonValue(val));
                }
                return list;
            default:
                throw new ClassCastException("unset Value element: " + any);
        }
    }

    protected JsonMapPb.JsonValue toProtoJsonValue(Object val) {
        JsonMapPb.JsonValue.Builder builder = JsonMapPb.JsonValue.newBuilder();
        setProtoJsonValue(builder, val);
        return builder.build();
    }

    private void setProtoJsonValue(JsonMapPb.JsonValue.Builder builder, Object val) {
        if (val == null) {
            builder.setNullValue(NullValue.NULL_VALUE);
        } else if (val instanceof Boolean) {
            builder.setBoolValue((Boolean) val);
        } else if (val instanceof Double) {
            builder.setDoubleValue((Double) val);
        } else if (val instanceof Float) {
            builder.setDoubleValue((Float) val);
        } else if (val instanceof Integer) {
            builder.setIntValue((Integer) val);
        } else if (val instanceof Long) {
            builder.setLongValue((Long) val);
        } else if (val instanceof Short) {
            builder.setIntValue((Short) val);
        } else if (val instanceof String) {
            builder.setStringValue((String) val);
        } else if (val instanceof Map) {
            //noinspection unchecked
            builder.setStructValue(convertToJsonMap((Map<String, Object>) val));
        } else if (val instanceof List) {
            JsonMapPb.JsonList.Builder list = JsonMapPb.JsonList.newBuilder();
            //noinspection unchecked
            for (Object obj : (List<Object>) val) {
                list.addValues(toProtoJsonValue(obj));
            }
            builder.setListValue(list.build());
        } else if (val instanceof JsonNode) {
            setProtoJsonValue(builder, getValueFromJsonNode((JsonNode) val));
        } else {
            Map<String, Object> map = strongMap(val);
            builder.setStructValue(convertToJsonMap(map));
        }
    }

    private Object getValueFromJsonNode(JsonNode val) {
        if (val instanceof IntNode) {
            return val.asInt();
        } else if (val instanceof LongNode) {
            return val.asLong();
        } else if (val instanceof TextNode) {
            return val.asText();
        } else if (val instanceof BooleanNode) {
            return val.asBoolean();
        } else if (val instanceof FloatNode) {
            return val.asDouble();
        } else if (val instanceof DoubleNode) {
            return val.asDouble();
        } else if (val instanceof ObjectNode) {
            return strongMap(val);
        } else if (val instanceof ArrayNode) {
            return strongList(val);
        } else if (val instanceof NullNode) {
            return null;
        }

        throw new RuntimeException(String.format("Cannot convert JsonNode of type %s to mapper", val.getClass()));
    }
}
