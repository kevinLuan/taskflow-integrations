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
///*
// * Copyright 2024 Taskflow, Inc.
// * <p>
// * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
// * the License. You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package cn.feiliu.taskflow.client.grpc;
//
//import cn.feiliu.taskflow.client.ApiClient;
//import cn.feiliu.taskflow.client.grpc.workflow.GrpcWorkflowClient;
//import cn.feiliu.taskflow.client.spi.TaskflowGrpcSPI;
//import cn.feiliu.taskflow.common.metadata.tasks.ExecutingTask;
//import cn.feiliu.taskflow.common.metadata.tasks.TaskExecResult;
//import cn.feiliu.taskflow.common.metadata.tasks.TaskLog;
//import cn.feiliu.taskflow.common.metadata.workflow.StartWorkflowRequest;
//import cn.feiliu.taskflow.mapper.MapperFactory;
//import cn.feiliu.taskflow.open.exceptions.ApiException;
//import cn.feiliu.taskflow.proto.FlowModelPb;
//import cn.feiliu.taskflow.proto.TaskModelPb;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.List;
//import java.util.concurrent.Future;
//
///**
// * @author SHOUSHEN.LUAN
// * @since 2024-09-23
// */
//public class TaskflowGrpcApiImpl implements TaskflowGrpcSPI {
//    private final Logger       log = LoggerFactory.getLogger(TaskflowGrpcApiImpl.class);
//    private ApiClient          client;
//    private ChannelManager     channelManager;
//    private GrpcTaskClient     taskClient;
//    private GrpcWorkflowClient workflowClient;
//
//    public void init(ApiClient client) {
//        this.client = client;
//        this.channelManager = new ChannelManager(client);
//        this.taskClient = new GrpcTaskClient(channelManager);
//        this.workflowClient = new GrpcWorkflowClient(channelManager);
//    }
//
//    @Override
//    public List<ExecutingTask> batchPollTask(String taskType, String workerId, String domain, int count,
//                                             int timeoutInMillisecond) {
//        return taskClient.batchPoll(taskType, workerId, domain, count, timeoutInMillisecond);
//    }
//
//    @Override
//    public void updateTask(TaskExecResult taskResult) {
//        taskClient.updateTask(taskResult);
//    }
//
//    @Override
//    public void shutdown() {
//        channelManager.shutdown();
//    }
//
//    @Override
//    public Future<?> asyncUpdateTask(TaskExecResult result) {
//        TaskModelPb.TaskResult taskResult = MapperFactory.getInstance().toProto(result);
//        TaskModelPb.UpdateTaskRequest request = TaskModelPb.UpdateTaskRequest.newBuilder().setResult(taskResult)
//            .build();
//        return channelManager.newTaskflowServiceFutureStub().updateTask(request);
//    }
//
//    @Override
//    public Future<?> addLog(TaskLog taskLog) {
//        TaskModelPb.AddLogRequest req = TaskModelPb.AddLogRequest.newBuilder().setLog(taskLog.getLog())
//            .setTaskId(taskLog.getTaskId()).build();
//        return channelManager.newTaskflowServiceFutureStub().addLog(req);
//    }
//
//    @Override
//    public String startWorkflow(StartWorkflowRequest req) {
//        try {
//            FlowModelPb.StartWorkflowResponse resp = workflowClient.start(req);
//            if (resp.hasError()) {
//                int code = resp.getError().getCode();
//                String message = resp.getError().getMessage();
//                throw new ApiException(code, message);
//            } else {
//                return resp.getWorkflow().getWorkflowId();
//            }
//        } catch (Throwable t) {
//            log.error("Error while trying to notify the client {}", t.getMessage(), t);
//            throw t;
//        }
//    }
//}
