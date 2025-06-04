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
package cn.feiliu.taskflow.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识一个简单的工作者任务。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface WorkerTask {
    /**
     * 任务的名称
     *
     * @return 任务名称
     */
    String value();

    /**
     * Number of threads used to execute tasks
     *
     * @return thread count
     */
    int threadCount() default 1;

    /**
     * Task polling interval (in milliseconds)
     *
     * @return polling interval
     */
    int pollingInterval() default 1000;

    /**
     * Task execution domain
     *
     * @return domain name
     */
    String domain() default "";
}
