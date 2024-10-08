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
package cn.feiliu.taskflow.sdk.workflow.def.tasks;

import cn.feiliu.taskflow.expression.PathExpression;
import com.google.common.base.Strings;
import cn.feiliu.taskflow.common.metadata.tasks.TaskType;
import cn.feiliu.taskflow.common.metadata.workflow.FlowTask;

/** Wait task */
public class Dynamic extends Task<Dynamic> {

    public static final String TASK_NAME_INPUT_PARAM = "taskToExecute";

    public Dynamic(String taskReferenceName, PathExpression expression) {
        this(taskReferenceName, expression.getExpression());
    }

    public Dynamic(String taskReferenceName, String dynamicTaskNameValue) {
        super(taskReferenceName, TaskType.DYNAMIC);
        if (Strings.isNullOrEmpty(dynamicTaskNameValue)) {
            throw new IllegalArgumentException("Null/Empty dynamicTaskNameValue");
        }
        super.input(TASK_NAME_INPUT_PARAM, dynamicTaskNameValue);
    }

    Dynamic(FlowTask workflowTask) {
        super(workflowTask);
    }

    @Override
    public void updateWorkflowTask(FlowTask task) {
        task.setDynamicTaskNameParam(TASK_NAME_INPUT_PARAM);
    }
}
