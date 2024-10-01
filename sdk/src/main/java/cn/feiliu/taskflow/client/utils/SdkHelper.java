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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-06-05
 */
public class SdkHelper {
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
     * 从给定文件中猜测内容类型头(默认为“applicationoctet-stream”)。
     *
     * @param file The given file
     * @return The guessed Content-Type
     */
    public static String guessContentTypeFromFile(File file) {
        String contentType = URLConnection.guessContentTypeFromName(file.getName());
        if (contentType == null) {
            return "application/octet-stream";
        } else {
            return contentType;
        }
    }
}
