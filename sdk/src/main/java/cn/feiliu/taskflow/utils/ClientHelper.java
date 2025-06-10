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
package cn.feiliu.taskflow.utils;

import cn.feiliu.common.api.encoder.EncoderFactory;
import cn.feiliu.common.api.model.resp.DataResult;
import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.common.exceptions.ApiException;
import cn.feiliu.taskflow.http.Pair;
import cn.feiliu.taskflow.http.types.ResponseTypeHandler;
import okhttp3.*;
import okio.BufferedSink;
import okio.Okio;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 客户端工具类
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-06-05
 */
public class ClientHelper {
    /**
     * 为请求构建器设置报头参数，包括默认报头。
     *
     * @param headerParams 以Map形式提供的Header参数
     * @param reqBuilder   Request.Builder对象
     */
    public static void processHeaderParams(ApiClient client, Map<String, String> headerParams,
                                           Request.Builder reqBuilder) {
        for (Map.Entry<String, String> param : headerParams.entrySet()) {
            reqBuilder.header(param.getKey(), ClientHelper.parameterToString(param.getValue()));
        }
        Map<String, String> defHeaders = client.getDefaultHeaderMap();
        for (Map.Entry<String, String> header : defHeaders.entrySet()) {
            if (!headerParams.containsKey(header.getKey())) {
                reqBuilder.header(header.getKey(), ClientHelper.parameterToString(header.getValue()));
            }
        }
    }

    /**
     * 使用给定的表单参数构建一个表单编码的请求体。
     *
     * @param formParams 以Map形式提供的表单参数
     * @return RequestBody对象
     */
    public static RequestBody buildRequestBodyFormEncoding(Map<String, Object> formParams) {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, Object> param : formParams.entrySet()) {
            builder.add(param.getKey(), ClientHelper.parameterToString(param.getValue()));
        }
        return builder.build();
    }

    /**
     * 处理给定的响应，当响应成功时返回反序列化的对象。
     *
     * @param <T>        返回类型的泛型参数
     * @param response   响应对象
     * @param returnType 返回类型
     * @return 反序列化后的对象
     * @throws ApiException 如果响应状态码不成功或反序列化响应体失败时抛出
     */
    public static <T> T handleResponse(ApiClient client, Response response, Type returnType) throws ApiException {
        if (response.isSuccessful()) {
            if (returnType == null || response.code() == 204) {
                // 如果未定义returnType，则返回null，或者状态码为204(无内容)
                if (response.body() != null) {
                    response.body().close();
                }
                return null;
            } else {
                return ClientHelper.deserialize(client, response, returnType);
            }
        } else {
            throw new ApiException(response.message(), response.code(), response.headers().toMultimap(), null);
        }
    }

    /**
     * 将参数对象格式化为字符串。
     *
     * @param param 需要格式化的参数对象
     * @return 格式化后的字符串
     */
    public static String parameterToString(Object param) {
        if (param == null) {
            return "";
        } else if (param instanceof Date /*|| param instanceof OffsetDateTime || param instanceof LocalDate*/) {
            // 序列化为json字符串并移除首尾的引号
            String jsonStr = EncoderFactory.getJsonEncoder().encode(param);
            return jsonStr.substring(1, jsonStr.length() - 1);
        } else if (param instanceof Collection) {
            StringBuilder b = new StringBuilder();
            for (Object o : (Collection) param) {
                if (b.length() > 0) {
                    b.append(",");
                }
                b.append(String.valueOf(o));
            }
            return b.toString();
        } else {
            return String.valueOf(param);
        }
    }

    /**
     * 构建一个多部分（文件上传）请求体，可以包含文本字段和文件字段。
     *
     * @param formParams 以Map形式提供的表单参数
     * @return RequestBody对象
     */
    public static RequestBody buildRequestBodyMultipart(Map<String, Object> formParams) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        for (Map.Entry<String, Object> param : formParams.entrySet()) {
            if (param.getValue() instanceof File) {
                File file = (File) param.getValue();
                Headers partHeaders = Headers.of("Content-Disposition", "form-data; name=\"" + param.getKey()
                                                                        + "\"; filename=\"" + file.getName() + "\"");
                MediaType mediaType = MediaType.parse(guessContentTypeFromFile(file));
                builder.addPart(partHeaders, RequestBody.create(mediaType, file));
            } else {
                Headers partHeaders = Headers.of("Content-Disposition", "form-data; name=\"" + param.getKey() + "\"");
                builder.addPart(partHeaders, RequestBody.create(null, parameterToString(param.getValue())));
            }
        }
        return builder.build();
    }

    /**
     * 根据对象的类和请求的Content-Type将给定的Java对象序列化为请求体。
     *
     * @param obj         要序列化的Java对象
     * @param contentType 请求的Content-Type
     * @return 序列化后的请求体
     * @throws ApiException 如果序列化给定对象失败时抛出
     */
    public static RequestBody serialize(Object obj, String contentType) throws ApiException {
        if (obj instanceof byte[]) {
            // 二进制（字节数组）体参数支持
            return RequestBody.create(MediaType.parse(contentType), (byte[]) obj);
        } else if (obj instanceof File) {
            // 文件体参数支持
            return RequestBody.create(MediaType.parse(contentType), (File) obj);
        } else if (isJsonMime(contentType)) {
            String content = null;
            if (obj != null) {
                if (obj instanceof String) {
                    content = (String) obj;
                } else {
                    content = EncoderFactory.getJsonEncoder().encode(obj);
                }
            }
            return RequestBody.create(MediaType.parse(contentType), content);
        } else {
            throw new ApiException("Content type \"" + contentType + "\" is not supported");
        }
    }

    /**
     * 从给定的响应中下载文件。
     *
     * @param response Response对象
     * @return 下载的文件
     * @throws ApiException 如果从响应中读取文件内容并写入磁盘失败时抛出
     */
    public static File downloadFileFromResponse(ApiClient client, Response response) throws ApiException {
        try {
            File file = prepareDownloadFile(client, response);
            BufferedSink sink = Okio.buffer(Okio.sink(file));
            sink.writeAll(response.body().source());
            sink.close();
            return file;
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }

    /**
     * 通过删除路径来清理文件名。
     * 例如：../../sun.gif 变成 sun.gif
     *
     * @param filename 需要清理的文件名
     * @return 清理后的文件名
     */
    public static String sanitizeFilename(String filename) {
        return filename.replaceAll(".*[/\\\\]", "");
    }

    /**
     * 准备下载文件
     *
     * @param response Response对象
     * @return 准备好的下载文件
     * @throws IOException 如果准备下载文件失败时抛出
     */
    public static File prepareDownloadFile(ApiClient client, Response response) throws IOException {
        String filename = null;
        String contentDisposition = response.header("Content-Disposition");
        if (contentDisposition != null && !"".equals(contentDisposition)) {
            // 从Content-Disposition头部获取文件名
            Pattern pattern = Pattern.compile("filename=['\"]?([^'\"\\s]+)['\"]?");
            Matcher matcher = pattern.matcher(contentDisposition);
            if (matcher.find()) {
                filename = sanitizeFilename(matcher.group(1));
            }
        }

        String prefix = null;
        String suffix = null;
        if (filename == null) {
            prefix = "download-";
            suffix = "";
        } else {
            int pos = filename.lastIndexOf(".");
            if (pos == -1) {
                prefix = filename + "-";
            } else {
                prefix = filename.substring(0, pos) + "-";
                suffix = filename.substring(pos);
            }
            // File.createTempFile要求前缀至少为3个字符
            if (prefix.length() < 3)
                prefix = "download-";
        }
        if (client.getTempFolderPath() == null)
            return Files.createTempFile(prefix, suffix).toFile();
        else
            return Files.createTempFile(Paths.get(client.getTempFolderPath()), prefix, suffix).toFile();
    }

    /**
     * 根据返回类型和Content-Type响应头，将响应体反序列化为Java对象。
     *
     * @param <T>        返回类型的泛型参数
     * @param response   HTTP响应
     * @param returnType Java对象的类型
     * @return 反序列化后的Java对象
     * @throws ApiException 如果反序列化响应体失败，即无法读取响应体或不支持响应的Content-Type时抛出
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(ApiClient client, Response response, Type returnType) throws ApiException {
        if (response == null || returnType == null) {
            return null;
        }

        if ("byte[]".equals(returnType.toString())) {
            // 处理二进制响应（字节数组）
            try {
                return (T) response.body().bytes();
            } catch (IOException e) {
                throw new ApiException(e);
            }
        } else if (returnType.equals(File.class)) {
            // 处理文件下载
            return (T) downloadFileFromResponse(client, response);
        }

        String respBody;
        try {
            if (response.body() != null)
                respBody = response.body().string();
            else
                respBody = null;
        } catch (IOException e) {
            throw new ApiException(e);
        }

        if (respBody == null || "".equals(respBody)) {
            return null;
        }

        String contentType = response.headers().get("Content-Type");
        if (contentType == null) {
            // 确保默认的content type
            contentType = "application/json";
        }
        if (ClientHelper.isJsonMime(contentType)) {
            return EncoderFactory.getJsonEncoder().decode(respBody, returnType);
        } else if (returnType.equals(String.class)) {
            // 期望字符串，返回原始响应体
            return (T) respBody;
        } else {
            throw new ApiException("Content type \"" + contentType + "\" is not supported for type: " + returnType,
                response.code(), response.headers().toMultimap(), null);
        }
    }

    /**
     * 反序列化响应为ApiResponse对象
     *
     * @param <T>          返回类型的泛型参数
     * @param response     响应对象
     * @param responseType 响应类型处理器
     * @return ApiResponse对象
     * @throws ApiException 如果反序列化失败时抛出
     */
    public static <T> DataResult<T> deserialize(ApiClient client, Response response, ResponseTypeHandler responseType)
                                                                                                                      throws ApiException {
        Type returnType = responseType.getElementType();
        if ("byte[]".equals(returnType.toString())) {
            // 处理二进制响应（字节数组）
            try {
                byte[] data = response.body().bytes();
                return (DataResult<T>) DataResult.ok(data);
            } catch (IOException e) {
                throw new ApiException(e);
            }
        } else if (returnType.equals(File.class)) {
            // 处理文件下载
            File file = downloadFileFromResponse(client, response);
            return (DataResult<T>) DataResult.ok(file);
        }
        String respBody;
        try {
            if (response.body() != null)
                respBody = response.body().string();
            else
                respBody = null;
        } catch (IOException e) {
            throw new ApiException(e);
        }
        if (respBody == null || "".equals(respBody)) {
            return DataResult.ok(null);
        }

        String contentType = response.headers().get("Content-Type");
        if (contentType == null) {
            // 确保默认的content type
            contentType = "application/json";
        }
        if (ClientHelper.isJsonMime(contentType)) {
            return EncoderFactory.getJsonEncoder().decode(respBody, responseType.getType());
        } else if (returnType.equals(String.class)) {
            // 期望字符串，返回原始响应体
            return (DataResult<T>) DataResult.ok(respBody);
        } else {
            throw new ApiException("Content type \"" + contentType + "\" is not supported for type: " + returnType,
                response.code(), response.headers().toMultimap());
        }
    }

    /**
     * 检查给定的MIME是否为JSON MIME。
     * JSON MIME示例:
     * - application/json
     * - application/json; charset=UTF8
     * - APPLICATION/JSON
     * - application/vnd.company+json
     *
     * @param mime MIME类型
     * @return 如果给定的MIME是JSON则返回true，否则返回false
     */
    public static boolean isJsonMime(String mime) {
        String jsonMime = "(?i)^(application/json|[^;/ \t]+/[^;/ \t]+[+]json)[ \t]*(;.*)?$";
        return mime != null && (mime.matches(jsonMime) || mime.equals("*/*"));
    }

    /**
     * 将指定的查询参数格式化为包含单个{@code Pair}对象的列表。
     * 注意{@code value}不能是一个集合。
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 包含单个{@code Pair}对象的列表
     */
    public static List<Pair> parameterToPair(String name, Object value) {
        List<Pair> params = new ArrayList<Pair>();

        // 前置条件检查
        if (name == null || name.isEmpty() || value == null || value instanceof Collection)
            return params;

        params.add(new Pair(name, ClientHelper.parameterToString(value)));
        return params;
    }

    /**
     * 通过连接基本路径、给定子路径和查询参数来构建完整的URL。
     *
     * @param path                  子路径
     * @param queryParams           查询参数
     * @param collectionQueryParams 集合查询参数
     * @return 完整的URL
     */
    public static String buildUrl(ApiClient client, String path, List<Pair> queryParams,
                                  List<Pair> collectionQueryParams) {
        final StringBuilder url = new StringBuilder();
        url.append(client.getBasePath()).append(path);
        if (queryParams != null && !queryParams.isEmpty()) {
            String prefix = path.contains("?") ? "&" : "?";
            for (Pair param : queryParams) {
                if (param.getValue() != null) {
                    if (prefix != null) {
                        url.append(prefix);
                        prefix = null;
                    } else {
                        url.append("&");
                    }
                    String value = ClientHelper.parameterToString(param.getValue());
                    url.append(escapeString(param.getName())).append("=").append(escapeString(value));
                }
            }
        }
        if (collectionQueryParams != null && !collectionQueryParams.isEmpty()) {
            String prefix = url.toString().contains("?") ? "&" : "?";
            for (Pair param : collectionQueryParams) {
                if (param.getValue() != null) {
                    if (prefix != null) {
                        url.append(prefix);
                        prefix = null;
                    } else {
                        url.append("&");
                    }
                    String value = ClientHelper.parameterToString(param.getValue());
                    url.append(escapeString(param.getName())).append("=").append(value);
                }
            }
        }
        return url.toString();
    }

    /**
     * 对URL参数进行转义
     *
     * @param str 要转义的字符串
     * @return 转义后的字符串
     */
    public static String escapeString(String str) {
        try {
            return URLEncoder.encode(str, "utf8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    /**
     * 从给定文件中猜测内容类型头（默认为"application/octet-stream"）。
     *
     * @param file 给定的文件
     * @return 猜测的Content-Type
     */
    public static String guessContentTypeFromFile(File file) {
        String contentType = URLConnection.guessContentTypeFromName(file.getName());
        if (contentType == null) {
            return "application/octet-stream";
        } else {
            return contentType;
        }
    }

    /**
     * 使用给定的分隔符连接字符串数组。
     * 注意：如果将来添加commons-lang或guava作为依赖，这个方法可能会被替换为这些库中的工具方法。
     *
     * @param array     要连接的字符串数组
     * @param separator 分隔符
     * @return 连接后的字符串结果
     */
    public static String join(String[] array, String separator) {
        if (array.length == 0)
            return "";

        StringBuilder out = new StringBuilder();
        out.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            out.append(separator).append(array[i]);
        }
        return out.toString();
    }

    /**
     * 创建一个新的空KeyStore
     *
     * @param password KeyStore的密码
     * @return 新创建的空KeyStore
     * @throws GeneralSecurityException 如果创建KeyStore失败时抛出
     */
    public static KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, password);
            return keyStore;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
