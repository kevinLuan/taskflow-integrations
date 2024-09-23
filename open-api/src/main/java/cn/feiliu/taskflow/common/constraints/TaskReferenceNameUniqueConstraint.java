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
package cn.feiliu.taskflow.common.constraints;

import cn.feiliu.taskflow.common.metadata.workflow.WorkflowDefinition;
import cn.feiliu.taskflow.common.utils.Validator;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.List;

import static java.lang.annotation.ElementType.TYPE;

/**
 * This constraint class validates following things.
 *
 * <ul>
 *   <li>1. WorkflowDef is valid or not
 *   <li>2. Make sure taskReferenceName used across different tasks are unique
 *   <li>3. Verify inputParameters points to correct tasks or not
 * </ul>
 */
@Documented
@Constraint(validatedBy = TaskReferenceNameUniqueConstraint.TaskReferenceNameUniqueValidator.class)
@Target({ TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface TaskReferenceNameUniqueConstraint {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class TaskReferenceNameUniqueValidator implements
                                          ConstraintValidator<TaskReferenceNameUniqueConstraint, WorkflowDefinition> {

        @Override
        public void initialize(TaskReferenceNameUniqueConstraint constraintAnnotation) {
        }

        @Override
        public boolean isValid(WorkflowDefinition workflowDef, ConstraintValidatorContext context) {
            context.disableDefaultConstraintViolation();
            List<String> errors = Validator.verifyWorkflowDef(workflowDef);
            errors.forEach((message) -> {
                context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            });
            return errors.isEmpty();
        }
    }
}
