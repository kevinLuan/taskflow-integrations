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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ProtoValueMapper {
    public static Value toProto(Object val) {
        Value.Builder builder = Value.newBuilder();
        if (val == null) {
            builder.setNullValue(NullValue.NULL_VALUE);
        } else if (val instanceof Boolean) {
            builder.setBoolValue((Boolean) val);
        } else if (val instanceof Double) {
            builder.setNumberValue((Double) val);
        } else if (val instanceof Integer) {
            builder.setNumberValue((Integer) val);
        } else if (val instanceof String) {
            builder.setStringValue((String) val);
        } else if (val instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) val;
            Struct.Builder struct = Struct.newBuilder();
            for (Map.Entry<String, Object> pair : map.entrySet()) {
                struct.putFields(pair.getKey(), toProto(pair.getValue()));
            }
            builder.setStructValue(struct.build());
        } else if (val instanceof List) {
            ListValue.Builder list = ListValue.newBuilder();
            for (Object obj : (List<Object>) val) {
                list.addValues(toProto(obj));
            }
            builder.setListValue(list.build());
        } else {
            throw new ClassCastException("cannot map to Value type: " + val);
        }
        return builder.build();
    }

    /**
     * Convert a ProtoBuf {@link Value} message into its native Java object equivalent.
     *
     * <p>See {@link ProtoValueMapper#toProto(Object)} for the reverse mapping and the possible values
     * that can be returned from this method.
     *
     * @param any an instance of a ProtoBuf {@link Value} message
     * @return a native Java object representing the value
     */
    public static Object fromProto(Value any) {
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
