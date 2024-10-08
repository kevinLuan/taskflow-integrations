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
package cn.feiliu.taskflow.client.automator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.Semaphore;

/**
 * A class wrapping a semaphore which holds the number of permits available for polling and
 * executing tasks.
 */
class PollingSemaphore {

    private static final Logger LOGGER = LoggerFactory.getLogger(PollingSemaphore.class);
    private final Semaphore     semaphore;

    PollingSemaphore(int numSlots) {
        LOGGER.debug("Polling semaphore initialized with {} permits", numSlots);
        semaphore = new Semaphore(numSlots);
    }

    /**
     * Signals if polling is allowed based on whether a permit can be acquired.
     *
     * @return {@code true} - if permit is acquired {@code false} - if permit could not be acquired
     */
    boolean canPoll() {
        boolean acquired = semaphore.tryAcquire();
        LOGGER.debug("Trying to acquire permit: {}", acquired);
        return acquired;
    }

    /**
     * Tries to acquire all available permits.
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
     * Signals that processing is complete and the permit can be released.
     */
    void complete() {
        LOGGER.debug("Completed execution; releasing permit");
        semaphore.release();
    }

    /**
     * Signals that processing is complete and the permits can be released.
     *
     * @param permits
     */
    void complete(int permits) {
        LOGGER.debug("Completed execution; releasing {} permits", permits);
        semaphore.release(permits);
    }

    /**
     * Gets the number of threads available for processing.
     *
     * @return number of available permits
     */
    int availableThreads() {
        int available = semaphore.availablePermits();
        LOGGER.debug("Number of available permits: {}", available);
        return available;
    }
}
