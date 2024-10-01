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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static cn.feiliu.taskflow.common.utils.TaskflowUtils.f;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-09-17
 */
public class JsValidator {
    private static final Set<String> JS_KEYWORDS = new HashSet<>(Arrays.asList("abstract", "arguments", "await",
                                                     "boolean", "break", "byte", "case", "catch", "char", "class",
                                                     "const", "continue", "debugger", "default", "delete", "do",
                                                     "double", "else", "enum", "eval", "export", "extends", "false",
                                                     "final", "finally", "float", "for", "function", "goto", "if",
                                                     "implements", "import", "in", "instanceof", "int", "interface",
                                                     "let", "long", "native", "new", "null", "package", "private",
                                                     "protected", "public", "return", "short", "static", "super",
                                                     "switch", "synchronized", "this", "throw", "throws", "transient",
                                                     "true", "try", "typeof", "var", "void", "volatile", "while",
                                                     "with", "yield"));

    /**
     * 验证给定的字符串是否为有效的JavaScript变量名称。
     *
     * @param name 要验证的变量名称
     * @return 如果是有效的变量名称则返回true，否则返回false
     */
    public static boolean isValidJsVariableName(String name) {
        // Check if the name is empty or starts with a digit
        if (name == null || name.isEmpty() || Character.isDigit(name.charAt(0))) {
            return false;
        }

        // Check if the name is a JavaScript keyword
        if (JS_KEYWORDS.contains(name)) {
            return false;
        }

        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            // Check each character to ensure it's valid
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '$') {
                return false;
            }
        }
        return true;
    }

    /**
     * 断言任务引用名称应该符合JS变量命名要求
     */
    public static void assertVariableName(String taskReferenceName) {
        if (!isValidJsVariableName(taskReferenceName)) {
            throw new IllegalArgumentException(f("The taskReferenceName: '%s' parameter is invalid", taskReferenceName));
        }
    }
}
