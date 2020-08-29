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

    private boolean enableInternalServices = true;
}
