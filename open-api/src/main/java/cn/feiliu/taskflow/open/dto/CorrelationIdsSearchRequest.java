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
package cn.feiliu.taskflow.open.dto;

import lombok.*;

import java.util.List;
import java.util.Objects;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-05-26
 */
@Getter
@Setter
@ToString
public class CorrelationIdsSearchRequest {
    /**
     * List of correlation ids to search
     */
    private List<String> correlationIds;
    /**
     * workflowNames  List of workflow names to search
     */
    private List<String> workflowNames;

    public CorrelationIdsSearchRequest() {
    }

    public CorrelationIdsSearchRequest(List<String> correlationIds, List<String> workflowNames) {
        this.correlationIds = correlationIds;
        this.workflowNames = workflowNames;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;
        CorrelationIdsSearchRequest that = (CorrelationIdsSearchRequest) object;
        return Objects.equals(correlationIds, that.correlationIds) && Objects.equals(workflowNames, that.workflowNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(correlationIds, workflowNames);
    }
}
