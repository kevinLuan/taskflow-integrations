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
package cn.feiliu.taskflow.common.utils;

import cn.feiliu.taskflow.sdk.worker.Worker;
import cn.feiliu.taskflow.sdk.workflow.executor.task.AnnotatedWorker;
import com.google.common.util.concurrent.Uninterruptibles;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-09-04
 */
@Slf4j
public class TaskflowUtils {

    /**
     * dump异常堆栈
     *
     * @param t
     * @return
     */
    public static String dumpStackTrace(Throwable t) {
        StringBuilder stackTrace = new StringBuilder();
        for (StackTraceElement stackTraceElement : t.getStackTrace()) {
            String className = stackTraceElement.getClassName();
            if (className.startsWith("jdk.") || className.startsWith(AnnotatedWorker.class.getName())) {
                break;
            }
            stackTrace.append(stackTraceElement);
            stackTrace.append("\n");
        }
        return stackTrace.toString();
    }

    /**
     * dump完整的执行栈
     *
     * @param t
     * @return
     */
    public static String dumpFullStackTrace(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        t.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    /***
     *  string format
     * @param format
     * @param args
     * @return
     */
    public static String f(String format, Object... args) {
        return String.format(format, args);
    }

    /**
     * 等待futures执行完成，累计等待超时时间为 timeoutMs
     *
     * @param futures
     * @param timeoutMs 超时时间(单位：毫秒)
     */
    public static void blockedWait(List<Future<?>> futures, int timeoutMs) {
        long startTime = System.currentTimeMillis();
        for (Future<?> future : futures) {
            try {
                long remainingTime = timeoutMs - (System.currentTimeMillis() - startTime);
                if (remainingTime > 0) {
                    future.get(remainingTime, TimeUnit.MILLISECONDS);
                } else {
                    future.cancel(true);
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Failed to wait for futures", e);
            }
        }
    }

    /**
     * 自动重试处理
     *
     * @param task   执行的任务
     * @param count  若执行出错，累计最大执行次数
     * @param opName 操作名称
     */
    public static void retryOperation(Runnable task, int count, String opName) {
        int index = 0;
        do {
            try {
                task.run();
                return;
            } catch (Exception e) {
                log.error("Error executing " + opName, e);
                index++;
                Uninterruptibles.sleepUninterruptibly(500L * (count + 1), TimeUnit.MILLISECONDS);
            }
        } while (index < count);
        throw new RuntimeException("Exhausted retries performing " + opName);
    }

    /**
     * 获取拉取任务超时时间，最小100ms，最大为1000ms
     *
     * @param worker
     * @return
     */
    public static int getReasonableTimeout(Worker worker) {
        int timeout = worker.getBatchPollTimeoutInMS();
        if (timeout <= 100) {
            return 100;
        } else if (timeout > 1000) {
            return 1000;
        } else {
            return timeout;
        }
    }
}
