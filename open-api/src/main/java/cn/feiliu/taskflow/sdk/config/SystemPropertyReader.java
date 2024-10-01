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
package cn.feiliu.taskflow.sdk.config;

import java.util.Optional;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-09-01
 */
class SystemPropertyReader {
    private final String prefix;
    private final String propName;
    private final String workerName;

    public SystemPropertyReader(String prefix, String propName, String workerName) {
        this.prefix = prefix;
        this.propName = propName;
        this.workerName = workerName;
    }

    private String getGlobalKey() {
        return prefix + "_" + propName;
    }

    private String getWorkerKey() {
        return prefix + "_" + workerName + "_" + propName;
    }

    public Optional<Integer> getInteger() {
        Optional<String> optional = getString();
        return optional.map((res) -> Integer.parseInt(res));
    }

    public Optional<String> getString() {
        String result = System.getenv(getWorkerKey());
        if (result == null) {
            result = System.getenv(getGlobalKey());
        }
        return Optional.ofNullable(result);
    }

    public Optional<Boolean> getBoolean() {
        Optional<String> result = getString();
        return result.map((res) -> Boolean.parseBoolean(res));
    }
}
