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
package cn.feiliu.taskflow.sdk.exceptions;

import lombok.Data;

import java.util.Optional;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-08-20
 */
@Data(staticConstructor = "of")
public class ExceptionSummary {
    /*周期时间内第x次出现该类异常*/
    private final long      errorCount;
    /*原始异常栈*/
    private final Throwable throwable;

    /**
     * 尝试解析异常类型
     *
     * @return
     */
    public Optional<String> tryParserType() {
        return ExceptionParser.tryParserType(throwable);
    }

    public boolean isFirst() {
        return errorCount == 1;
    }
}
