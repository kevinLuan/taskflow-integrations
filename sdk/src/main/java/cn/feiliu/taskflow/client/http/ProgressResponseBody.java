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

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.ResponseBody;
import okio.*;

import java.io.IOException;

/**
 * 带进度监听的响应体包装类
 */
public class ProgressResponseBody extends ResponseBody {

    /**
     * 进度监听接口
     */
    public interface ProgressListener {
        /**
         * 响应进度回调方法
         * @param bytesRead 已读取字节数
         * @param contentLength 总字节数
         * @param done 是否完成
         */
        void update(long bytesRead, long contentLength, boolean done);
    }

    // 原始响应体
    private final ResponseBody     responseBody;
    // 进度监听器
    private final ProgressListener progressListener;
    // 缓冲数据源
    private BufferedSource         bufferedSource;

    /**
     * 构造方法
     * @param responseBody 原始响应体
     * @param progressListener 进度监听器
     */
    public ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener) {
        this.responseBody = responseBody;
        this.progressListener = progressListener;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() throws IOException {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    /**
     * 创建带进度监听的数据源
     * @param source 原始数据源
     * @return 包装后的数据源
     */
    private Source source(Source source) {
        return new ForwardingSource(source) {
            // 已读取的总字节数
            long totalBytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                // read() 返回实际读取的字节数，如果数据源已耗尽则返回 -1
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                return bytesRead;
            }
        };
    }
}
