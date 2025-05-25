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
import com.squareup.okhttp.RequestBody;
import okio.*;

import java.io.IOException;

/**
 * 带进度监听的请求体包装类
 */
public class ProgressRequestBody extends RequestBody {

    /**
     * 进度监听接口
     */
    public interface ProgressRequestListener {
        /**
         * 请求进度回调方法
         * @param bytesWritten 已写入字节数
         * @param contentLength 总字节数
         * @param done 是否完成
         */
        void onRequestProgress(long bytesWritten, long contentLength, boolean done);
    }

    // 原始请求体
    private final RequestBody             requestBody;

    // 进度监听器
    private final ProgressRequestListener progressListener;

    /**
     * 构造方法
     * @param requestBody 原始请求体
     * @param progressListener 进度监听器
     */
    public ProgressRequestBody(RequestBody requestBody, ProgressRequestListener progressListener) {
        this.requestBody = requestBody;
        this.progressListener = progressListener;
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        // 包装BufferedSink以支持进度监听
        BufferedSink bufferedSink = Okio.buffer(sink(sink));
        requestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    /**
     * 创建带进度监听的Sink
     * @param sink 原始Sink
     * @return 包装后的Sink
     */
    private Sink sink(Sink sink) {
        return new ForwardingSink(sink) {

            // 已写入字节数
            long bytesWritten  = 0L;
            // 总字节数
            long contentLength = 0L;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0) {
                    contentLength = contentLength();
                }

                bytesWritten += byteCount;
                // 回调进度
                progressListener.onRequestProgress(bytesWritten, contentLength, bytesWritten == contentLength);
            }
        };
    }
}
