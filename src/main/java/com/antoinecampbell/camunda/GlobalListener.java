package com.antoinecampbell.camunda;

import com.antoinecampbell.camunda.external.task.ExternalTaskWorkerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.impl.bpmn.behavior.ExternalTaskActivityBehavior;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.spring.boot.starter.event.ExecutionEvent;
import org.camunda.bpm.spring.boot.starter.event.TaskEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@ConditionalOnExpression("false ") // Disabling
@Component
@Slf4j
public class GlobalListener {

    private final ObjectMapper objectMapper;
    private final ExternalTaskWorkerService externalTaskWorkerService;

    public GlobalListener(ExternalTaskWorkerService externalTaskWorkerService,
                          ObjectMapper objectMapper) {
        this.externalTaskWorkerService = externalTaskWorkerService;
        this.objectMapper = objectMapper;
    }

    @EventListener(condition = "false ") // Disabling
    public void onDelegateTask(DelegateTask taskDelegate) {
        // handle mutable task event
        logValue(taskDelegate);
    }

    @EventListener(condition = "false ") // Disabling
    public void onTaskEvent(TaskEvent taskEvent) {
        // handle immutable task event
        logValue(taskEvent);
    }

    @EventListener(condition = "false ") // Disabling
    public void onDelegateExecution(DelegateExecution executionDelegate) {
        // handle mutable execution event - THIS IS CALLED
        if (isExternalTaskStart(executionDelegate)) {
            // Process external tasks
            externalTaskWorkerService.processTasks();
            logValue(executionDelegate);
        }
    }

    @EventListener(condition = "false ") // Disabling
    public void onExecutionEvent(ExecutionEvent executionEvent) {
        // handle immutable execution event - THIS IS CALLED
        logValue(executionEvent);
    }

    private boolean isExternalTaskStart(DelegateExecution executionDelegate) {
        if (executionDelegate instanceof ExecutionEntity) {
            ExecutionEntity executionEntity = (ExecutionEntity) executionDelegate;
            ActivityImpl activity = executionEntity.getActivity();
            ActivityBehavior activityBehavior = activity.getActivityBehavior();
            return activityBehavior instanceof ExternalTaskActivityBehavior
                    && executionEntity.isActive();

        }
        return false;
    }

    private void logValue(Object object) {
        try {
            log.debug(objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(object));
        } catch (JsonProcessingException e) {
            log.debug(object.toString());
        }
    }
}
