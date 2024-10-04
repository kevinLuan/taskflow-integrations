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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.*;

import cn.feiliu.taskflow.client.core.TokenManager;
import cn.feiliu.taskflow.open.ApiResponse;
import cn.feiliu.taskflow.client.http.*;
import cn.feiliu.taskflow.client.http.types.TypeFactory;
import cn.feiliu.taskflow.client.http.types.ResponseTypeHandler;
import cn.feiliu.taskflow.client.utils.HttpHelper;
import cn.feiliu.taskflow.client.utils.SecurityHelper;
import cn.feiliu.taskflow.open.exceptions.ApiException;
import cn.feiliu.taskflow.sdk.workflow.executor.extension.TaskHandlerManager;
import com.squareup.okhttp.*;
import lombok.Getter;
import lombok.SneakyThrows;

public class ApiClient {
    private final String              basePath;
    private final Map<String, String> defaultHeaderMap    = new HashMap();

    private String                    tempFolderPath;

    private InputStream               sslCaCert;
    private boolean                   verifyingSsl;
    private KeyManager[]              keyManagers;

    private OkHttpClient              httpClient;
    private String                    grpcHost            = "localhost";
    private int                       grpcPort            = 9000;

    private boolean                   useSSL;

    private boolean                   useGRPC;

    private int                       executorThreadCount = 0;

    private final TokenManager        tokenManager;
    private final TaskflowApis        apis;
    @Getter
    private final TaskHandlerManager  taskHandlerManager  = new TaskHandlerManager();

    public ApiClient(String basePath, String keyId, String keySecret) {
        this.basePath = normalizePath(basePath);
        this.httpClient = new OkHttpClient();
        this.httpClient.setRetryOnConnectionFailure(true);
        this.verifyingSsl = true;
        this.tokenManager = new TokenManager(this, keyId, keySecret);
        this.apis = new TaskflowApis(this);
    }

    private String normalizePath(String basePath) {
        Objects.requireNonNull(basePath, "basePath is required");
        if (basePath.endsWith("/")) {
            return basePath.substring(0, basePath.length() - 1);
        } else {
            return basePath;
        }
    }

    public boolean isUseGRPC() {
        return useGRPC;
    }

    public void setUseGRPC(String host, int port) {
        this.grpcHost = host;
        this.grpcPort = port;
        if (this.useGRPC = getApis().isGrpcSpiAvailable()) {
            getApis().getGrpcApi().init(this);
        }
    }

    public boolean useSSL() {
        return useSSL;
    }

    /**
     * Used for GRPC
     *
     * @param useSSL set f using SSL connection for gRPC
     */
    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    /**
     * Get base path
     *
     * @return Base path
     */
    public String getBasePath() {
        return basePath;
    }

    public int getExecutorThreadCount() {
        return executorThreadCount;
    }

    public void setExecutorThreadCount(int executorThreadCount) {
        this.executorThreadCount = executorThreadCount;
    }

    /**
     * Get HTTP client
     *
     * @return An instance of OkHttpClient
     */
    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Set HTTP client
     *
     * @param httpClient An instance of OkHttpClient
     * @return Api Client
     */
    public ApiClient setHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    @SneakyThrows
    public void shutdown() {
        this.httpClient.getDispatcher().getExecutorService().shutdown();
        tokenManager.close();
        apis.shutdown();
    }

    public int getGrpcPort() {
        return grpcPort;
    }

    public String getGrpcHost() {
        return grpcHost;
    }

    public void setGrpcPort(int grpcPort) {
        this.grpcPort = grpcPort;
    }

    public String getHost() {
        try {
            return new URL(basePath).getHost();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 如果isVerifyingSsl标志打开，则为True
     */
    public boolean isVerifyingSsl() {
        return verifyingSsl;
    }

    /**
     * 配置https请求时是否验证证书和主机名。默认为true。注意:不要在产品代码中设置为false，否则您将面临多种类型的加密攻击。
     *
     * @param verifyingSsl True to verify TLS/SSL connection
     * @return ApiClient
     */
    public ApiClient setVerifyingSsl(boolean verifyingSsl) {
        this.verifyingSsl = verifyingSsl;
        applySslSettings();
        return this;
    }

    /**
     * 获取SSL CA证书
     *
     * @return Input stream to the SSL CA cert
     */
    public InputStream getSslCaCert() {
        return sslCaCert;
    }

    /**
     * 在发起https请求时，配置CA证书为受信任证书。使用null重置为默认值。
     *
     * @param sslCaCert input stream for SSL CA cert
     * @return ApiClient
     */
    public ApiClient setSslCaCert(InputStream sslCaCert) {
        this.sslCaCert = sslCaCert;
        applySslSettings();
        return this;
    }

    public KeyManager[] getKeyManagers() {
        return keyManagers;
    }

    /**
     * 配置在SSL会话中用于授权的客户端密钥。使用null重置为默认值。
     *
     * @param managers The KeyManagers to use
     * @return ApiClient
     */
    public ApiClient setKeyManagers(KeyManager[] managers) {
        this.keyManagers = managers;
        applySslSettings();
        return this;
    }

    /**
     * Set the User-Agent header's value (by adding to the default header map).
     *
     * @param userAgent HTTP request's user agent
     * @return ApiClient
     */
    public ApiClient setUserAgent(String userAgent) {
        addDefaultHeader("User-Agent", userAgent);
        return this;
    }

    /**
     * 添加默认标头
     *
     * @param key   The header's key
     * @param value The header's value
     * @return ApiClient
     */
    public ApiClient addDefaultHeader(String key, String value) {
        defaultHeaderMap.put(key, value);
        return this;
    }

    /**
     * 用于存储从具有文件响应的端点下载的文件的临时文件夹的路径。默认值是<code>null<code>，即使用系统默认的临时文件夹。
     *
     * @return 临时文件夹路径
     * @see <a href=
     * "https://docs.oracle.com/javase/7/docs/api/java/io/File.html#createTempFile">createTempFile</a>
     */
    public String getTempFolderPath() {
        return tempFolderPath;
    }

    /**
     * 设置临时文件夹路径(用于下载文件)
     *
     * @param tempFolderPath 临时文件夹路径
     * @return ApiClient
     */
    public ApiClient setTempFolderPath(String tempFolderPath) {
        this.tempFolderPath = tempFolderPath;
        return this;
    }

    /**
     * 获取连接超时(以毫秒为单位)。
     *
     * @return Timeout in milliseconds
     */
    public int getConnectTimeout() {
        return httpClient.getConnectTimeout();
    }

    /**
     * 设置连接超时(以毫秒为单位)。值为0表示没有超时，否则值必须在1到之间
     *
     * @param connectionTimeout 连接超时(毫秒)
     * @return Api client
     */
    public ApiClient setConnectTimeout(int connectionTimeout) {
        httpClient.setConnectTimeout(connectionTimeout, TimeUnit.MILLISECONDS);
        return this;
    }

    /**
     * 获取读取超时(以毫秒为单位)。
     *
     * @return Timeout in milliseconds
     */
    public int getReadTimeout() {
        return httpClient.getReadTimeout();
    }

    /**
     * Sets the read timeout (in milliseconds). A value of 0 means no timeout, otherwise values must
     * be between 1 and {@link Integer#MAX_VALUE}.
     *
     * @param readTimeout read timeout in milliseconds
     * @return Api client
     */
    public ApiClient setReadTimeout(int readTimeout) {
        httpClient.setReadTimeout(readTimeout, TimeUnit.MILLISECONDS);
        return this;
    }

    /**
     * Get write timeout (in milliseconds).
     *
     * @return Timeout in milliseconds
     */
    public int getWriteTimeout() {
        return httpClient.getWriteTimeout();
    }

    /**
     * Sets the write timeout (in milliseconds). A value of 0 means no timeout, otherwise values
     * must be between 1 and {@link Integer#MAX_VALUE}.
     *
     * @param writeTimeout connection timeout in milliseconds
     * @return Api client
     */
    public ApiClient setWriteTimeout(int writeTimeout) {
        httpClient.setWriteTimeout(writeTimeout, TimeUnit.MILLISECONDS);
        return this;
    }

    /**
     * {@link #execute(Call, Type)}
     *
     * @param <T>  Type
     * @param call An instance of the Call object
     * @return ApiResponse&lt;T&gt;
     * @throws ApiException If fail to execute the call
     */
    public <T> ApiResponse<T> execute(Call call) throws ApiException {
        return execute(call, null);
    }

    /**
     * Execute HTTP call and deserialize the HTTP response body into the given return type.
     *
     * @param returnType The return type used to deserialize HTTP response body
     * @param <T>        The return type corresponding to (same with) returnType
     * @param call       Call
     * @return ApiResponse object containing response status, headers and data, which is a Java
     * object deserialized from response body and would be null when returnType is null.
     * @throws ApiException If fail to execute the call
     */
    public <T> ApiResponse<T> execute(Call call, Type returnType) throws ApiException {
        return doExecute(call, TypeFactory.of(returnType));
    }

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
     * {@link #executeAsync(Call, Type, ApiCallback)}
     *
     * @param <T>      Type
     * @param call     An instance of the Call object
     * @param callback ApiCallback&lt;T&gt;
     */
    public <T> void executeAsync(Call call, ApiCallback<T> callback) {
        executeAsync(call, null, callback);
    }

    /**
     * 异步执行HTTP调用
     *
     * @param <T>        Type
     * @param call       当API调用结束时执行的回调
     * @param returnType Return type
     * @param callback   ApiCallback
     * @see #execute(Call, Type)
     */
    @SuppressWarnings("unchecked")
    public <T> void executeAsync(Call call, final Type returnType, final ApiCallback<T> callback) {
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onFailure(new ApiException(e), 0, null);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                T result;
                try {
                    result = (T) HttpHelper.handleResponse(ApiClient.this, response, returnType);
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
     * @param path                    HTTP 请求的子路径(uri)
     * @param method                  请求方法 ["GET", "HEAD", "OPTIONS", "POST", "PUT", "PATCH", "DELETE"]
     * @param queryParams             查询参数
     * @param collectionQueryParams   收集查询参数
     * @param body                    请求Body参数
     * @param headerParams            请求Header参数
     * @param formParams              请求Form参数
     * @param progressRequestListener 进度请求监听器
     * @return The HTTP call
     * @throws ApiException 当序列化请求对象失败时抛出该异常
     */
    public Call buildCall(String path, String method, List<Pair> queryParams, List<Pair> collectionQueryParams,
                          Object body, Map<String, String> headerParams, Map<String, Object> formParams,
                          ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Request request = HttpHelper.buildRequest(this, path, method, queryParams, collectionQueryParams, body,
            headerParams, formParams, progressRequestListener);
        return httpClient.newCall(request);
    }

    /**
     * 根据身份验证设置更新查询和报头参数
     *
     * @param headerParams Header参数映射
     */
    public void updateParamsForAuth(Map<String, String> headerParams) {
        if (tokenManager.useSecurity()) {
            headerParams.put("X-Authorization", getToken());
        }
    }

    /**
     * 根据“verifyingSsl”和“sslCaCert”的当前值，对httpClient应用SSL相关设置。
     */
    private void applySslSettings() {
        try {
            TrustManager[] trustManagers = null;
            HostnameVerifier hostnameVerifier = null;
            if (!verifyingSsl) {
                TrustManager trustAll = new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType)
                                                                                            throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType)
                                                                                            throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                };
                trustManagers = new TrustManager[] { trustAll };
                hostnameVerifier = new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };
            } else if (sslCaCert != null) {
                char[] password = null; // Any password will work.
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(sslCaCert);
                if (certificates.isEmpty()) {
                    throw new IllegalArgumentException("expected non-empty set of trusted certificates");
                }
                KeyStore caKeyStore = SecurityHelper.newEmptyKeyStore(password);
                int index = 0;
                for (Certificate certificate : certificates) {
                    String certificateAlias = "ca" + Integer.toString(index++);
                    caKeyStore.setCertificateEntry(certificateAlias, certificate);
                }
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory
                    .getDefaultAlgorithm());
                trustManagerFactory.init(caKeyStore);
                trustManagers = trustManagerFactory.getTrustManagers();
            }

            if (keyManagers != null || trustManagers != null) {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(keyManagers, trustManagers, new SecureRandom());
                httpClient.setSslSocketFactory(sslContext.getSocketFactory());
            } else {
                httpClient.setSslSocketFactory(null);
            }
            httpClient.setHostnameVerifier(hostnameVerifier);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> getDefaultHeaderMap() {
        return Collections.unmodifiableMap(defaultHeaderMap);
    }

    public boolean useSecurity() {
        return tokenManager.useSecurity();
    }

    public String getToken() {
        return tokenManager.getToken();
    }

    public void shouldRefreshToken() {
        tokenManager.tryFlushToken();
    }

    private final Map<String, String> headerParams = new HashMap<>();

    {
        headerParams.put("Accept", "application/json");
        headerParams.put("Content-Type", "application/json");
    }

    public Call buildPostCall(String localVarPath, Object body) {
        List<Pair> queryParams = new ArrayList<>();
        List<Pair> collectionQueryParams = new ArrayList<>();
        Map<String, Object> formParams = new HashMap<>();
        return buildCall(localVarPath, "POST", queryParams, collectionQueryParams, body, headerParams, formParams, null);
    }

    public Call buildPostCall(String localVarPath, Object body, List<Pair> queryParams) {
        List<Pair> collectionQueryParams = new ArrayList<>();
        return buildCall(localVarPath, "POST", queryParams, collectionQueryParams, body, headerParams, new HashMap<>(),
            null);
    }

    public Call buildGetCall(String localVarPath, List<Pair> collectionQueryParams) {
        List<Pair> queryParams = new ArrayList<>();
        Map<String, Object> formParams = new HashMap<>();
        return buildCall(localVarPath, "GET", queryParams, collectionQueryParams, null, headerParams, formParams, null);
    }

    public Call buildDeleteCall(String path) {
        return this.buildDeleteCall(path, new ArrayList<>());
    }

    public Call buildDeleteCall(String path, List<Pair> params) {
        Map<String, Object> formParams = new HashMap<>();
        return buildCall(path, "DELETE", new ArrayList<>(), params, null, headerParams, formParams, null);
    }

    /**
     * 获取平台多客户端
     *
     * @return
     */
    public TaskflowApis getApis() {
        return Objects.requireNonNull(apis);
    }
}
