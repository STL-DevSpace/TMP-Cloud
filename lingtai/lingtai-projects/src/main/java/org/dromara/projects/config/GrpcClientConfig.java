package org.dromara.projects.config;

import com.edgeai.training.api.TrainingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * gRPC 客户端配置
 */
@Slf4j
@Configuration
public class GrpcClientConfig {
    @Value("${grpc.training.host:localhost}")
    private String grpcHost;

    @Value("${grpc.training.port:50051}")
    private int grpcPort;

    private ManagedChannel channel;

    @Bean
    public ManagedChannel managedChannel() {
        channel = ManagedChannelBuilder
            .forAddress(grpcHost, grpcPort)
            .usePlaintext()
            .build();
        log.info("gRPC Channel created: {}:{}", grpcHost, grpcPort);
        return channel;
    }

    @Bean
    public TrainingServiceGrpc.TrainingServiceBlockingStub trainingServiceStub(ManagedChannel channel) {
        return TrainingServiceGrpc.newBlockingStub(channel);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
            log.info("gRPC Channel shutdown");
        }
    }
}
