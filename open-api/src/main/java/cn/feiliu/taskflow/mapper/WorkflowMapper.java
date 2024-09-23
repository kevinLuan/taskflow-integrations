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
package cn.feiliu.taskflow.mapper;

import cn.feiliu.taskflow.common.metadata.tasks.ExecutingTask;
import cn.feiliu.taskflow.common.metadata.workflow.StartWorkflowRequest;
import cn.feiliu.taskflow.common.model.WorkflowRun;
import cn.feiliu.taskflow.common.run.ExecutingWorkflow;
import cn.feiliu.taskflow.proto.FlowModelPb;
import cn.feiliu.taskflow.proto.TaskModelPb;
import com.google.protobuf.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-06-29
 */
class WorkflowMapper extends BaseMapper {
    public static final WorkflowMapper INSTANCE = new WorkflowMapper();

    public FlowModelPb.StartWorkflowRequest toProto(StartWorkflowRequest from) {
        FlowModelPb.StartWorkflowRequest.Builder to = FlowModelPb.StartWorkflowRequest.newBuilder();
        if (from.getName() != null) {
            to.setName(from.getName());
        }
        if (from.getVersion() != null) {
            to.setVersion(from.getVersion());
        }
        if (from.getCorrelationId() != null) {
            to.setCorrelationId(from.getCorrelationId());
        }
        for (Map.Entry<String, Object> pair : from.getInput().entrySet()) {
            to.putInput(pair.getKey(), ValueMapper.getInstance().toProto(pair.getValue()));
        }
        to.putAllTaskToDomain(from.getTaskToDomain());

        if (from.getExternalInputPayloadStoragePath() != null) {
            to.setExternalInputPayloadStoragePath(from.getExternalInputPayloadStoragePath());
        }
        if (from.getPriority() != null) {
            to.setPriority(from.getPriority());
        }
        if (from.getIdempotencyKey() != null) {
            to.setIdempotencyKey(from.getIdempotencyKey());
        }
        to.setIdempotencyStrategy(toProto(from.getIdempotencyStrategy()));
        return to.build();
    }

    public FlowModelPb.StartWorkflowRequest.IdempotencyStrategy toProto(StartWorkflowRequest.IdempotencyStrategy from) {
        switch (from) {
            case NONE:
                return FlowModelPb.StartWorkflowRequest.IdempotencyStrategy.NONE;
            case FAIL:
                return FlowModelPb.StartWorkflowRequest.IdempotencyStrategy.FAIL;
            case RETURN_EXISTING:
                return FlowModelPb.StartWorkflowRequest.IdempotencyStrategy.RETURN_EXISTING;
            default:
                throw new IllegalArgumentException("Unexpected enum constant: " + from);
        }
    }

    public StartWorkflowRequest fromProto(FlowModelPb.StartWorkflowRequest from) {
        StartWorkflowRequest to = new StartWorkflowRequest();
        to.setName(from.getName());
        to.setVersion(from.getVersion());
        to.setCorrelationId(from.getCorrelationId());
        Map<String, Object> inputMap = new HashMap<String, Object>();
        for (Map.Entry<String, Value> pair : from.getInputMap().entrySet()) {
            inputMap.put(pair.getKey(), ValueMapper.getInstance().fromProto(pair.getValue()));
        }
        to.setInput(inputMap);
        to.setTaskToDomain(from.getTaskToDomainMap());

        to.setExternalInputPayloadStoragePath(from.getExternalInputPayloadStoragePath());
        to.setPriority(from.getPriority());
        //        to.setCreatedBy(from.getCreatedBy());
        return to;
    }

    public FlowModelPb.WorkflowRun toProto(WorkflowRun workflow) {
        FlowModelPb.WorkflowRun.Builder builder =
                FlowModelPb.WorkflowRun.newBuilder()
                        .setStatus(FlowModelPb.WorkflowRun.Status.valueOf(workflow.getStatus().name()))
                        .setInput(JsonMapMapper.INSTANCE.convertToJsonMap(workflow.getInput()))
                        .setOutput(JsonMapMapper.INSTANCE.convertToJsonMap(workflow.getOutput()))
                        .setVariables(JsonMapMapper.INSTANCE.convertToJsonMap(workflow.getVariables()))
                        .setWorkflowId(workflow.getWorkflowId())
                        .setCreateTime(workflow.getCreateTime())
                        .setUpdatedTime(workflow.getUpdateTime());

        if (workflow.getCreatedBy() != null) {
            builder.setCreatedBy(workflow.getCreatedBy());
        }
        if (workflow.getCorrelationId() != null) {
            builder.setCorrelationId(workflow.getCorrelationId());
        }
        String requestId = (String) workflow.getInput().get("_x-request-id");
        if (requestId != null) {
            builder.setRequestId(requestId);
        }
        workflow.getTasks().forEach(
                task -> {
                    builder.addTasks(toProto(task));
                });
        return builder.build();
    }

    public TaskModelPb.Task toProto(ExecutingTask from) {
        TaskModelPb.Task.Builder to = TaskModelPb.Task.newBuilder();
        if (from.getTaskType() != null) {
            to.setTaskType(from.getTaskType());
        }
        if (from.getStatus() != null) {
            to.setStatus(toProto(from.getStatus()));
        }
        for (Map.Entry<String, Object> pair : from.getInputData().entrySet()) {
            to.putInputData(pair.getKey(), ValueMapper.getInstance().toProto(pair.getValue()));
        }
        if (from.getReferenceTaskName() != null) {
            to.setReferenceTaskName(from.getReferenceTaskName());
        }
        to.setRetryCount(from.getRetryCount());
        to.setSeq(from.getSeq());
        if (from.getCorrelationId() != null) {
            to.setCorrelationId(from.getCorrelationId());
        }
        to.setPollCount(from.getPollCount());
        if (from.getTaskDefName() != null) {
            to.setTaskDefName(from.getTaskDefName());
        }
        to.setScheduledTime(from.getScheduledTime());
        to.setStartTime(from.getStartTime());
        to.setEndTime(from.getEndTime());
        to.setUpdateTime(from.getUpdateTime());
        to.setStartDelayInSeconds(from.getStartDelayInSeconds());
        if (from.getRetriedTaskId() != null) {
            to.setRetriedTaskId(from.getRetriedTaskId());
        }
        to.setRetried(from.isRetried());
        to.setExecuted(from.isExecuted());
        to.setCallbackFromWorker(from.isCallbackFromWorker());
        to.setResponseTimeoutSeconds(from.getResponseTimeoutSeconds());
        if (from.getWorkflowInstanceId() != null) {
            to.setWorkflowInstanceId(from.getWorkflowInstanceId());
        }
        if (from.getWorkflowType() != null) {
            to.setWorkflowType(from.getWorkflowType());
        }
        if (from.getTaskId() != null) {
            to.setTaskId(from.getTaskId());
        }
        if (from.getReasonForIncompletion() != null) {
            to.setReasonForIncompletion(from.getReasonForIncompletion());
        }
        to.setCallbackAfterSeconds(from.getCallbackAfterSeconds());
        if (from.getWorkerId() != null) {
            to.setWorkerId(from.getWorkerId());
        }
        for (Map.Entry<String, Object> pair : from.getOutputData().entrySet()) {
            to.putOutputData(pair.getKey(), ValueMapper.getInstance().toProto(pair.getValue()));
        }

        if (from.getDomain() != null) {
            to.setDomain(from.getDomain());
        }

        to.setRateLimitPerFrequency(from.getRateLimitPerFrequency());
        to.setRateLimitFrequencyInSeconds(from.getRateLimitFrequencyInSeconds());
        if (from.getExternalInputPayloadStoragePath() != null) {
            to.setExternalInputPayloadStoragePath(from.getExternalInputPayloadStoragePath());
        }
        if (from.getExternalOutputPayloadStoragePath() != null) {
            to.setExternalOutputPayloadStoragePath(from.getExternalOutputPayloadStoragePath());
        }
        to.setWorkflowPriority(from.getWorkflowPriority());
        if (from.getExecutionNameSpace() != null) {
            to.setExecutionNameSpace(from.getExecutionNameSpace());
        }
        if (from.getIsolationGroupId() != null) {
            to.setIsolationGroupId(from.getIsolationGroupId());
        }
        to.setIteration(from.getIteration());
        if (from.getSubWorkflowId() != null) {
            to.setSubWorkflowId(from.getSubWorkflowId());
        }
        to.setSubworkflowChanged(from.isSubworkflowChanged());
        return to.build();
    }

    public TaskModelPb.Task.Status toProto(ExecutingTask.Status from) {
        TaskModelPb.Task.Status to;
        switch (from) {
            case IN_PROGRESS:
                to = TaskModelPb.Task.Status.IN_PROGRESS;
                break;
            case CANCELED:
                to = TaskModelPb.Task.Status.CANCELED;
                break;
            case FAILED:
                to = TaskModelPb.Task.Status.FAILED;
                break;
            case FAILED_WITH_TERMINAL_ERROR:
                to = TaskModelPb.Task.Status.FAILED_WITH_TERMINAL_ERROR;
                break;
            case COMPLETED:
                to = TaskModelPb.Task.Status.COMPLETED;
                break;
            case COMPLETED_WITH_ERRORS:
                to = TaskModelPb.Task.Status.COMPLETED_WITH_ERRORS;
                break;
            case SCHEDULED:
                to = TaskModelPb.Task.Status.SCHEDULED;
                break;
            case TIMED_OUT:
                to = TaskModelPb.Task.Status.TIMED_OUT;
                break;
            case SKIPPED:
                to = TaskModelPb.Task.Status.SKIPPED;
                break;
            default:
                throw new IllegalArgumentException("Unexpected enum constant: " + from);
        }
        return to;
    }

    public WorkflowRun fromProto(FlowModelPb.WorkflowRun workflowPb) {

        WorkflowRun workflowRun = new WorkflowRun();
        workflowRun.setWorkflowId(workflowPb.getWorkflowId());
        workflowRun.setRequestId(workflowPb.getRequestId());
        workflowRun.setCorrelationId(workflowPb.getCorrelationId());
        workflowRun.setStatus(ExecutingWorkflow.WorkflowStatus.valueOf(workflowPb.getStatus().toString()));

        workflowRun.setInput(JsonMapMapper.INSTANCE.convertToJavaMap(workflowPb.getInput()));
        workflowRun.setOutput(JsonMapMapper.INSTANCE.convertToJavaMap(workflowPb.getOutput()));
        workflowRun.setVariables(JsonMapMapper.INSTANCE.convertToJavaMap(workflowPb.getVariables()));
        workflowRun.setTasks(new ArrayList<>());

        for (TaskModelPb.Task task : workflowPb.getTasksList()) {
            workflowRun.getTasks().add(fromProto(task));
        }

        workflowRun.setCreatedBy(workflowRun.getCreatedBy());
        workflowRun.setCreateTime(workflowPb.getCreateTime());
        workflowRun.setUpdateTime(workflowPb.getUpdatedTime());

        return workflowRun;
    }

    public ExecutingTask fromProto(TaskModelPb.Task from) {
        ExecutingTask to = new ExecutingTask();
        to.setTaskType(from.getTaskType());
        to.setStatus(fromProto(from.getStatus()));
        Map<String, Object> inputDataMap = new HashMap<String, Object>();
        for (Map.Entry<String, Value> pair : from.getInputDataMap().entrySet()) {
            inputDataMap.put(pair.getKey(), ValueMapper.getInstance().fromProto(pair.getValue()));
        }
        to.setInputData(inputDataMap);
        to.setReferenceTaskName(from.getReferenceTaskName());
        to.setRetryCount(from.getRetryCount());
        to.setSeq(from.getSeq());
        to.setCorrelationId(from.getCorrelationId());
        to.setPollCount(from.getPollCount());
        to.setTaskDefName(from.getTaskDefName());
        to.setScheduledTime(from.getScheduledTime());
        to.setStartTime(from.getStartTime());
        to.setEndTime(from.getEndTime());
        to.setUpdateTime(from.getUpdateTime());
        to.setStartDelayInSeconds(from.getStartDelayInSeconds());
        to.setRetriedTaskId(from.getRetriedTaskId());
        to.setRetried(from.getRetried());
        to.setExecuted(from.getExecuted());
        to.setCallbackFromWorker(from.getCallbackFromWorker());
        to.setResponseTimeoutSeconds(from.getResponseTimeoutSeconds());
        to.setWorkflowInstanceId(from.getWorkflowInstanceId());
        to.setWorkflowType(from.getWorkflowType());
        to.setTaskId(from.getTaskId());
        to.setReasonForIncompletion(from.getReasonForIncompletion());
        to.setCallbackAfterSeconds(from.getCallbackAfterSeconds());
        to.setWorkerId(from.getWorkerId());
        Map<String, Object> outputDataMap = new HashMap<String, Object>();
        for (Map.Entry<String, Value> pair : from.getOutputDataMap().entrySet()) {
            outputDataMap.put(pair.getKey(), ValueMapper.getInstance().fromProto(pair.getValue()));
        }
        to.setOutputData(outputDataMap);
        to.setDomain(from.getDomain());
        to.setRateLimitPerFrequency(from.getRateLimitPerFrequency());
        to.setRateLimitFrequencyInSeconds(from.getRateLimitFrequencyInSeconds());
        to.setExternalInputPayloadStoragePath(from.getExternalInputPayloadStoragePath());
        to.setExternalOutputPayloadStoragePath(from.getExternalOutputPayloadStoragePath());
        to.setWorkflowPriority(from.getWorkflowPriority());
        to.setExecutionNameSpace(from.getExecutionNameSpace());
        to.setIsolationGroupId(from.getIsolationGroupId());
        to.setIteration(from.getIteration());
        to.setSubWorkflowId(from.getSubWorkflowId());
        to.setSubworkflowChanged(from.getSubworkflowChanged());
        return to;
    }

    public ExecutingTask.Status fromProto(TaskModelPb.Task.Status from) {
        ExecutingTask.Status to;
        switch (from) {
            case IN_PROGRESS:
                to = ExecutingTask.Status.IN_PROGRESS;
                break;
            case CANCELED:
                to = ExecutingTask.Status.CANCELED;
                break;
            case FAILED:
                to = ExecutingTask.Status.FAILED;
                break;
            case FAILED_WITH_TERMINAL_ERROR:
                to = ExecutingTask.Status.FAILED_WITH_TERMINAL_ERROR;
                break;
            case COMPLETED:
                to = ExecutingTask.Status.COMPLETED;
                break;
            case COMPLETED_WITH_ERRORS:
                to = ExecutingTask.Status.COMPLETED_WITH_ERRORS;
                break;
            case SCHEDULED:
                to = ExecutingTask.Status.SCHEDULED;
                break;
            case TIMED_OUT:
                to = ExecutingTask.Status.TIMED_OUT;
                break;
            case SKIPPED:
                to = ExecutingTask.Status.SKIPPED;
                break;
            default:
                throw new IllegalArgumentException("Unexpected enum constant: " + from);
        }
        return to;
    }

}
