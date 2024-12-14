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

import cn.feiliu.taskflow.common.metadata.workflow.FlowTask;
import cn.feiliu.taskflow.common.metadata.workflow.WorkflowDefinition;

import java.util.*;

import static cn.feiliu.common.api.utils.CommonUtils.f;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-11-20
 */
public class SdkValidator {

    /**
     * 验证任务引用名称全局唯一性
     *
     * @param workflowDef
     * @return
     */
    private static List<String> verifyTaskRefNameUniqueness(WorkflowDefinition workflowDef) {
        Objects.requireNonNull(workflowDef, "workflowDef cannot be null");
        List<String> errors = new ArrayList<>();
        // check if taskReferenceNames are unique across tasks or not
        Set<String> taskReferences = new HashSet<>();
        workflowDef.collectTasks().forEach((task) -> {
            if (!Validator.isValidTaskName(task.getName())) {
                errors.add("Invalid task name: " + task.getName());
            }
            if (!Validator.isValidTaskRefName(task.getTaskReferenceName())) {
                errors.add("Invalid taskReferenceName: " + task.getTaskReferenceName());
            }
            if (!taskReferences.add(task.getTaskReferenceName())) {
                errors.add(f("taskReferenceName: %s should be unique across tasks for a given workflowDefinition: %s",
                        task.getTaskReferenceName(), workflowDef.getName()));
            }
        });
        return errors;
    }

    public static List<String> verifyWorkflowDef(WorkflowDefinition workflowDef) {
        List<String> errors = new ArrayList<>();
        if (!Validator.isValidWorkflowName(workflowDef.getName())) {
            errors.add("Invalid workflow name");
        }
        if (workflowDef.getTasks().isEmpty()) {
            errors.add("tasks cannot be empty");
        }
        errors.addAll(verifyTaskRefNameUniqueness(workflowDef));
        errors.addAll(verifyTaskInputParameters(workflowDef));
        return errors;
    }

    private static List<String> verifyTaskInputParameters(WorkflowDefinition workflow) {
        List<String> errors = new LinkedList<>();
        // check inputParameters points to valid taskDef
        for (FlowTask task : workflow.getTasks()) {
            if (!Validator.isValidTaskName(task.getName())) {
                errors.add("Invalid task name: " + task.getName());
            }
            errors.addAll(ConstraintParamUtil.validateInputParam(task.getInputParameters(), task.getName(), workflow));
        }
        return errors;
    }
}
