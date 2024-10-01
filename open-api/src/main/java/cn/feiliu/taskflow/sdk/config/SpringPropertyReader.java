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

import org.springframework.core.env.Environment;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-09-01
 */
public class SpringPropertyReader implements PropertyReader {
    private final static AtomicReference<Environment> envRef = new AtomicReference<>();
    private final String                              prefix;
    private final String                              propName;
    private final String                              workerName;

    public SpringPropertyReader(String prefix, String propName, String workerName) {
        this.prefix = prefix;
        this.propName = propName;
        this.workerName = workerName;
    }

    public static void init(Environment environment) {
        Objects.requireNonNull(environment, "Environment is required");
        envRef.set(environment);
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
        Integer result = envRef.get().getProperty(getWorkerKey(), Integer.class);
        if (result == null) {
            return envRef.get().getProperty(getGlobalKey(), Integer.class, defaultValue);
        } else {
            return result;
        }
    }

    /**
     * @param defaultValue Default Value
     * @return Returns the value as String. If not value is set (either global or worker specific),
     * then returns the default value.
     */
    public String getString(String defaultValue) {
        String result = envRef.get().getProperty(getWorkerKey());
        if (result == null) {
            return envRef.get().getProperty(getGlobalKey(), defaultValue);
        } else {
            return result;
        }
    }

    /**
     * @param defaultValue Default Value
     * @return Returns the value as Boolean. If not value is set (either global or worker specific),
     * then returns the default value.
     */
    public Boolean getBoolean(Boolean defaultValue) {
        Boolean result = envRef.get().getProperty(getWorkerKey(), Boolean.class);
        if (result == null) {
            return envRef.get().getProperty(getGlobalKey(), Boolean.class, defaultValue);
        } else {
            return result;
        }
    }
}
