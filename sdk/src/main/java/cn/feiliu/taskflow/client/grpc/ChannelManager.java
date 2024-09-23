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

import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.grpc.TaskflowServiceGrpc;
import cn.feiliu.taskflow.grpc.TaskflowStreamServiceGrpc;
import cn.feiliu.taskflow.serialization.SerializerFactory;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ChannelManager {
    private final ApiClient             apiClient;
    private ManagedChannel              channel = null;
    private final Map<Class<?>, Object> CACHE   = new ConcurrentHashMap<>();

    public ChannelManager(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ManagedChannel getChannel() {
        if (channel == null) {
            synchronized (this) {
                if (channel == null) {
                    this.channel = newChannel();
                }
            }
        }
        return channel;
    }

    private ManagedChannel newChannel() {
        String host = apiClient.getGrpcHost();
        int port = apiClient.getGrpcPort();
        boolean useSSL = apiClient.useSSL();
        Map<String, Object> serviceConfig = tryLookupServiceConfig();
        NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress(host, port)
            .eventLoopGroup(new NioEventLoopGroup()).channelType(NioSocketChannel.class).enableRetry()
            .withOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TimeUnit.SECONDS.toMillis(30))
            .defaultServiceConfig(serviceConfig).keepAliveTime(10, TimeUnit.MINUTES)
            .defaultLoadBalancingPolicy("round_robin");
        if (apiClient.getExecutorThreadCount() > 0) {
            log.info("GRPC customize thread pool size: {}", apiClient.getExecutorThreadCount());
            channelBuilder = channelBuilder.executor(new ThreadPoolExecutor(0, apiClient.getExecutorThreadCount(), 60L,
                TimeUnit.SECONDS, new SynchronousQueue<>()));
        }
        if (!useSSL) {
            channelBuilder = channelBuilder.usePlaintext();
        } else {
            channelBuilder = channelBuilder.useTransportSecurity();
        }
        return channelBuilder.build();
    }

    private Map<String, Object> tryLookupServiceConfig() {
        Map<String, Object> serviceConfig;
        try {
            serviceConfig = SerializerFactory.getSerializer().readMap(
                ChannelManager.class.getResourceAsStream("/service_config.json"));
        } catch (Exception e) {
            throw new RuntimeException("Unable to find a service configuration", e);
        }
        return serviceConfig;
    }

    public void shutdown() {
        if (channel != null) {
            this.channel.shutdown();
        }
    }

    private <T> T createIfAbsent(Class<?> type, Supplier<T> supplier) {
        return (T) CACHE.computeIfAbsent(type, (k) -> {
            return supplier.get();
        });
    }

    public TaskflowServiceGrpc.TaskflowServiceFutureStub newTaskflowServiceFutureStub() {
        return createIfAbsent(TaskflowServiceGrpc.TaskflowServiceFutureStub.class, () -> {
            return TaskflowServiceGrpc.newFutureStub(getChannel())
                    .withDeadline(Deadline.after(5, TimeUnit.SECONDS))
                    .withInterceptors(new HeaderClientInterceptor(apiClient));
        });
    }

    public TaskflowStreamServiceGrpc.TaskflowStreamServiceBlockingStub newTaskflowStreamServiceBlockingStub() {
        return createIfAbsent(TaskflowStreamServiceGrpc.TaskflowStreamServiceBlockingStub.class, () -> {
            return TaskflowStreamServiceGrpc.newBlockingStub(getChannel())
                    .withDeadline(Deadline.after(5, TimeUnit.SECONDS))
                    .withInterceptors(new HeaderClientInterceptor(apiClient));
        });
    }

    public TaskflowServiceGrpc.TaskflowServiceBlockingStub newTaskflowServiceBlockingStub() {
        return createIfAbsent(TaskflowServiceGrpc.TaskflowServiceBlockingStub.class, () -> {
            return TaskflowServiceGrpc.newBlockingStub(getChannel())
                    .withDeadline(Deadline.after(5, TimeUnit.SECONDS))
                    .withInterceptors(new HeaderClientInterceptor(apiClient));
        });
    }
}
