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
package cn.feiliu.taskflow.client.http.types;

import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.open.ApiResponse;
import cn.feiliu.taskflow.open.exceptions.ApiException;
import cn.feiliu.taskflow.client.utils.HttpHelper;
import com.google.inject.util.Types;
import com.squareup.okhttp.Response;

import java.io.IOException;
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
        if (elementType == ApiResponse.class) {
            throw new IllegalArgumentException("Invalid elementType");
        }
    }

    /**
     * 获取包装类型
     *
     * @return
     */
    public final Type getType() {
        return Types.newParameterizedType(ApiResponse.class, getElementType());
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

    public <T> ApiResponse<T> handleResponse(ApiClient client, Response response) {
        if (response.isSuccessful()) {
            if (isVoid() && response.code() == 204) {
                // 如果未定义returnType，则返回null，或者状态码为204(无内容)
                if (response.body() != null) {
                    try {
                        response.body().close();
                    } catch (IOException e) {
                        throw new ApiException(response.message(), e, response.code(), response.headers().toMultimap());
                    }
                }
                return ApiResponse.ok(null);
            } else {
                ApiResponse<T> apiResponse = HttpHelper.deserialize(client, response, this);
                if (apiResponse.isSuccessful()) {
                    return apiResponse;
                } else {
                    throw apiResponse.makeException();
                }
            }
        } else {
            throw new ApiException(response.message(), response.code(), response.headers().toMultimap(), null);
        }
    }
}
