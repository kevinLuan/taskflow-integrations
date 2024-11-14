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
package cn.feiliu.taskflow.client.utils;

import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.open.ApiResponse;
import cn.feiliu.taskflow.client.http.*;
import cn.feiliu.taskflow.client.http.types.ResponseTypeHandler;
import cn.feiliu.taskflow.open.exceptions.ApiException;
import cn.feiliu.taskflow.serialization.SerializerFactory;
import com.squareup.okhttp.*;
import com.squareup.okhttp.internal.http.HttpMethod;
import okio.BufferedSink;
import okio.Okio;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-06-05
 */
public class HttpHelper {
    /**
     * 为请求构建器设置报头参数，包括默认报头。
     *
     * @param headerParams Header parameters in the from of Map
     * @param reqBuilder   Request.Builder
     */
    private static void processHeaderParams(ApiClient client, Map<String, String> headerParams,
                                            Request.Builder reqBuilder) {
        for (Map.Entry<String, String> param : headerParams.entrySet()) {
            reqBuilder.header(param.getKey(), HttpHelper.parameterToString(param.getValue()));
        }
        Map<String, String> defHeaders = client.getDefaultHeaderMap();
        for (Map.Entry<String, String> header : defHeaders.entrySet()) {
            if (!headerParams.containsKey(header.getKey())) {
                reqBuilder.header(header.getKey(), HttpHelper.parameterToString(header.getValue()));
            }
        }
    }

    /**
     * Build a form-encoding request body with the given form parameters.
     *
     * @param formParams Form parameters in the form of Map
     * @return RequestBody
     */
    public static RequestBody buildRequestBodyFormEncoding(Map<String, Object> formParams) {
        FormEncodingBuilder formBuilder = new FormEncodingBuilder();
        for (Map.Entry<String, Object> param : formParams.entrySet()) {
            formBuilder.add(param.getKey(), HttpHelper.parameterToString(param.getValue()));
        }
        return formBuilder.build();
    }

    /**
     * 处理给定的响应，当响应成功时返回反序列化的对象。
     *
     * @param <T>        Type
     * @param response   Response
     * @param returnType Return type
     * @return Type
     * @throws ApiException If the response has a unsuccessful status code or fail to deserialize
     *                      the response body
     */
    public static <T> T handleResponse(ApiClient client, Response response, Type returnType) throws ApiException {
        if (response.isSuccessful()) {
            if (returnType == null || response.code() == 204) {
                // 如果未定义returnType，则返回null，或者状态码为204(无内容)
                if (response.body() != null) {
                    try {
                        response.body().close();
                    } catch (IOException e) {
                        throw new ApiException(response.message(), e, response.code(), response.headers().toMultimap());
                    }
                }
                return null;
            } else {
                return HttpHelper.deserialize(client, response, returnType);
            }
        } else {
            throw new ApiException(response.message(), response.code(), response.headers().toMultimap(), null);
        }
    }

    /**
     * 从给定的Accept数组中选择Accept头的值:如果JSON存在于给定的数组中，则使用它;否则全部使用(连接成字符串)
     *
     * @param accepts 要从中选择的接受数组
     * @return 要使用的Accept报头。如果给定的数组为空，则返回null(而不是显式设置Accept标头)。
     */
    public static String selectHeaderAccept(String[] accepts) {
        if (accepts.length == 0) {
            return null;
        }
        for (String accept : accepts) {
            if (HttpHelper.isJsonMime(accept)) {
                return accept;
            }
        }
        return StringUtil.join(accepts, ",");
    }

    /**
     * 从给定数组中选择Content-Type头的值:如果JSON存在于给定数组中，则使用它;否则使用数组的第一个。
     *
     * @param contentTypes 要从中选择的内容类型数组
     * @return 要使用的Content-Type报头。如果给定的数组为空，或者匹配“any”，则使用JSON。
     */
    public static String selectHeaderContentType(String[] contentTypes) {
        if (contentTypes.length == 0 || contentTypes[0].equals("*/*")) {
            return "application/json";
        }
        for (String contentType : contentTypes) {
            if (HttpHelper.isJsonMime(contentType)) {
                return contentType;
            }
        }
        return contentTypes[0];
    }

    /**
     * 将指定的集合查询参数格式化为{@code Pair}对象列表。
     * <p>注意，每个返回的Pair对象的值都是百分比编码的。
     *
     * @param collectionFormat 参数的收集格式
     * @param name             参数名称
     * @param value            参数值
     * @return A list of {@code Pair} objects.
     */
    public static List<Pair> parameterToPairs(String collectionFormat, String name, Collection value) {
        List<Pair> params = new ArrayList<Pair>();

        // preconditions
        if (name == null || name.isEmpty() || value == null || value.isEmpty()) {
            return params;
        }

        // create the params based on the collection format
        if ("multi".equals(collectionFormat)) {
            for (Object item : value) {
                params.add(new Pair(name, SdkHelper.escapeString(HttpHelper.parameterToString(item))));
            }
            return params;
        }

        // collectionFormat is assumed to be "csv" by default
        String delimiter = ",";

        // escape all delimiters except commas, which are URI reserved
        // characters
        if ("ssv".equals(collectionFormat)) {
            delimiter = SdkHelper.escapeString(" ");
        } else if ("tsv".equals(collectionFormat)) {
            delimiter = SdkHelper.escapeString("\t");
        } else if ("pipes".equals(collectionFormat)) {
            delimiter = SdkHelper.escapeString("|");
        }

        StringBuilder sb = new StringBuilder();
        for (Object item : value) {
            sb.append(delimiter);
            sb.append(SdkHelper.escapeString(HttpHelper.parameterToString(item)));
        }

        params.add(new Pair(name, sb.substring(delimiter.length())));

        return params;
    }

    /**
     * 将参数对象格式化为字符串。
     *
     * @param param 参数对象
     * @return 返回序列化后字符串形式
     */
    public static String parameterToString(Object param) {
        if (param == null) {
            return "";
        } else if (param instanceof Date /*|| param instanceof OffsetDateTime || param instanceof LocalDate*/) {
            // Serialize to json string and remove the " enclosing characters
            String jsonStr = SerializerFactory.getSerializer().writeAsString(param);
            return jsonStr.substring(1, jsonStr.length() - 1);
        } else if (param instanceof Collection) {//TODO 这里没看太懂，为啥不使用JSON[]形式？
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
     * 构建一个文件上传请求体，其中可以包含文本字段和文件字段。
     * Build a multipart (file uploading) request body with the given form parameters, which could
     * contain text fields and file fields.
     *
     * @param formParams Form parameters in the form of Map
     * @return RequestBody
     */
    public static RequestBody buildRequestBodyMultipart(Map<String, Object> formParams) {
        MultipartBuilder mpBuilder = new MultipartBuilder().type(MultipartBuilder.FORM);
        for (Map.Entry<String, Object> param : formParams.entrySet()) {
            if (param.getValue() instanceof File) {
                File file = (File) param.getValue();
                Headers partHeaders = Headers.of("Content-Disposition", "form-data; name=\"" + param.getKey()
                                                                        + "\"; filename=\"" + file.getName() + "\"");
                MediaType mediaType = MediaType.parse(SdkHelper.guessContentTypeFromFile(file));
                mpBuilder.addPart(partHeaders, RequestBody.create(mediaType, file));
            } else {
                Headers partHeaders = Headers.of("Content-Disposition", "form-data; name=\"" + param.getKey() + "\"");
                mpBuilder.addPart(partHeaders, RequestBody.create(null, parameterToString(param.getValue())));
            }
        }
        return mpBuilder.build();
    }

    /**
     * 根据对象的类和请求的Content-Type将给定的Java对象序列化为请求体。
     *
     * @param obj         The Java object
     * @param contentType The request Content-Type
     * @return The serialized request body
     * @throws ApiException If fail to serialize the given object
     */
    public static RequestBody serialize(Object obj, String contentType) throws ApiException {
        if (obj instanceof byte[]) {
            // Binary (byte array) body parameter support.
            return RequestBody.create(MediaType.parse(contentType), (byte[]) obj);
        } else if (obj instanceof File) {
            // File body parameter support.
            return RequestBody.create(MediaType.parse(contentType), (File) obj);
        } else if (isJsonMime(contentType)) {
            String content = null;
            if (obj != null) {
                if (obj instanceof String) {
                    content = (String) obj;
                } else {
                    content = SerializerFactory.getSerializer().writeAsString(obj);
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
     * @param response Response对象的实例
     * @return Downloaded file
     * @throws ApiException 如果从响应中读取文件内容并写入磁盘失败
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
     * Sanitize filename by removing path. e.g. ../../sun.gif becomes sun.gif
     *
     * @param filename The filename to be sanitized
     * @return The sanitized filename
     */
    public static String sanitizeFilename(String filename) {
        return filename.replaceAll(".*[/\\\\]", "");
    }

    /**
     * Prepare file for download
     *
     * @param response An instance of the Response object
     * @return Prepared file for the download
     * @throws IOException If fail to prepare file for download
     */
    public static File prepareDownloadFile(ApiClient client, Response response) throws IOException {
        String filename = null;
        String contentDisposition = response.header("Content-Disposition");
        if (contentDisposition != null && !"".equals(contentDisposition)) {
            // Get filename from the Content-Disposition header.
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
            // File.createTempFile requires the prefix to be at least three characters long
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
     * @param <T>        Type
     * @param response   HTTP response
     * @param returnType The type of the Java object
     * @return The deserialized Java object
     * @throws ApiException If fail to deserialize response body, i.e. cannot read response body or
     *                      the Content-Type of the response is not supported.
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(ApiClient client, Response response, Type returnType) throws ApiException {
        if (response == null || returnType == null) {
            return null;
        }

        if ("byte[]".equals(returnType.toString())) {
            // Handle binary response (byte array).
            try {
                return (T) response.body().bytes();
            } catch (IOException e) {
                throw new ApiException(e);
            }
        } else if (returnType.equals(File.class)) {
            // Handle file downloading.
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
            // ensuring a default content type
            contentType = "application/json";
        }
        if (HttpHelper.isJsonMime(contentType)) {
            return SerializerFactory.getSerializer().decode(respBody, returnType);
        } else if (returnType.equals(String.class)) {
            // Expecting string, return the raw response body.
            return (T) respBody;
        } else {
            throw new ApiException("Content type \"" + contentType + "\" is not supported for type: " + returnType,
                response.code(), response.headers().toMultimap(), null);
        }
    }

    public static <T> ApiResponse<T> deserialize(ApiClient client, Response response, ResponseTypeHandler responseType)
                                                                                                                       throws ApiException {
        Type returnType = responseType.getElementType();
        if ("byte[]".equals(returnType.toString())) {
            // Handle binary response (byte array).
            try {
                byte[] data = response.body().bytes();
                return (ApiResponse<T>) ApiResponse.ok(data);
            } catch (IOException e) {
                throw new ApiException(e);
            }
        } else if (returnType.equals(File.class)) {
            // Handle file downloading.
            File file = downloadFileFromResponse(client, response);
            return (ApiResponse<T>) ApiResponse.ok(file);
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
            return ApiResponse.ok(null);
        }

        String contentType = response.headers().get("Content-Type");
        if (contentType == null) {
            // ensuring a default content type
            contentType = "application/json";
        }
        if (HttpHelper.isJsonMime(contentType)) {
            return SerializerFactory.getSerializer().decode(respBody, responseType.getType());
        } else if (returnType.equals(String.class)) {
            // Expecting string, return the raw response body.
            return (ApiResponse<T>) ApiResponse.ok(respBody);
        } else {
            throw new ApiException("Content type \"" + contentType + "\" is not supported for type: " + returnType,
                response.code(), response.headers().toMultimap());
        }
    }

    /**
     * 检查给定的 MIME is a JSON MIME. JSON MIME examples: application/json
     * application/json; charset=UTF8 APPLICATION/JSON application/vnd.company+json "* / *" is also
     * default to JSON
     *
     * @param mime MIME (Multipurpose Internet Mail Extensions)
     * @return True if the given MIME is JSON, false otherwise.
     */
    public static boolean isJsonMime(String mime) {
        String jsonMime = "(?i)^(application/json|[^;/ \t]+/[^;/ \t]+[+]json)[ \t]*(;.*)?$";
        return mime != null && (mime.matches(jsonMime) || mime.equals("*/*"));
    }

    /**
     * 将指定的查询参数格式化为包含单个{@code Pair}对象的列表。
     * <p>注意{@code value}不能是一个集合。
     *
     * @param name  The name of the parameter.
     * @param value The value of the parameter.
     * @return A list containing a single {@code Pair} object.
     */
    public static List<Pair> parameterToPair(String name, Object value) {
        List<Pair> params = new ArrayList<Pair>();

        // preconditions
        if (name == null || name.isEmpty() || value == null || value instanceof Collection)
            return params;

        params.add(new Pair(name, HttpHelper.parameterToString(value)));
        return params;
    }

    /**
     * 通过连接基本路径、给定子路径和查询参数来构建完整的URL。
     *
     * @param path                  子路径
     * @param queryParams           查询参数
     * @param collectionQueryParams 采集查询参数
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
                    String value = HttpHelper.parameterToString(param.getValue());
                    url.append(SdkHelper.escapeString(param.getName())).append("=")
                        .append(SdkHelper.escapeString(value));
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
                    String value = HttpHelper.parameterToString(param.getValue());
                    url.append(SdkHelper.escapeString(param.getName())).append("=").append(value);
                }
            }
        }
        return url.toString();
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
    public static Request buildRequest(ApiClient client, String path, String method, List<Pair> queryParams,
                                       List<Pair> collectionQueryParams, Object body, Map<String, String> headerParams,
                                       Map<String, Object> formParams,
                                       ProgressRequestBody.ProgressRequestListener progressRequestListener)
                                                                                                           throws ApiException {
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
        if (progressRequestListener != null && reqBody != null) {
            ProgressRequestBody progressRequestBody = new ProgressRequestBody(reqBody, progressRequestListener);
            request = reqBuilder.method(method, progressRequestBody).build();
        } else {
            request = reqBuilder.method(method, reqBody).build();
        }
        return request;
    }

}
