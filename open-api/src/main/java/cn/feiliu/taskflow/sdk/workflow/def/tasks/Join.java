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
package cn.feiliu.taskflow.sdk.workflow.def.tasks;

import cn.feiliu.taskflow.common.metadata.tasks.TaskType;
import cn.feiliu.taskflow.common.metadata.workflow.FlowTask;

import java.util.Arrays;

public class Join extends Task<Join> {

    private String[] joinOn;

    /**
     * @param taskReferenceName
     * @param joinOn List of task reference names to join on
     */
    public Join(String taskReferenceName, String... joinOn) {
        super(taskReferenceName, TaskType.JOIN);
        this.joinOn = joinOn;
    }

    Join(FlowTask workflowTask) {
        super(workflowTask);
        this.joinOn = workflowTask.getJoinOn().toArray(new String[0]);
    }

    @Override
    protected void updateWorkflowTask(FlowTask workflowTask) {
        workflowTask.setJoinOn(Arrays.asList(joinOn));
    }

    public String[] getJoinOn() {
        return joinOn;
    }
}
