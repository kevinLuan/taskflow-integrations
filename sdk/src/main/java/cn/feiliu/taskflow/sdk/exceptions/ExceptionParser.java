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

import cn.feiliu.taskflow.exceptions.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * 异常解析类
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-08-20
 */
public class ExceptionParser {
    private static final Logger log = LoggerFactory.getLogger(ExceptionParser.class);

    public static boolean isConnectionException(Throwable e) {
        try {
            return isAnyMatches(e, (msg) -> msg.indexOf("Connection reset") != -1, ConnectException.class, NoRouteToHostException.class);
        } catch (Exception ex) {
            log.error("isConnectionException", ex);
            return false;
        }
    }

    public static boolean isTimeoutException(Throwable e) {
        try {
            return isAnyMatches(e, TimeoutException.class, SocketTimeoutException.class);
        } catch (Exception ex) {
            log.error("isTimeoutException", ex);
            return false;
        }
    }

    public static boolean isAnyMatches(Throwable e, Class<? extends Throwable>... clazz) {
        return isAnyMatches(e, null, clazz);
    }

    public static boolean isAnyMatches(Throwable e, Function<String, Boolean> customMatches,
                                       Class<? extends Throwable>... clazz) {
        try {
            if (e != null) {
                if (clazz.length == 0 && customMatches == null) {
                    return false;
                } else {
                    for (Class<? extends Throwable> aClass : clazz) {
                        if (aClass.isAssignableFrom(e.getClass())) {
                            return true;
                        }
                    }
                    if (e.getMessage() != null && customMatches != null && customMatches.apply(e.getMessage())) {
                        return true;
                    }
                    if (e.getCause() != null) {
                        return isAnyMatches(e.getCause(), customMatches, clazz);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("isAnyMatches", ex);
        }
        return false;
    }

    public static String getStacks(StackTraceElement[] stacks) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < stacks.length; i++) {
            StackTraceElement stack = stacks[i];
            if (isMatches(stack)) {
                builder.append("\tat " + stacks[i]).append("\n");
            }
        }
        return builder.toString();
    }

    private static boolean isMatches(StackTraceElement element) {
        String className = element.getClassName();
        if (className.startsWith("cn.feiliu.")) {
            return true;
        }
        return false;
    }

    /**
     * 尝试解析异常类型
     *
     * @param e
     * @return
     */
    public static Optional<String> tryParserType(Throwable e) {
        if ((e instanceof ApiException && ((ApiException) e).getStatusCode() == 404)) {
            return Optional.of("NotFound error");
        } else if (ExceptionParser.isConnectionException(e)) {
            return Optional.of("Connection error");
        } else if (ExceptionParser.isTimeoutException(e)) {
            return Optional.of("Timeout error");
        }
        return Optional.empty();
    }
}
