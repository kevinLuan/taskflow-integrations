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
import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.common.exceptions.ApiException;
import cn.feiliu.taskflow.utils.ClientHelper;
import com.google.inject.util.Types;
import okhttp3.Response;

import java.lang.reflect.Type;
import java.util.Optional;

/**
 * 定义接口响应类型
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-06-13
 */
public class ResponseTypeHandler<T> {
    private final Type elementType;

    public ResponseTypeHandler(Type elementType) {
        this.elementType = Optional.ofNullable(elementType).orElseGet(() -> void.class);
        if (elementType == DataResult.class) {
            throw new IllegalArgumentException("Invalid elementType");
        }
    }

    /**
     * 获取包装类型
     *
     * @return
     */
    public final Type getType() {
        return Types.newParameterizedType(DataResult.class, getElementType());
    }

    /**
     * 获取数据类型
     *
     * @return
     */
    public final Type getElementType() {
        return elementType == void.class ? Void.class : elementType;
    }

    public final boolean isVoid() {
        return getElementType() == null || getElementType() == Void.class || getElementType() == void.class;
    }

    public <T> DataResult<T> handleResponse(ApiClient client, Response response) {
        if (response.isSuccessful()) {
            if (isVoid() && response.code() == 204) {
                // 如果未定义returnType，则返回null，或者状态码为204(无内容)
                if (response.body() != null) {
                    response.body().close();
                }
                return DataResult.ok(null);
            } else {
                DataResult<T> dataResult = ClientHelper.deserialize(client, response, this);
                if (dataResult.isSuccessful()) {
                    return dataResult;
                } else {
                    throw new ApiException(dataResult.getCode(), dataResult.getMsg());
                }
            }
        } else {
            throw new ApiException(response.message(), response.code(), response.headers().toMultimap(), null);
        }
    }
}
