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

import cn.feiliu.taskflow.common.metadata.tasks.ExecutingTask;
import cn.feiliu.taskflow.common.metadata.tasks.TaskExecResult;
import cn.feiliu.taskflow.common.metadata.workflow.StartWorkflowRequest;
import cn.feiliu.taskflow.common.model.WorkflowRun;
import cn.feiliu.taskflow.proto.FlowModelPb;
import cn.feiliu.taskflow.proto.TaskModelPb;
import com.google.protobuf.Value;

import java.util.Map;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-06-29
 */
public class MapperFactory {
    static MapperFactory INSTANCE = new MapperFactory();

    public static MapperFactory getInstance() {
        return INSTANCE;
    }

    public TaskModelPb.TaskResult toProto(TaskExecResult from) {
        return TaskMapper.INSTANCE.toProto(from);
    }

    public TaskExecResult fromProto(TaskModelPb.TaskResult from) {
        return TaskMapper.INSTANCE.fromProto(from);
    }

    public TaskModelPb.Task toProto(ExecutingTask from) {
        return TaskMapper.INSTANCE.toProto(from);
    }

    public ExecutingTask fromProto(TaskModelPb.Task from) {
        return TaskMapper.INSTANCE.fromProto(from);
    }

    public Map<String, Object> fromProto(Map<String, Value> map) {
        return TaskMapper.INSTANCE.fromProto(map);
    }

    public FlowModelPb.StartWorkflowRequest toProto(StartWorkflowRequest from) {
        return WorkflowMapper.INSTANCE.toProto(from);
    }

    public StartWorkflowRequest fromProto(FlowModelPb.StartWorkflowRequest from) {
        return WorkflowMapper.INSTANCE.fromProto(from);
    }

    public Value toValue(Object val) {
        return ProtoValueMapper.toProto(val);
    }

    public static Object fromProto(Value any) {
        return ProtoValueMapper.fromProto(any);
    }

    public FlowModelPb.WorkflowRun toProto(WorkflowRun workflow) {
        return WorkflowMapper.INSTANCE.toProto(workflow);
    }

    public WorkflowRun fromProto(FlowModelPb.WorkflowRun workflowPb) {
        return WorkflowMapper.INSTANCE.fromProto(workflowPb);
    }
}
