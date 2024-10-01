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

import cn.feiliu.taskflow.common.constraints.EmailConstraint;
import cn.feiliu.taskflow.common.constraints.TaskNameConstraint;
import cn.feiliu.taskflow.common.constraints.TaskTimeoutConstraint;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.*;

@Data
@Valid
@TaskTimeoutConstraint
public class TaskDefinition {

    public enum TimeoutPolicy {
        RETRY, TIME_OUT_WF, ALERT_ONLY
    }

    public enum RetryLogic {
        FIXED, EXPONENTIAL_BACKOFF, LINEAR_BACKOFF
    }

    private static final int    ONE_HOUR                    = 60 * 60;

    /** Unique name identifying the task. The name is unique across */
    @TaskNameConstraint(message = "Illegal TaskDef name field")
    private String              name;
    //任务标题(应用在云平台展示)
    private String              title;
    /**
     * 任务描述
     */
    private String              description;
    @Min(value = 0, message = "TaskDef retryCount: {value} must be >= 0")
    private int                 retryCount                  = 3;                        // Default

    @NotNull
    private long                timeoutSeconds;
    /**
     * inputKeys Set of keys that the task accepts in the input map
     */
    private List<String>        inputKeys                   = new ArrayList<>();
    /**
     * the output keys for the task when executed
     */
    private List<String>        outputKeys                  = new ArrayList<>();

    private TimeoutPolicy       timeoutPolicy               = TimeoutPolicy.TIME_OUT_WF;

    private RetryLogic          retryLogic                  = RetryLogic.FIXED;

    private int                 retryDelaySeconds           = 60;
    /**
     * timeout for task to send response. After this timeout, the task will be re-queued
     */
    @Min(value = 1, message = "TaskDef responseTimeoutSeconds: ${validatedValue} should be minimum {value} second")
    private long                responseTimeoutSeconds      = ONE_HOUR;
    /**
     * concurrentExecLimit Limit of number of concurrent task that can be IN_PROGRESS at a given time. Seting the value to 0 removes the limit.
     */
    private Integer             concurrentExecLimit         = 0;

    private Map<String, Object> inputTemplate               = new HashMap<>();

    // This field is deprecated, do not use id 13.
    //	private Integer rateLimitPerSecond;
    /**
     * The max number of tasks that will be allowed to be executed per rateLimitFrequencyInSeconds. Setting the value to 0 removes the rate limit
     */
    private Integer             rateLimitPerFrequency       = 0;
    /**
     * The time window/bucket for which the rate limit needs to be applied. This will only have affect if {@link #getRateLimitPerFrequency()} is greater than zero
     */
    private Integer             rateLimitFrequencyInSeconds = 1;

    private String              isolationGroupId;

    private String              executionNameSpace;
    /**
     * email of the owner of this task definition
     */
    @EmailConstraint(message = "ownerEmail should be valid email address")
    @Email(message = "ownerEmail should be valid email address")
    private String              ownerEmail;
    /**
     * poll timeout of this task definition
     */
    @Min(value = 0, message = "TaskDef pollTimeoutSeconds: {value} must be >= 0")
    private Integer             pollTimeoutSeconds;
    /**
     * backoff rate of this task definition
     */
    @Min(value = 1, message = "Backoff scale factor. Applicable for LINEAR_BACKOFF")
    private Integer             backoffScaleFactor          = 1;

    public TaskDefinition() {
    }

    public TaskDefinition(String name) {
        this.name = name;
    }

    public TaskDefinition(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public TaskDefinition(String name, String description, int retryCount, long timeoutSeconds) {
        this.name = name;
        this.description = description;
        this.retryCount = retryCount;
        this.timeoutSeconds = timeoutSeconds;
    }

    public TaskDefinition(String name, String description, String ownerEmail, int retryCount, long timeoutSeconds,
                          long responseTimeoutSeconds) {
        this.name = name;
        this.description = description;
        this.ownerEmail = ownerEmail;
        this.retryCount = retryCount;
        this.timeoutSeconds = timeoutSeconds;
        this.responseTimeoutSeconds = responseTimeoutSeconds;
    }

    public void addInputKey(String name) {
        this.inputKeys.add(name);
    }

    public void addOutputKey(String name) {
        this.inputKeys.add(name);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String              name;
        private String              title;
        private String              description;
        private int                 retryCount                  = 3;
        private long                timeoutSeconds;
        private List<String>        inputKeys                   = new ArrayList<>();
        private List<String>        outputKeys                  = new ArrayList<>();
        private TimeoutPolicy       timeoutPolicy               = TimeoutPolicy.TIME_OUT_WF;
        private RetryLogic          retryLogic                  = RetryLogic.FIXED;
        private int                 retryDelaySeconds           = 60;
        private long                responseTimeoutSeconds      = ONE_HOUR;
        private Integer             concurrentExecLimit         = 0;
        private Map<String, Object> inputTemplate               = new HashMap<>();
        private Integer             rateLimitPerFrequency       = 0;
        private Integer             rateLimitFrequencyInSeconds = 1;
        private String              isolationGroupId;
        private String              executionNameSpace;
        private String              ownerEmail;
        private Integer             pollTimeoutSeconds;
        private Integer             backoffScaleFactor          = 1;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder retryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder timeoutSeconds(long timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        public Builder inputKeys(List<String> inputKeys) {
            this.inputKeys = inputKeys;
            return this;
        }

        public Builder outputKeys(List<String> outputKeys) {
            this.outputKeys = outputKeys;
            return this;
        }

        public Builder timeoutPolicy(TimeoutPolicy timeoutPolicy) {
            this.timeoutPolicy = timeoutPolicy;
            return this;
        }

        public Builder retryLogic(RetryLogic retryLogic) {
            this.retryLogic = retryLogic;
            return this;
        }

        public Builder retryDelaySeconds(int retryDelaySeconds) {
            this.retryDelaySeconds = retryDelaySeconds;
            return this;
        }

        public Builder responseTimeoutSeconds(long responseTimeoutSeconds) {
            this.responseTimeoutSeconds = responseTimeoutSeconds;
            return this;
        }

        public Builder concurrentExecLimit(Integer concurrentExecLimit) {
            this.concurrentExecLimit = concurrentExecLimit;
            return this;
        }

        public Builder inputTemplate(Map<String, Object> inputTemplate) {
            this.inputTemplate = inputTemplate;
            return this;
        }

        public Builder rateLimitPerFrequency(Integer rateLimitPerFrequency) {
            this.rateLimitPerFrequency = rateLimitPerFrequency;
            return this;
        }

        public Builder rateLimitFrequencyInSeconds(Integer rateLimitFrequencyInSeconds) {
            this.rateLimitFrequencyInSeconds = rateLimitFrequencyInSeconds;
            return this;
        }

        public Builder isolationGroupId(String isolationGroupId) {
            this.isolationGroupId = isolationGroupId;
            return this;
        }

        public Builder executionNameSpace(String executionNameSpace) {
            this.executionNameSpace = executionNameSpace;
            return this;
        }

        public Builder ownerEmail(String ownerEmail) {
            this.ownerEmail = ownerEmail;
            return this;
        }

        public Builder pollTimeoutSeconds(Integer pollTimeoutSeconds) {
            this.pollTimeoutSeconds = pollTimeoutSeconds;
            return this;
        }

        public Builder backoffScaleFactor(Integer backoffScaleFactor) {
            this.backoffScaleFactor = backoffScaleFactor;
            return this;
        }

        public TaskDefinition build() {
            TaskDefinition taskDefinition = new TaskDefinition();
            taskDefinition.name = this.name;
            taskDefinition.title = this.title;
            taskDefinition.description = this.description;
            taskDefinition.retryCount = this.retryCount;
            taskDefinition.timeoutSeconds = this.timeoutSeconds;
            taskDefinition.inputKeys = this.inputKeys;
            taskDefinition.outputKeys = this.outputKeys;
            taskDefinition.timeoutPolicy = this.timeoutPolicy;
            taskDefinition.retryLogic = this.retryLogic;
            taskDefinition.retryDelaySeconds = this.retryDelaySeconds;
            taskDefinition.responseTimeoutSeconds = this.responseTimeoutSeconds;
            taskDefinition.concurrentExecLimit = this.concurrentExecLimit;
            taskDefinition.inputTemplate = this.inputTemplate;
            taskDefinition.rateLimitPerFrequency = this.rateLimitPerFrequency;
            taskDefinition.rateLimitFrequencyInSeconds = this.rateLimitFrequencyInSeconds;
            taskDefinition.isolationGroupId = this.isolationGroupId;
            taskDefinition.executionNameSpace = this.executionNameSpace;
            taskDefinition.ownerEmail = this.ownerEmail;
            taskDefinition.pollTimeoutSeconds = this.pollTimeoutSeconds;
            taskDefinition.backoffScaleFactor = this.backoffScaleFactor;
            return taskDefinition;
        }
    }
}
