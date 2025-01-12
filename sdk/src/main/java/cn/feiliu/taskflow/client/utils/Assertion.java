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
package cn.feiliu.taskflow.client.utils;

import cn.feiliu.taskflow.exceptions.ApiException;

import java.util.Collection;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-06-15
 */
public class Assertion {
    public static void assertNotNull(Object value, String name) {
        if (value == null) {
            throw new ApiException("Missing the required parameter '" + name + "'");
        }
    }

    public static void assertNotEmpty(Collection collection, String name) {
        if (collection == null || collection.isEmpty()) {
            throw new ApiException("Missing the required parameter '" + name + "'");
        }
    }

    public static void assertArgument(Long value, Long min, Long max) {
        if (value > max || value < min)
            throw new IllegalArgumentException("Invalid parameter. Parameter range: 100 ~ 2000");
    }

    public static void assertArgument(Integer value, Integer min, Integer max) {
        if (value > max || value < min)
            throw new IllegalArgumentException("Invalid parameter. Parameter range: 100 ~ 2000");
    }
}
