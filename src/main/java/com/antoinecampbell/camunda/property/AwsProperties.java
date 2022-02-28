package com.antoinecampbell.camunda.property;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("aws")
@Getter
@Setter
public class AwsProperties {

    private String responseQueue;
    private String taskTopic;
    private int responseQueueThreadCount = 1;
    private int externalTaskThreadCount = 1;
}
