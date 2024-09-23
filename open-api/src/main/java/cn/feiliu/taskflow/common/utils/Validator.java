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
package cn.feiliu.taskflow.common.utils;

import cn.feiliu.taskflow.common.metadata.workflow.FlowTask;
import cn.feiliu.taskflow.common.metadata.workflow.WorkflowDefinition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

import static cn.feiliu.taskflow.common.utils.TaskflowUtils.f;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-06-30
 */
@Slf4j
public class Validator {
    // 定义电子邮件验证的正则表达式
    private static final String  EMAIL_PATTERN = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

    // 编译正则表达式
    private static final Pattern pattern       = Pattern.compile(EMAIL_PATTERN);

    /**
     * 验证email格式
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        if (StringUtils.isNotBlank(email)) {
            if (email.length() <= 128) {
                return pattern.matcher(email).matches();
            }
        }
        return false;
    }

    /**
     * 验证有效的工作流定义名称
     *
     * @param name
     * @return
     */
    public static boolean isValidWorkflowDefName(String name) {
        return name != null && name.matches("^[a-zA-Z0-9_-]{1,64}$");
    }

    public static boolean isValidWorkflowVersion(Integer version) {
        return version != null && version >= 1;
    }

    /**
     * 判断是否为表达式
     *
     * @param value
     * @return
     */
    public static boolean isExpression(Object value) {
        if (value != null && value instanceof String) {
            String str = ((String) value).trim();
            return str.startsWith("${") && str.endsWith("}");
        }
        return false;
    }

    /**
     * 验证是否有效的任务引用名称
     * @param taskRefName
     * @return
     */
    public static boolean isValidTaskRefName(String taskRefName) {
        String regex = "^[a-zA-Z0-9_]{1,64}$";
        return taskRefName != null && taskRefName.matches(regex);
    }

    public static void assertTaskRefName(String taskRefName) {
        if (!isValidTaskRefName(taskRefName)) {
            throw new IllegalArgumentException(f("The taskReferenceName: '%s' parameter is invalid", taskRefName));
        }
    }

    /**
     * 验证是否有效的任务名称
     * @param taskName
     * @return
     */
    public static boolean isValidTaskName(String taskName) {
        String regex = "^[a-zA-Z0-9_]{1,64}$";
        return taskName != null && taskName.matches(regex);
    }

    public static void assertTaskName(String taskRefName) {
        if (!isValidTaskName(taskRefName)) {
            throw new IllegalArgumentException(f("The taskName: '%s' parameter is invalid", taskRefName));
        }
    }

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
            if(!Validator.isValidTaskName(task.getName())){
                errors.add("Invalid task name: " + task.getName());
            }
            if(!Validator.isValidTaskRefName(task.getTaskReferenceName())){
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
        if (!isValidWorkflowDefName(workflowDef.getName())) {
            errors.add("Invalid workflow name");
        }
        if (!isValidWorkflowVersion(workflowDef.getVersion())) {
            errors.add("Invalid workflow version");
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
