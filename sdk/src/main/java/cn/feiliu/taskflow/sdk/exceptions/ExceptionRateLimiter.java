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
package cn.feiliu.taskflow.sdk.exceptions;

import cn.feiliu.taskflow.open.exceptions.ApiException;
import cn.feiliu.taskflow.open.exceptions.NotFoundException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;

import java.net.ConnectException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-08-20
 */
public class ExceptionRateLimiter {
    /**
     * 100 times in 10 minutes
     * For the first 100 errors, just print them as is...
     */
    private final int                                           maxLimit = 100;
    private final int                                           maxSize  = 128;
    /*Reset after 10 minutes errors*/
    private final Cache<Class<? extends Throwable>, AtomicLong> cache    = CacheBuilder.newBuilder()
                                                                             .maximumSize(maxSize)
                                                                             .expireAfterWrite(10, TimeUnit.MINUTES)
                                                                             .build();

    /**
     * 根据限速控制来决定是否应该记录日志
     *
     * @param t
     * @return
     */
    @SneakyThrows
    public void shouldRecordLog(Throwable t, Consumer<ExceptionSummary> consumer) {
        AtomicLong cnt;
        if ((t instanceof ApiException && ((ApiException) t).getStatusCode() == 404)) {
            cnt = cache.get(NotFoundException.class, () -> new AtomicLong(0));
        } else if (ExceptionParser.isConnectionException(t)) {
            cnt = cache.get(ConnectException.class, () -> new AtomicLong(0));
        } else if (ExceptionParser.isTimeoutException(t)) {
            cnt = cache.get(TimeoutException.class, () -> new AtomicLong(0));
        } else {
            Class<? extends Throwable> clazz = t.getCause() == null ? t.getClass() : t.getCause().getClass();
            cnt = cache.get(clazz, () -> new AtomicLong(0));
        }
        long errorCount = cnt.incrementAndGet();
        if (errorCount <= maxLimit) {
            consumer.accept(ExceptionSummary.of(errorCount, t));
        }
    }
}
