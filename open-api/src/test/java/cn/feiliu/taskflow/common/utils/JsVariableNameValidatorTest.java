/*
 * Copyright 2024 taskflow, Inc.
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

import org.junit.Test;

import static cn.feiliu.taskflow.common.utils.JsValidator.isValidJsVariableName;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-09-17
 */
public class JsVariableNameValidatorTest {
    @Test
    public void test() {
        assertTrue(isValidJsVariableName("_myVar"));
        assertFalse(isValidJsVariableName("2ndVar"));
        assertFalse(isValidJsVariableName("for"));
        assertTrue(isValidJsVariableName("my_Var"));
        assertTrue(isValidJsVariableName("$var"));
    }
}
