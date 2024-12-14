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
package cn.feiliu.taskflow.common.metadata.tasks;

import cn.feiliu.taskflow.common.enums.TaskStatus;
import cn.feiliu.taskflow.common.metadata.workflow.FlowTask;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Data
public class ExecutingTask {

    /*Type of the task*/
    private String              taskType;

    private TaskStatus          status;

    private Map<String, Object> inputData          = new HashMap<>();
    private String              referenceTaskName;
    private int                 retryCount;
    private int                 seq;
    /*业务关联ID*/
    private String              correlationId;

    private int                 pollCount;

    private String              taskDefName;

    /**
     * Time when the task was scheduled
     */
    private long                scheduledTime;

    /**
     * Time when the task was first polled
     */
    private long                startTime;

    /**
     * Time when the task completed executing
     */
    private long                endTime;

    /**
     * Time when the task was last updated
     */
    private long                updateTime;

    private int                 startDelayInSeconds;

    private String              retriedTaskId;
    /*如果任务失败后重试，则为True*/
    private boolean             retried;
    /**
     * True if the task has completed its lifecycle within taskflow (from start to completion to being updated in the datastore)
     */
    private boolean             executed;

    private boolean             callbackFromWorker = true;
    /*the timeout for task to send response. After this timeout, the task will be re-queued*/
    private long                responseTimeoutSeconds;

    private String              workflowInstanceId;
    /*the name of the workflow*/
    private String              workflowType;

    private String              taskId;
    /*未完成的原因*/
    private String              reasonForIncompletion;

    private long                callbackAfterSeconds;

    private String              workerId;

    private Map<String, Object> outputData         = new HashMap<>();
    /*任务定义*/
    private FlowTask            workflowTask;
    private String              domain;
    //    private Any                 inputMessage;
    //
    //    private Any                 outputMessage;

    // id 31 is reserved
    private int                 rateLimitPerFrequency;

    private int                 rateLimitFrequencyInSeconds;
    /*任务输入负载的外部存储路径*/
    private String              externalInputPayloadStoragePath;
    /*任务输出负载的外部存储路径*/
    private String              externalOutputPayloadStoragePath;
    /*在工作流上定义的优先级*/
    private int                 workflowPriority;

    private String              executionNameSpace;

    private String              isolationGroupId;

    private int                 iteration;

    private String              subWorkflowId;

    /**
     * Use to note that a sub workflow associated with SUB_WORKFLOW task has an action performed on
     * it directly.
     */
    private boolean             subworkflowChanged;

    public ExecutingTask() {
    }

    /**
     * @return the queueWaitTime
     */
    public long getQueueWaitTime() {
        if (this.startTime > 0 && this.scheduledTime > 0) {
            if (this.updateTime > 0 && getCallbackAfterSeconds() > 0) {
                long waitTime = System.currentTimeMillis() - (this.updateTime + (getCallbackAfterSeconds() * 1000));
                return waitTime > 0 ? waitTime : 0;
            } else {
                return this.startTime - this.scheduledTime;
            }
        }
        return 0L;
    }

    /**
     * @return Name of the task definition
     */
    public String getTaskDefName() {
        if (taskDefName == null || "".equals(taskDefName)) {
            taskDefName = taskType;
        }
        return taskDefName;
    }

    /**
     * @param reasonForIncompletion the reasonForIncompletion to set
     */
    public void setReasonForIncompletion(String reasonForIncompletion) {
        this.reasonForIncompletion = StringUtils.substring(reasonForIncompletion, 0, 500);
    }

    public boolean isLoopOverTask() {
        return iteration > 0;
    }

    public String getSubWorkflowId() {
        // For backwards compatibility
        if (StringUtils.isNotBlank(subWorkflowId)) {
            return subWorkflowId;
        } else {
            return this.getOutputData() != null && this.getOutputData().get("subWorkflowId") != null ? (String) this
                .getOutputData().get("subWorkflowId") : this.getInputData() != null ? (String) this.getInputData().get(
                "subWorkflowId") : null;
        }
    }

    public void setSubWorkflowId(String subWorkflowId) {
        this.subWorkflowId = subWorkflowId;
        // For backwards compatibility
        if (this.getOutputData() != null && this.getOutputData().containsKey("subWorkflowId")) {
            this.getOutputData().put("subWorkflowId", subWorkflowId);
        }
    }

    public ExecutingTask copy() {
        ExecutingTask copy = new ExecutingTask();
        copy.setCallbackAfterSeconds(callbackAfterSeconds);
        copy.setCallbackFromWorker(callbackFromWorker);
        copy.setCorrelationId(correlationId);
        copy.setInputData(inputData);
        copy.setOutputData(outputData);
        copy.setReferenceTaskName(referenceTaskName);
        copy.setStartDelayInSeconds(startDelayInSeconds);
        copy.setTaskDefName(taskDefName);
        copy.setTaskType(taskType);
        copy.setWorkflowInstanceId(workflowInstanceId);
        copy.setWorkflowType(workflowType);
        copy.setResponseTimeoutSeconds(responseTimeoutSeconds);
        copy.setStatus(status);
        copy.setRetryCount(retryCount);
        copy.setPollCount(pollCount);
        copy.setTaskId(taskId);
        copy.setWorkflowTask(workflowTask);
        copy.setDomain(domain);
        //        copy.setInputMessage(inputMessage);
        //        copy.setOutputMessage(outputMessage);
        copy.setRateLimitPerFrequency(rateLimitPerFrequency);
        copy.setRateLimitFrequencyInSeconds(rateLimitFrequencyInSeconds);
        copy.setExternalInputPayloadStoragePath(externalInputPayloadStoragePath);
        copy.setExternalOutputPayloadStoragePath(externalOutputPayloadStoragePath);
        copy.setWorkflowPriority(workflowPriority);
        copy.setIteration(iteration);
        copy.setExecutionNameSpace(executionNameSpace);
        copy.setIsolationGroupId(isolationGroupId);
        copy.setSubWorkflowId(getSubWorkflowId());
        copy.setSubworkflowChanged(subworkflowChanged);

        return copy;
    }

    /**
     * @return a deep copy of the task instance To be used inside copy Workflow method to provide a
     * valid deep copied object. Note: This does not copy the following fields:
     * <ul>
     *   <li>retried
     *   <li>updateTime
     *   <li>retriedTaskId
     * </ul>
     */
    public ExecutingTask deepCopy() {
        ExecutingTask deepCopy = copy();
        deepCopy.setStartTime(startTime);
        deepCopy.setScheduledTime(scheduledTime);
        deepCopy.setEndTime(endTime);
        deepCopy.setWorkerId(workerId);
        deepCopy.setReasonForIncompletion(reasonForIncompletion);
        deepCopy.setSeq(seq);
        return deepCopy;
    }
}
