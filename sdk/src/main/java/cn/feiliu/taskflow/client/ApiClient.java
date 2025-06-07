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
package cn.feiliu.taskflow.client;

import cn.feiliu.taskflow.common.dto.ApiResponse;
import cn.feiliu.taskflow.common.exceptions.ApiException;
import cn.feiliu.taskflow.core.TaskEngine;
import cn.feiliu.taskflow.core.TokenManager;
import cn.feiliu.taskflow.executor.extension.TaskHandlerManager;
import cn.feiliu.taskflow.http.ApiCallback;
import cn.feiliu.taskflow.http.Pair;
import cn.feiliu.taskflow.http.RequestBuilder;
import cn.feiliu.taskflow.http.types.ResponseTypeHandler;
import cn.feiliu.taskflow.http.types.TypeFactory;
import cn.feiliu.taskflow.utils.ClientHelper;
import cn.feiliu.taskflow.utils.TaskflowConfig;
import lombok.Getter;
import lombok.SneakyThrows;
import okhttp3.*;

import javax.net.ssl.KeyManager;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API客户端类，用于处理HTTP请求和响应
 */
public final class ApiClient {
    // 默认请求头映射
    private final Map<String, String> defaultHeaderMap    = new ConcurrentHashMap<>();

    // 临时文件夹路径
    private String                    tempFolderPath;

    // SSL证书相关配置
    private InputStream               sslCaCert;
    private boolean                   verifyingSsl;
    private KeyManager[]              keyManagers;

    // HTTP客户端实例
    private OkHttpClient              httpClient;

    // 是否使用SSL连接
    private boolean                   useSSL;

    // 执行器线程数
    private int                       executorThreadCount = 0;

    // 令牌管理器
    private final TokenManager        tokenManager;
    // API集合
    private final TaskflowApis        apis;
    // 任务处理器管理器
    @Getter
    private final TaskHandlerManager  taskHandlerManager  = new TaskHandlerManager();
    private final TaskflowConfig      config;
    private final TaskEngine          taskEngine;

    /**
     * 构造函数
     */
    public ApiClient(TaskflowConfig config) {
        this.config = config;
        config.setBaseUrl(normalizePath(config.getBaseUrl()));
        this.httpClient = new OkHttpClient().newBuilder().retryOnConnectionFailure(true).build();
        this.verifyingSsl = true;
        this.apis = new TaskflowApis(this);
        this.tokenManager = new TokenManager(this.apis.getAuthClient(), config.getKeyId(), config.getKeySecret());
        //所有的对象初始化完成后，最后执行初始调度执行
        this.tokenManager.shouldStartSchedulerAndInitializeToken();
        this.taskEngine = new TaskEngine(this);
    }

    /**
     * 规范化基础路径
     *
     * @param basePath 原始基础路径
     * @return 规范化后的基础路径
     */
    private String normalizePath(String basePath) {
        Objects.requireNonNull(basePath, "basePath is required");
        if (basePath.endsWith("/")) {
            return basePath.substring(0, basePath.length() - 1);
        } else {
            return basePath;
        }
    }

    /**
     * 设置是否使用SSL连接(用于GRPC)
     *
     * @param useSSL 是否使用SSL连接
     */
    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    /**
     * 获取基础路径
     *
     * @return 基础路径
     */
    public String getBasePath() {
        return config.getBaseUrl();
    }

    /**
     * 获取执行器线程数
     *
     * @return 执行器线程数
     */
    public int getExecutorThreadCount() {
        return executorThreadCount;
    }

    /**
     * 设置执行器线程数
     *
     * @param executorThreadCount 执行器线程数
     */
    public void setExecutorThreadCount(int executorThreadCount) {
        this.executorThreadCount = executorThreadCount;
    }

    /**
     * 获取HTTP客户端实例
     *
     * @return OkHttpClient实例
     */
    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * 设置HTTP客户端
     *
     * @param httpClient OkHttpClient实例
     * @return ApiClient实例
     */
    public ApiClient setHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * 关闭客户端，释放资源
     */
    @SneakyThrows
    public void shutdown() {
        tokenManager.close();
    }

    /**
     * 检查是否启用SSL验证
     *
     * @return 如果启用SSL验证返回true，否则返回false
     */
    public boolean isVerifyingSsl() {
        return verifyingSsl;
    }

    /**
     * 获取SSL CA证书
     *
     * @return SSL CA证书输入流
     */
    public InputStream getSslCaCert() {
        return sslCaCert;
    }

    /**
     * 获取密钥管理器数组
     *
     * @return 密钥管理器数组
     */
    public KeyManager[] getKeyManagers() {
        return keyManagers;
    }

    /**
     * 设置User-Agent请求头
     *
     * @param userAgent HTTP请求的user agent
     * @return ApiClient实例
     */
    public ApiClient setUserAgent(String userAgent) {
        addDefaultHeader("User-Agent", userAgent);
        return this;
    }

    /**
     * 添加默认请求头
     *
     * @param key   请求头的键
     * @param value 请求头的值
     * @return ApiClient实例
     */
    public ApiClient addDefaultHeader(String key, String value) {
        defaultHeaderMap.put(key, value);
        return this;
    }

    /**
     * 获取临时文件夹路径
     * 用于存储从具有文件响应的端点下载的文件的临时文件夹的路径。
     * 默认值是null，即使用系统默认的临时文件夹。
     *
     * @return 临时文件夹路径
     * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/io/File.html#createTempFile">createTempFile</a>
     */
    public String getTempFolderPath() {
        return tempFolderPath;
    }

    /**
     * 设置临时文件夹路径
     *
     * @param tempFolderPath 临时文件夹路径
     * @return ApiClient实例
     */
    public ApiClient setTempFolderPath(String tempFolderPath) {
        this.tempFolderPath = tempFolderPath;
        return this;
    }

    /**
     * 执行HTTP调用
     *
     * @param <T>  返回类型
     * @param call Call对象实例
     * @return ApiResponse&lt;T&gt;
     * @throws ApiException 如果执行调用失败
     */
    public <T> ApiResponse<T> execute(Call call) throws ApiException {
        return execute(call, null);
    }

    /**
     * 执行HTTP调用并将响应体反序列化为指定的返回类型
     *
     * @param returnType 用于反序列化HTTP响应体的返回类型
     * @param <T>        与returnType对应的返回类型
     * @param call       Call对象
     * @return 包含响应状态、头部和数据的ApiResponse对象
     * @throws ApiException 如果执行调用失败
     */
    public <T> ApiResponse<T> execute(Call call, Type returnType) throws ApiException {
        return doExecute(call, TypeFactory.of(returnType));
    }

    /**
     * 执行HTTP调用并处理响应
     *
     * @param <T>          返回类型
     * @param call         Call对象
     * @param responseType 响应类型处理器
     * @return ApiResponse对象
     * @throws ApiException 如果执行调用失败
     */
    public <T> ApiResponse<T> doExecute(Call call, ResponseTypeHandler responseType) throws ApiException {
        try {
            Response response = call.execute();
            responseType = Optional.ofNullable(responseType).orElseGet(() -> TypeFactory.empty());
            return responseType.handleResponse(this, response);
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }

    /**
     * 异步执行HTTP调用
     *
     * @param <T>      返回类型
     * @param call     Call对象
     * @param callback 回调接口
     */
    public <T> void executeAsync(Call call, ApiCallback<T> callback) {
        executeAsync(call, null, callback);
    }

    /**
     * 异步执行HTTP调用
     *
     * @param <T>        返回类型
     * @param call       Call对象
     * @param returnType 返回类型
     * @param callback   回调接口
     */
    @SuppressWarnings("unchecked")
    public <T> void executeAsync(Call call, final Type returnType, final ApiCallback<T> callback) {
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(new ApiException(e), 0, null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                T result;
                try {
                    result = (T) ClientHelper.handleResponse(ApiClient.this, response, returnType);
                } catch (ApiException e) {
                    callback.onFailure(e, response.code(), response.headers().toMultimap());
                    return;
                }
                callback.onSuccess(result, response.code(), response.headers().toMultimap());
            }
        });
    }

    /**
     * 根据给定参数构建HTTP请求
     *
     * @param path                  HTTP请求的子路径(uri)
     * @param method                请求方法 ["GET", "HEAD", "OPTIONS", "POST", "PUT", "PATCH", "DELETE"]
     * @param queryParams           查询参数
     * @param collectionQueryParams 集合查询参数
     * @param body                  请求体参数
     * @param formParams            表单参数
     * @return HTTP调用对象
     * @throws ApiException 当序列化请求对象失败时抛出该异常
     */
    public Call buildCall(String path, String method, List<Pair> queryParams, List<Pair> collectionQueryParams,
                          Object body, Map<String, Object> formParams) throws ApiException {
        Request request = RequestBuilder.of(this, path, method).queryParams(queryParams)
            .collectionQueryParams(collectionQueryParams).body(body).headers(headerParams).formParams(formParams)
            .build();
        return httpClient.newCall(request);
    }

    /**
     * 根据身份验证设置更新请求头参数
     *
     * @param path         请求路径
     * @param headerParams 请求头参数映射
     */
    public void updateParamsForAuth(String path, Map<String, String> headerParams) {
        if ("/auth/token".equalsIgnoreCase(path)) {
            headerParams.put("Authorization", tokenManager.constructCredentials());
        } else {
            headerParams.put("Authorization", getToken());
        }
    }

    /**
     * 获取默认请求头映射
     *
     * @return 默认请求头映射的不可修改视图
     */
    public Map<String, String> getDefaultHeaderMap() {
        return Collections.unmodifiableMap(defaultHeaderMap);
    }

    /**
     * 获取访问令牌
     *
     * @return Bearer令牌
     */
    public String getToken() {
        return tokenManager.getBearerToken();
    }

    /**
     * 刷新访问令牌
     */
    public void shouldRefreshToken() {
        tokenManager.tryFlushToken();
    }

    // 请求头参数映射
    private final Map<String, String> headerParams = new HashMap<>();

    {
        headerParams.put("Accept", "application/json");
        headerParams.put("Content-Type", "application/json");
    }

    /**
     * 构建POST请求
     *
     * @param localVarPath 请求路径
     * @param body         请求体
     * @return Call对象
     */
    public Call buildPostCall(String localVarPath, Object body) {
        Request request = RequestBuilder.post(this, localVarPath).body(body).headers(headerParams).build();
        return httpClient.newCall(request);
    }

    /**
     * 构建带查询参数的POST请求
     *
     * @param localVarPath 请求路径
     * @param body         请求体
     * @param queryParams  查询参数
     * @return Call对象
     */
    public Call buildPostCall(String localVarPath, Object body, List<Pair> queryParams) {
        Request request = RequestBuilder.post(this, localVarPath).body(body).headers(headerParams)
            .queryParams(queryParams).build();
        return httpClient.newCall(request);
    }

    /**
     * 构建GET请求
     *
     * @param localVarPath          请求路径
     * @param collectionQueryParams 集合查询参数
     * @return Call对象
     */
    public Call buildGetCall(String localVarPath, List<Pair> collectionQueryParams) {
        Request request = RequestBuilder.get(this, localVarPath).collectionQueryParams(collectionQueryParams)
            .headers(headerParams).build();
        return httpClient.newCall(request);
    }

    /**
     * 构建DELETE请求
     *
     * @param path 请求路径
     * @return Call对象
     */
    public Call buildDeleteCall(String path) {
        Request request = RequestBuilder.delete(this, path).build();
        return httpClient.newCall(request);
    }

    /**
     * 构建带参数的DELETE请求
     *
     * @param path   请求路径
     * @param params 请求参数
     * @return Call对象
     */
    public Call buildDeleteCall(String path, List<Pair> params) {
        Request request = RequestBuilder.delete(this, path).collectionQueryParams(params).headers(headerParams).build();
        return httpClient.newCall(request);
    }

    /**
     * 获取平台多客户端API集合
     *
     * @return TaskflowApis实例
     */
    public TaskflowApis getApis() {
        return Objects.requireNonNull(apis);
    }

    public TaskflowConfig getConfig() {
        return config;
    }

    public boolean isSupportWebSocket() {
        return config.isSupportWebsocket();
    }

    /**
     * 添加自定义算子节点(工作节点)
     *
     * @param workerBeans
     * @return
     */
    public ApiClient addWorker(Collection<Object> workerBeans) {
        this.taskEngine.addWorkers(workerBeans.toArray());
        return this;
    }

    /**
     * 添加自定义算子节点(工作节点)
     *
     * @param workerBeans
     * @return
     */
    public ApiClient addWorker(Object... workerBeans) {
        this.taskEngine.addWorkers(workerBeans);
        return this;
    }

    public ApiClient start() {
        this.taskEngine.start();
        return this;
    }

}
