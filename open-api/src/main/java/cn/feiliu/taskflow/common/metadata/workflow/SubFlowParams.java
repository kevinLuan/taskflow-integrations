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
package cn.feiliu.taskflow.common.metadata.workflow;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class SubFlowParams {
    @NotNull(message = "SubWorkflowParams name cannot be null")
    @NotEmpty(message = "SubWorkflowParams name cannot be empty")
    private String              name;

    private Integer             version;

    private Map<String, String> taskToDomain;

    private WorkflowDefinition  workflowDefinition;

    public String getName() {
        if (workflowDefinition != null) {
            return workflowDefinition.getName();
        } else {
            return name;
        }
    }

    public Integer getVersion() {
        if (workflowDefinition != null) {
            return workflowDefinition.getVersion();
        } else {
            return version;
        }
    }

}
