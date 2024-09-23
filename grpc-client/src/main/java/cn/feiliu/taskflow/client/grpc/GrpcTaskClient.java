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
package cn.feiliu.taskflow.client.grpc;

import cn.feiliu.taskflow.common.metadata.tasks.ExecutingTask;
import cn.feiliu.taskflow.common.metadata.tasks.TaskExecResult;
import cn.feiliu.taskflow.grpc.TaskflowServiceGrpc;
import cn.feiliu.taskflow.grpc.TaskflowStreamServiceGrpc;
import cn.feiliu.taskflow.mapper.MapperFactory;
import cn.feiliu.taskflow.proto.TaskModelPb;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

public class GrpcTaskClient {
    private final TaskflowServiceGrpc.TaskflowServiceBlockingStub             stub;

    private final TaskflowStreamServiceGrpc.TaskflowStreamServiceBlockingStub streamStub;

    public GrpcTaskClient(ChannelManager channelManager) {
        this.streamStub = channelManager.newTaskflowStreamServiceBlockingStub();
        this.stub = channelManager.newTaskflowServiceBlockingStub();
    }

    public List<ExecutingTask> batchPoll(String taskType, String workerId, String domain, int count,
                                         int timeoutInMillisecond) {
        TaskModelPb.BatchPollRequest.Builder requestBuilder = TaskModelPb.BatchPollRequest.newBuilder().setCount(count)
                .setTaskType(taskType).setTimeout(timeoutInMillisecond);
        if (workerId != null) {
            requestBuilder.setWorkerId(workerId);
        }
        if (domain != null) {
            requestBuilder = requestBuilder.setDomain(domain);
        }
        TaskModelPb.BatchPollRequest request = requestBuilder.build();
        Iterator<TaskModelPb.Task> tasks = this.streamStub.batchPoll(request);
        return Lists.newArrayList(Iterators.transform(tasks, MapperFactory.getInstance()::fromProto));
    }

    public void updateTask(TaskExecResult taskResult) {
        TaskModelPb.UpdateTaskRequest request = TaskModelPb.UpdateTaskRequest.newBuilder()
            .setResult(MapperFactory.getInstance().toProto(taskResult)).build();
        TaskModelPb.UpdateTaskResponse response = stub.updateTask(request);
        response.getTaskId();
    }
}
