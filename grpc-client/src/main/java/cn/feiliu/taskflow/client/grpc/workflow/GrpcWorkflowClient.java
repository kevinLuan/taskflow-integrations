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
package cn.feiliu.taskflow.client.grpc.workflow;

import cn.feiliu.taskflow.client.grpc.ChannelManager;
import cn.feiliu.taskflow.common.metadata.workflow.StartWorkflowRequest;
import cn.feiliu.taskflow.grpc.TaskflowServiceGrpc;
import cn.feiliu.taskflow.mapper.MapperFactory;
import cn.feiliu.taskflow.proto.FlowModelPb;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class GrpcWorkflowClient {

    final TaskflowServiceGrpc.TaskflowServiceFutureStub   futureStub;
    final TaskflowServiceGrpc.TaskflowServiceBlockingStub blockingStub;

    public GrpcWorkflowClient(ChannelManager channelManager) {
        futureStub = channelManager.newTaskflowServiceFutureStub();
        blockingStub = channelManager.newTaskflowServiceBlockingStub();
    }

    public ListenableFuture<FlowModelPb.StartWorkflowResponse> executeWorkflow(StartWorkflowRequest request) {
        String requestId = UUID.randomUUID().toString();
        FlowModelPb.StartWorkflowRequest startRequest = MapperFactory.getInstance().toProto(request).toBuilder()
            .setRequestId(requestId).build();
        return futureStub.startWorkflow(startRequest);
    }

    public FlowModelPb.StartWorkflowResponse start(StartWorkflowRequest request) {
        String requestId = UUID.randomUUID().toString();
        FlowModelPb.StartWorkflowRequest startRequest = MapperFactory.getInstance().toProto(request).toBuilder()
            .setRequestId(requestId).build();
        return blockingStub.startWorkflow(startRequest);
    }
}
