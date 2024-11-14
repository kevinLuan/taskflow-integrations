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
package cn.feiliu.taskflow.common.metadata.workflow;

import cn.feiliu.taskflow.common.constraints.WorkflowNameConstraint;
import cn.feiliu.taskflow.common.enums.IdempotencyStrategy;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
public class StartWorkflowRequest {
    @WorkflowNameConstraint(message = "Invalid workflow name")
    private String              name;

    private Integer             version             = 1;

    private String              correlationId;

    private Map<String, Object> input               = new HashMap<>();

    private Map<String, String> taskToDomain        = new HashMap<>();

    private String              externalInputPayloadStoragePath;

    @Min(value = 0, message = "priority: ${validatedValue} should be minimum {value}")
    @Max(value = 99, message = "priority: ${validatedValue} should be maximum {value}")
    private Integer             priority            = 0;

    /**
     * Idempotency Key is a user generated key to avoid conflicts with other workflows.
     * Idempotency data is retained for the life of the workflow executions.
     */
    private String              idempotencyKey;
    /**
     * 幂等控制策略
     */
    private IdempotencyStrategy idempotencyStrategy = IdempotencyStrategy.FAIL;

    public static StartWorkflowRequest of(String name, Integer version) {
        Objects.requireNonNull(name, "workflow name cannot be null");
        if (version < 1) {
            throw new IllegalArgumentException("invalid workflow version");
        }
        StartWorkflowRequest request = new StartWorkflowRequest();
        request.setName(name);
        request.setVersion(version);
        return request;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String              name;
        private Integer             version             = 1;
        private String              correlationId;
        private Map<String, Object> input               = new HashMap<>();
        private Map<String, String> taskToDomain        = new HashMap<>();
        private String              externalInputPayloadStoragePath;
        private Integer             priority            = 0;
        private String              idempotencyKey;
        private IdempotencyStrategy idempotencyStrategy = IdempotencyStrategy.FAIL;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(Integer version) {
            this.version = version;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder input(Map<String, Object> input) {
            this.input = input;
            return this;
        }

        public Builder taskToDomain(Map<String, String> taskToDomain) {
            this.taskToDomain = taskToDomain;
            return this;
        }

        public Builder externalInputPayloadStoragePath(String externalInputPayloadStoragePath) {
            this.externalInputPayloadStoragePath = externalInputPayloadStoragePath;
            return this;
        }

        public Builder priority(Integer priority) {
            this.priority = priority;
            return this;
        }

        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public Builder idempotencyStrategy(IdempotencyStrategy idempotencyStrategy) {
            this.idempotencyStrategy = idempotencyStrategy;
            return this;
        }

        public StartWorkflowRequest build() {
            StartWorkflowRequest request = new StartWorkflowRequest();
            request.setName(this.name);
            request.setVersion(this.version);
            request.setCorrelationId(this.correlationId);
            request.setInput(this.input);
            request.setTaskToDomain(this.taskToDomain);
            request.setExternalInputPayloadStoragePath(this.externalInputPayloadStoragePath);
            request.setPriority(this.priority);
            request.setIdempotencyKey(this.idempotencyKey);
            request.setIdempotencyStrategy(this.idempotencyStrategy);
            return request;
        }
    }

    @Override
    public String toString() {
        return "StartWorkflowRequest{" + "name='" + name + '\'' + ", version=" + version + ", correlationId='"
               + correlationId + '\'' + ", input=" + input + ", taskToDomain=" + taskToDomain
               + ", externalInputPayloadStoragePath='" + externalInputPayloadStoragePath + '\'' + ", priority="
               + priority + ", idempotencyKey='" + idempotencyKey + '\'' + ", idempotencyStrategy="
               + idempotencyStrategy + '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;
        StartWorkflowRequest request = (StartWorkflowRequest) object;
        return Objects.equals(name, request.name) && Objects.equals(version, request.version)
               && Objects.equals(correlationId, request.correlationId) && Objects.equals(input, request.input)
               && Objects.equals(taskToDomain, request.taskToDomain)
               && Objects.equals(externalInputPayloadStoragePath, request.externalInputPayloadStoragePath)
               && Objects.equals(priority, request.priority) && Objects.equals(idempotencyKey, request.idempotencyKey)
               && idempotencyStrategy == request.idempotencyStrategy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, correlationId, input, taskToDomain, externalInputPayloadStoragePath,
            priority, idempotencyKey, idempotencyStrategy);
    }
}
