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
package cn.feiliu.taskflow.http.types;

import cn.feiliu.common.api.model.resp.DataResult;
import com.google.inject.util.Types;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-06-13
 */
public class TypeFactory {

    public static <T> ResponseTypeHandler<DataResult<T>> empty() {
        return new ResponseTypeHandler<>(null);
    }

    public static <T> ResponseTypeHandler<DataResult<T>> of(Type elementType) {
        return new ResponseTypeHandler<>(elementType);
    }

    public static <T> ResponseTypeHandler<DataResult<List<T>>> ofList(Class<T> elementType) {
        return new ResponseTypeHandler<>(Types.listOf(elementType));
    }

    public static <T> ResponseTypeHandler<DataResult<Map<String, T>>> ofMap(Class<T> elementType) {
        return new ResponseTypeHandler<>(Types.mapOf(String.class, elementType));
    }
}
