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

import cn.feiliu.taskflow.common.utils.Validator;
import org.apache.commons.lang3.StringUtils;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;

/**
 * This constraint class validates that owner email is non-empty, but only if configuration says
 * owner email is mandatory.
 */
@Documented
@Constraint(validatedBy = EmailConstraint.WorkflowTaskValidValidator.class)
@Target({ TYPE, FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface EmailConstraint {

    String message() default "{cn.feiliu.taskflow.common.constraints.EmailConstraint.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class WorkflowTaskValidValidator implements ConstraintValidator<EmailConstraint, String> {
        @Override
        public void initialize(EmailConstraint constraintAnnotation) {
        }

        @Override
        public boolean isValid(String ownerEmail, ConstraintValidatorContext context) {
            if (StringUtils.isNotBlank(ownerEmail)) {
                return Validator.isEmail(ownerEmail);
            }
            return true;
        }
    }
}
