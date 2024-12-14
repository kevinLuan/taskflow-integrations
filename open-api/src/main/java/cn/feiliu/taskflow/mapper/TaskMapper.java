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
package cn.feiliu.taskflow.mapper;

import cn.feiliu.taskflow.common.enums.TaskStatus;
import cn.feiliu.taskflow.common.enums.TaskUpdateStatus;
import cn.feiliu.taskflow.common.metadata.tasks.ExecutingTask;
import cn.feiliu.taskflow.common.metadata.tasks.TaskExecResult;
import cn.feiliu.taskflow.proto.TaskModelPb;
import com.google.protobuf.Value;

import java.util.HashMap;
import java.util.Map;

class TaskMapper {

    public static final TaskMapper INSTANCE = new TaskMapper();

    public static TaskMapper getInstance() {
        return INSTANCE;
    }

    public TaskModelPb.TaskResult toProto(TaskExecResult from) {
        TaskModelPb.TaskResult.Builder to = TaskModelPb.TaskResult.newBuilder();
        if (from.getWorkflowInstanceId() != null) {
            to.setWorkflowInstanceId(from.getWorkflowInstanceId());
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
        if (from.getStatus() != null) {
            to.setStatus(TaskModelPb.TaskResult.Status.valueOf(from.getStatus().name()));
        }
        for (Map.Entry<String, Object> pair : from.getOutputData().entrySet()) {
            to.putOutputData(pair.getKey(), ValueMapper.getInstance().toProto(pair.getValue()));
        }
        return to.build();
    }

    public TaskExecResult fromProto(TaskModelPb.TaskResult from) {
        TaskExecResult to = new TaskExecResult();
        to.setWorkflowInstanceId(from.getWorkflowInstanceId());
        to.setTaskId(from.getTaskId());
        to.setReasonForIncompletion(from.getReasonForIncompletion());
        to.setCallbackAfterSeconds(from.getCallbackAfterSeconds());
        to.setWorkerId(from.getWorkerId());
        to.setStatus(TaskUpdateStatus.valueOf(from.getStatus().name()));
        Map<String, Object> outputDataMap = new HashMap<String, Object>();
        for (Map.Entry<String, Value> pair : from.getOutputDataMap().entrySet()) {
            outputDataMap.put(pair.getKey(), ValueMapper.getInstance().fromProto(pair.getValue()));
        }
        to.setOutputData(outputDataMap);
        return to;
    }

    public TaskModelPb.Task toProto(ExecutingTask from) {
        TaskModelPb.Task.Builder to = TaskModelPb.Task.newBuilder();
        if (from.getTaskType() != null) {
            to.setTaskType(from.getTaskType());
        }
        if (from.getStatus() != null) {
            to.setStatus(TaskModelPb.Task.Status.valueOf(from.getStatus().name()));
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

    public ExecutingTask fromProto(TaskModelPb.Task from) {
        ExecutingTask to = new ExecutingTask();
        to.setTaskType(from.getTaskType());
        to.setStatus(TaskStatus.valueOf(from.getStatus().name()));
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

    public Map<String, Object> fromProto(Map<String, Value> map) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Value> entry : map.entrySet()) {
            result.put(entry.getKey(), ValueMapper.getInstance().fromProto(entry.getValue()));
        }
        return result;
    }
}
