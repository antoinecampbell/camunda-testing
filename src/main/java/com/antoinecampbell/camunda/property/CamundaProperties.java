package com.antoinecampbell.camunda.property;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("com.antoinecampbell.camunda")
@Getter
@Setter
public class CamundaProperties {

    /**
     * Enables the ResponseService and ExternalTaskWorkerService when true.
     */
    private boolean enableInternalServices = false;
    /**
     * Max number of messages to receive in a SQS receive message request.
     * Valid values: 1 to 10
     */
    private int maxReceiveMessageCount = 10;
}
