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
package cn.feiliu.taskflow.executor.task;

/**
 * 表示任务执行过程中出现的不可重试错误的运行时异常。
 * 如果抛出此异常，任务将以 FAILED_WITH_TERMINAL_ERROR 状态失败，
 * 并且不会触发重试机制。
 */
public class NonRetryableException extends RuntimeException {

    /**
     * 使用指定的错误消息构造一个新的 NonRetryableException
     *
     * @param message 详细描述该异常的消息
     */
    public NonRetryableException(String message) {
        super(message);
    }

    /**
     * 使用指定的错误消息和原因构造一个新的 NonRetryableException
     *
     * @param message 详细描述该异常的消息
     * @param cause 导致此异常的原因
     */
    public NonRetryableException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因构造一个新的 NonRetryableException
     *
     * @param cause 导致此异常的原因
     */
    public NonRetryableException(Throwable cause) {
        super(cause);
    }
}
