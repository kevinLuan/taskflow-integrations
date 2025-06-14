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
package cn.feiliu.taskflow.automator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.Semaphore;

/**
 * 一个包装信号量的类,用于持有可用于轮询和执行任务的许可数量
 */
class PollingSemaphore {

    private static final Logger LOGGER = LoggerFactory.getLogger(PollingSemaphore.class);
    private final Semaphore     semaphore;

    PollingSemaphore(int numSlots) {
        LOGGER.debug("Polling semaphore initialized with {} permits", numSlots);
        semaphore = new Semaphore(numSlots);
    }

    /**
     * 根据是否可以获取许可来判断是否允许轮询
     *
     * @return {@code true} - 如果获取到许可 {@code false} - 如果无法获取许可
     */
    boolean canPoll() {
        boolean acquired = semaphore.tryAcquire();
        LOGGER.debug("Trying to acquire permit: {}", acquired);
        return acquired;
    }

    /**
     * 尝试获取所有可用的许可
     *
     * @return
     */
    Optional<Integer> tryAcquireAvailablePermits() {
        int available = semaphore.availablePermits();
        if (available > 0 && semaphore.tryAcquire(available)) {
            return Optional.of(available);
        }
        return Optional.empty();
    }

    /**
     * 表示处理已完成,可以释放许可
     */
    void complete() {
        LOGGER.debug("Completed execution; releasing permit");
        semaphore.release();
    }

    /**
     * 表示处理已完成,可以释放指定数量的许可
     *
     * @param permits
     */
    void complete(int permits) {
        LOGGER.debug("Completed execution; releasing {} permits", permits);
        semaphore.release(permits);
    }

    /**
     * 获取可用于处理的线程数量
     *
     * @return 可用许可的数量
     */
    int availableThreads() {
        int available = semaphore.availablePermits();
        LOGGER.debug("Number of available permits: {}", available);
        return available;
    }
}
