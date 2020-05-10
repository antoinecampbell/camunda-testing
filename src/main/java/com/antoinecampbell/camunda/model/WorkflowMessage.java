package com.antoinecampbell.camunda.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WorkflowMessage {

    private String businessKey;
    private String workflow;
    private Map<String, Object> variables;
}
