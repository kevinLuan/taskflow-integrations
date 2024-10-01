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

import com.google.protobuf.ListValue;
import com.google.protobuf.NullValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import lombok.Data;

import java.util.*;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-06-29
 */
@Data(staticConstructor = "of")
class ValueMapper extends BaseMapper {
    private MapperFactory mapperFactory;

    public static ValueMapper getInstance() {
        return new ValueMapper();
    }

    public Value toProto(Object val) {
        Value.Builder builder = Value.newBuilder();
        if (val == null) {
            builder.setNullValue(NullValue.NULL_VALUE);
        } else if (val instanceof Boolean) {
            builder.setBoolValue((Boolean) val);
        } else if (val instanceof Float) {
            builder.setNumberValue((Float) val);
        } else if (val instanceof Double) {
            builder.setNumberValue((Double) val);
        } else if (val instanceof Long) {
            builder.setNumberValue((Long) val);
        } else if (val instanceof Integer) {
            builder.setNumberValue((Integer) val);
        } else if (val instanceof String) {
            builder.setStringValue((String) val);
        } else if (val instanceof Map) {
            builder.setStructValue(convertStruct((Map<String, Object>) val));
        } else if (val instanceof List) {
            builder.setListValue(convertList((List<?>) val));
        } else if (val.getClass().isArray()) {
            builder.setListValue(convertList((Object[]) val));
        } else {
            return toProto(strongMap(val));
        }
        return builder.build();
    }

    private Struct convertStruct(Map<String, Object> map) {
        Struct.Builder struct = Struct.newBuilder();
        for (Map.Entry<String, Object> pair : map.entrySet()) {
            struct.putFields(pair.getKey(), toProto(pair.getValue()));
        }
        return struct.build();
    }

    private ListValue convertList(List<?> list) {
        ListValue.Builder builder = ListValue.newBuilder();
        for (Object object : list) {
            builder.addValues(toProto(object));
        }
        return builder.build();
    }

    private ListValue convertList(Object[] array) {
        ListValue.Builder list = ListValue.newBuilder();
        for (Object object : array) {
            list.addValues(toProto(object));
        }
        return list.build();
    }

    public Object fromProto(Value any) {
        switch (any.getKindCase()) {
            case NULL_VALUE:
                return null;
            case BOOL_VALUE:
                return any.getBoolValue();
            case NUMBER_VALUE:
                return any.getNumberValue();
            case STRING_VALUE:
                return any.getStringValue();
            case STRUCT_VALUE:
                Struct struct = any.getStructValue();
                Map<String, Object> map = new HashMap<>();
                for (Map.Entry<String, Value> pair : struct.getFieldsMap().entrySet()) {
                    map.put(pair.getKey(), fromProto(pair.getValue()));
                }
                return map;
            case LIST_VALUE:
                List<Object> list = new ArrayList<>();
                for (Value val : any.getListValue().getValuesList()) {
                    list.add(fromProto(val));
                }
                return list;
            default:
                throw new ClassCastException("unset Value element: " + any);
        }
    }
}
