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

/** Wait task */
public class Wait extends Task<Wait> {

    /**
     * Wait until and external signal completes the task. The external signal can be either an API
     * call (POST /api/task) to update the task status or an event coming from a supported external
     * queue integration like SQS, Kafka, NATS, AMQP etc.
     *
     * @param taskReferenceName
     */
    public Wait(String taskReferenceName) {
        super(taskReferenceName, TaskType.WAIT);
    }

    Wait(FlowTask workflowTask) {
        super(workflowTask);
    }
}
