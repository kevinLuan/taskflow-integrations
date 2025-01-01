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
package cn.feiliu.taskflow.common.metadata.tasks;

public enum TaskType {
    SIMPLE, //
    DYNAMIC, //
    FORK_JOIN, //
    FORK_JOIN_DYNAMIC, //
    DECISION, //
    SWITCH, //
    JOIN, //
    DO_WHILE, //
    FOR_EACH, //
    FORK_FOR_EACH, //
    SUB_WORKFLOW, //
    START_WORKFLOW, //
    EVENT, //
    WAIT, //
    WAIT_FOR_WEBHOOK,//
    USER_DEFINED, //
    HTTP, //
    LAMBDA, //
    INLINE, //
    EXCLUSIVE_JOIN, //
    TERMINATE, //
    KAFKA_PUBLISH, //
    JSON_JQ_TRANSFORM, //
    SET_VARIABLE;

    /**
     * Converts a task type string to {@link TaskType}. For an unknown string, the value is
     * defaulted to {@link TaskType#USER_DEFINED}.
     *
     * <p>NOTE: Use {@link Enum#valueOf(Class, String)} if the default of USER_DEFINED is not
     * necessary.
     *
     * @param taskType The task type string.
     * @return The {@link TaskType} enum.
     */
    public static TaskType of(String taskType) {
        try {
            return TaskType.valueOf(taskType);
        } catch (IllegalArgumentException iae) {
            return TaskType.USER_DEFINED;
        }
    }

    public boolean isForEach() {
        return this == FOR_EACH;
    }

    public boolean isForkForEach() {
        return this == FORK_FOR_EACH;
    }

    public boolean isSimple() {
        return this == SIMPLE;
    }

    public boolean isSubWorkflow() {
        return this == SUB_WORKFLOW;
    }
}
