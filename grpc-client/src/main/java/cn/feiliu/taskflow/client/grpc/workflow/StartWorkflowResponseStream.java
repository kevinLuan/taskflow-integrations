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

import cn.feiliu.taskflow.common.model.WorkflowRun;
import cn.feiliu.taskflow.mapper.MapperFactory;
import cn.feiliu.taskflow.open.exceptions.ApiException;
import cn.feiliu.taskflow.proto.FlowModelPb;
import io.grpc.Status;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class StartWorkflowResponseStream
                                        implements
                                        ClientResponseObserver<FlowModelPb.StartWorkflowRequest, FlowModelPb.StartWorkflowResponse> {

    private final WorkflowExecutionMonitor                             executionMonitor;

    private ClientCallStreamObserver<FlowModelPb.StartWorkflowRequest> requestStream;

    public StartWorkflowResponseStream(WorkflowExecutionMonitor executionMonitor) {
        this.executionMonitor = executionMonitor;
    }

    @Override
    public void onNext(FlowModelPb.StartWorkflowResponse response) {

        try {

            CompletableFuture<WorkflowRun> future = this.executionMonitor.getFuture(response.getRequestId());
            if (future == null) {
                log.warn("No waiting client for the request {}", response.getRequestId());
                return;
            }
            if (response.hasError()) {
                String message = response.getError().getMessage();
                int code = response.getError().getCode();
                future.completeExceptionally(new ApiException(code, message));
            } else {
                WorkflowRun workflowRun = MapperFactory.getInstance().fromProto(response.getWorkflow());
                future.complete(workflowRun);
            }

        } catch (Throwable t) {
            log.error("Error while trying to notify the client {}", t.getMessage(), t);
        }
    }

    @Override
    public void onError(Throwable t) {
        Status status = Status.fromThrowable(t);
        Status.Code code = status.getCode();
        switch (code) {
            case UNAVAILABLE:
            case ABORTED:
            case INTERNAL:
            case UNKNOWN:
                log.error("Received an error from the server {}-{}", code, t.getMessage());
                break;
            case CANCELLED:
                log.info("Server cancelled"); //TODO: move this to trace
            default:
                log.warn("Server Error {} - {}", code, t.getMessage(), t);
        }
    }

    public boolean isReady() {
        return requestStream.isReady();
    }

    @Override
    public void onCompleted() {
        log.info("Completed....");
    }

    @Override
    public void beforeStart(ClientCallStreamObserver<FlowModelPb.StartWorkflowRequest> requestStream) {
        this.requestStream = requestStream;
    }
}
