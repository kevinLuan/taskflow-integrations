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
package cn.feiliu.taskflow.client;

import cn.feiliu.taskflow.utils.FieldUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author kevin.luan
 * @since 2025-06-04
 */
public class FieldUtilsTests {
    @Test
    public void testFieldUtils() {
        String[] fields = FieldUtils.getJavaFieldNames(Person.class);
        Assert.assertEquals(3, fields.length);
    }

    public static class Person {
        private String  name;  // 私有字段
        private int     age;
        private boolean active;

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public boolean isActive() {
            return active;
        }
    }
}
