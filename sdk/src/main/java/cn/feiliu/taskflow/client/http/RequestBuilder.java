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
package cn.feiliu.taskflow.client.http;

import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.client.utils.HttpHelper;
import cn.feiliu.taskflow.client.http.ProgressRequestBody.ProgressRequestListener;
import cn.feiliu.taskflow.exceptions.ApiException;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.internal.http.HttpMethod;

import java.util.*;

/**
 * HTTP请求构建器类
 * 用于构建HTTP请求的各个组成部分,包括查询参数、请求体、请求头等
 */
public class RequestBuilder {
    private final ApiClient         client;               // API客户端实例
    private final String            path;                 // 请求路径
    private final String            method;               // HTTP方法(GET/POST等)

    private List<Pair>              queryParams;          // URL查询参数列表
    private List<Pair>              collectionQueryParams; // 集合类型的查询参数列表
    private Object                  body;                 // 请求体
    private Map<String, String>     headerParams;         // 请求头参数
    private Map<String, Object>     formParams;           // 表单参数
    private ProgressRequestListener progressListener;     // 进度监听器

    /**
     * 私有构造函数
     * @param client API客户端实例
     * @param path 请求路径
     * @param method HTTP方法
     */
    private RequestBuilder(ApiClient client, String path, String method) {
        this.client = client;
        this.path = path;
        this.method = method;
        this.queryParams = new ArrayList<>();
        this.collectionQueryParams = new ArrayList<>();
        this.headerParams = new HashMap<>();
        this.formParams = new HashMap<>();
    }

    /**
     * 创建GET请求构建器
     * @param client API客户端实例
     * @param path 请求路径
     * @return RequestBuilder实例
     */
    public static RequestBuilder get(ApiClient client, String path) {
        return new RequestBuilder(client, path, "GET");
    }

    public static RequestBuilder of(ApiClient client, String path, String method) {
        return new RequestBuilder(client, path, method);
    }

    /**
     * 创建POST请求构建器
     * @param client API客户端实例
     * @param path 请求路径
     * @return RequestBuilder实例
     */
    public static RequestBuilder post(ApiClient client, String path) {
        return new RequestBuilder(client, path, "POST");
    }

    /**
     * 创建PUT请求构建器
     * @param client API客户端实例
     * @param path 请求路径
     * @return RequestBuilder实例
     */
    public static RequestBuilder put(ApiClient client, String path) {
        return new RequestBuilder(client, path, "PUT");
    }

    /**
     * 创建DELETE请求构建器
     * @param client API客户端实例
     * @param path 请求路径
     * @return RequestBuilder实例
     */
    public static RequestBuilder delete(ApiClient client, String path) {
        return new RequestBuilder(client, path, "DELETE");
    }

    /**
     * 创建PATCH请求构建器
     * @param client API客户端实例
     * @param path 请求路径
     * @return RequestBuilder实例
     */
    public static RequestBuilder patch(ApiClient client, String path) {
        return new RequestBuilder(client, path, "PATCH");
    }

    /**
     * 创建HEAD请求构建器
     * @param client API客户端实例
     * @param path 请求路径
     * @return RequestBuilder实例
     */
    public static RequestBuilder head(ApiClient client, String path) {
        return new RequestBuilder(client, path, "HEAD");
    }

    /**
     * 创建OPTIONS请求构建器
     * @param client API客户端实例
     * @param path 请求路径
     * @return RequestBuilder实例
     */
    public static RequestBuilder options(ApiClient client, String path) {
        return new RequestBuilder(client, path, "OPTIONS");
    }

    /**
     * 通用的创建请求构建器方法
     * @deprecated 建议使用具体的HTTP方法构建器方法替代，如 {@link #get}, {@link #post} 等
     * @param client API客户端实例
     * @param path 请求路径
     * @param method HTTP方法
     * @return RequestBuilder实例
     */
    @Deprecated
    public static RequestBuilder create(ApiClient client, String path, String method) {
        return new RequestBuilder(client, path, method);
    }

    /**
     * 添加查询参数
     * @param name 参数名
     * @param value 参数值
     * @return RequestBuilder实例(支持链式调用)
     */
    public RequestBuilder queryParam(String name, Object value) {
        if (value != null) {
            this.queryParams.addAll(HttpHelper.parameterToPair(name, value));
        }
        return this;
    }

    public RequestBuilder queryParams(List<Pair> queryParams) {
        this.queryParams.addAll(queryParams);
        return this;
    }

    /**
     * 添加集合类型的查询参数
     * @param name 参数名
     * @param value 集合参数值
     * @param format 格式化方式
     * @return RequestBuilder实例(支持链式调用)
     */
    public RequestBuilder collectionQueryParam(String name, Object value, String format) {
        if (value != null && value instanceof Collection) {
            this.collectionQueryParams.addAll(HttpHelper.parameterToPairs(format, name, (Collection) value));
        }
        return this;
    }

    public RequestBuilder collectionQueryParams(List<Pair> collectionQueryParams) {
        this.collectionQueryParams.addAll(collectionQueryParams);
        return this;
    }

    /**
     * 设置请求体
     * @param body 请求体对象
     * @return RequestBuilder实例(支持链式调用)
     */
    public RequestBuilder body(Object body) {
        this.body = body;
        return this;
    }

    /**
     * 添加请求头
     * @param name 请求头名称
     * @param value 请求头值
     * @return RequestBuilder实例(支持链式调用)
     */
    public RequestBuilder header(String name, String value) {
        if (value != null) {
            this.headerParams.put(name, value);
        }
        return this;
    }

    /**
     * 批量添加请求头
     * @param headers 请求头Map
     * @return RequestBuilder实例(支持链式调用)
     */
    public RequestBuilder headers(Map<String, String> headers) {
        if (headers != null) {
            this.headerParams.putAll(headers);
        }
        return this;
    }

    /**
     * 添加表单参数
     * @param name 参数名
     * @param value 参数值
     * @return RequestBuilder实例(支持链式调用)
     */
    public RequestBuilder formParam(String name, Object value) {
        if (value != null) {
            this.formParams.put(name, value);
        }
        return this;
    }

    public RequestBuilder formParams(Map<String, Object> formParams) {
        this.formParams.putAll(formParams);
        return this;
    }

    /**
     * 设置进度监听器
     * @param listener 进度监听器实例
     * @return RequestBuilder实例(支持链式调用)
     */
    public RequestBuilder progressListener(ProgressRequestListener listener) {
        this.progressListener = listener;
        return this;
    }

    /**
     * 构建最终的HTTP请求
     * @return Request对象
     * @throws ApiException 构建请求过程中的异常
     */
    public Request build() throws ApiException {
        client.updateParamsForAuth(path, headerParams);
        final String url = HttpHelper.buildUrl(client, path, queryParams, collectionQueryParams);
        final Request.Builder reqBuilder = new Request.Builder().url(url);
        HttpHelper.processHeaderParams(client, headerParams, reqBuilder);
        String contentType = Optional.ofNullable(headerParams.get("Content-Type")).orElse("application/json");
        RequestBody reqBody;
        if (!HttpMethod.permitsRequestBody(method)) {
            reqBody = null;
        } else if ("application/x-www-form-urlencoded".equals(contentType)) {
            reqBody = HttpHelper.buildRequestBodyFormEncoding(formParams);
        } else if ("multipart/form-data".equals(contentType)) {
            reqBody = HttpHelper.buildRequestBodyMultipart(formParams);
        } else if (body == null) {
            if ("DELETE".equals(method)) {
                // 允许调用DELETE而不发送请求体
                reqBody = null;
            } else {
                // 使用空请求体(用于POST、PUT和PATCH)
                reqBody = RequestBody.create(MediaType.parse(contentType), "");
            }
        } else {
            reqBody = HttpHelper.serialize(body, contentType);
        }
        Request request = null;
        if (progressListener != null && reqBody != null) {
            ProgressRequestBody progressRequestBody = new ProgressRequestBody(reqBody, progressListener);
            request = reqBuilder.method(method, progressRequestBody).build();
        } else {
            request = reqBuilder.method(method, reqBody).build();
        }
        return request;
    }
}