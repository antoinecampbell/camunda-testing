package com.antoinecampbell.camunda;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@EnableAsync
@EnableScheduling
@EnableJdbcRepositories
@EnableConfigurationProperties
public class Config {

    @Bean
    public TaskExecutor responseQueueExecutor() {
        return createTaskExecutor(5, "response-");
    }

    @Bean
    public TaskExecutor externalTaskExecutor() {
        return createTaskExecutor(3, "worker-");
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
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    private TaskExecutor createTaskExecutor(int threadCount, String prefix) {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(threadCount);
        threadPoolTaskExecutor.setMaxPoolSize(threadCount);
        threadPoolTaskExecutor.setThreadNamePrefix(prefix);
        return threadPoolTaskExecutor;
    }
}
