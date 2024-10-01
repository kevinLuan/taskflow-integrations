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
package cn.feiliu.taskflow.expression;

import java.util.List;
import java.util.Objects;

/**
 * 任务输出参数表达式
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-09-08
 */
public class OutputExpression extends AbstractPathExpression {
    private final List<String> parts;

    public OutputExpression(List<String> parts) {
        Objects.requireNonNull(parts, "parts is null");
        parts.add("output");
        this.parts = parts;
    }

    public OutputExpression get(String field) {
        parts.add(field);
        return this;
    }

    public OutputExpression getResult() {
        return get("result");
    }

    public OutputExpression path(String... paths) {
        for (String path : paths) {
            parts.add(path);
        }
        return this;
    }

    @Override
    public String getExpression() {
        return super.getExpression(parts);
    }

    public OutputExpression index(int i) {
        int index = parts.size() - 1;
        parts.set(index, parts.get(index) + "[" + i + "]");
        return this;
    }
}
