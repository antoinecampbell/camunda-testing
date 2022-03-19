package com.antoinecampbell.camunda.config;

import com.antoinecampbell.camunda.property.AwsProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@ConditionalOnProperty("com.antoinecampbell.camunda.enable-internal-services")
public class AwsConfig {

    private final AwsProperties awsProperties;

    public AwsConfig(AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
    }

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.create();
    }

    @Bean
    public SnsClient snsClient() {
        return SnsClient.create();
    }

    @Bean
    public TaskExecutor responseQueueExecutor() {
        return createTaskExecutor(awsProperties.getResponseQueueThreadCount(), "response-");
    }

    @Bean
    public TaskExecutor externalTaskExecutor() {
        return createTaskExecutor(awsProperties.getExternalTaskThreadCount(), "worker-");
    }

    private TaskExecutor createTaskExecutor(int threadCount, String prefix) {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(threadCount);
        threadPoolTaskExecutor.setMaxPoolSize(threadCount);
        threadPoolTaskExecutor.setThreadNamePrefix(prefix);
        return threadPoolTaskExecutor;
    }
}
