package com.antoinecampbell.camunda;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;

public class Sample1BpmnTest extends BaseBpmnTest {

    private static final String PROCESS_KEY = "sample1";

    @Test
    @Deployment(resources = "sample1.bpmn")
    public void shouldCompleteProcess() {
        processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
        assertThat(processInstance).isStarted()
                .isWaitingAt("task1");

        complete(externalTask("task1"));
        assertThat(processInstance).isEnded();
    }
}
