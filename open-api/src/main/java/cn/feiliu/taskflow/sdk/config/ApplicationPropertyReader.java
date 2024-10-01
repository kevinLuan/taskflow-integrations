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
class ApplicationPropertyReader implements PropertyReader {
    private final String               prefix;
    private final String               propName;
    private final String               workerName;
    private final SystemPropertyReader envWrapper;

    public ApplicationPropertyReader(String prefix, String propName, String workerName) {
        this.prefix = prefix;
        this.propName = propName;
        this.workerName = workerName;
        this.envWrapper = new SystemPropertyReader(prefix, propName, workerName);
    }

    private String getGlobalKey() {
        return prefix + "." + propName;
    }

    private String getWorkerKey() {
        return prefix + "." + workerName + "." + propName;
    }

    /**
     * @param defaultValue Default Value
     * @return Returns the value as integer. If not value is set (either global or worker specific),
     * then returns the default value.
     */
    public Integer getInteger(int defaultValue) {
        return Integer.parseInt(getString(Integer.toString(defaultValue)));
    }

    /**
     * @param defaultValue Default Value
     * @return Returns the value as String. If not value is set (either global or worker specific),
     * then returns the default value.
     */
    public String getString(String defaultValue) {
        String result = System.getProperty(getWorkerKey());
        if (result != null) {
            return result;
        }
        Optional<String> optional = envWrapper.getString();
        if (optional.isPresent()) {
            return optional.get();
        } else {
            return System.getProperty(getGlobalKey(), defaultValue);
        }
    }

    /**
     * @param defaultValue Default Value
     * @return Returns the value as Boolean. If not value is set (either global or worker specific),
     * then returns the default value.
     */
    public Boolean getBoolean(Boolean defaultValue) {
        String result = getString(defaultValue == null ? null : defaultValue.toString());
        if (result != null) {
            return Boolean.parseBoolean(result);
        } else {
            return defaultValue;
        }
    }
}
