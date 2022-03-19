package com.antoinecampbell.camunda;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.externalTask;

@Deployment(resources = "diagrams/aws-test.bpmn")
public class AwsBpmnTest extends BaseBpmnTest {

    @Rule
    @ClassRule
    public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

    private static final String PROCESS_KEY = "aws-test";

    @Override
    ProcessEngineRule getRule() {
        return rule;
    }

    @Test
    public void shouldCompleteProcess() {
        processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
        // task 1
        assertThat(processInstance).isStarted().isWaitingAt("task1");
        complete(externalTask("task1"), Collections.singletonMap("randomNumber", 1));
        // task 2
        assertThat(processInstance).isStarted().isWaitingAt("task2");
        complete(externalTask("task2"));
        // task 3
        assertThat(processInstance).isStarted().isWaitingAt("task3");
        complete(externalTask("task3"));
        // task 4
        assertThat(processInstance).isStarted().isWaitingAt("task4");
        complete(externalTask("task4"));
        // task 5
        assertThat(processInstance).isStarted().isWaitingAt("task5");
        complete(externalTask("task5"));
        // task 6
        assertThat(processInstance).isStarted().isWaitingAt("task6");
        complete(externalTask("task6"));
        // task 7
        assertThat(processInstance).isStarted().isWaitingAt("task7");
        complete(externalTask("task7"));
        // task 8
        assertThat(processInstance).isStarted().isWaitingAt("task8");
        complete(externalTask("task8"));
        // task 9
        assertThat(processInstance).isStarted().isWaitingAt("task9");
        complete(externalTask("task9"));
        // end
        assertThat(processInstance).isEnded();
    }

    @Test
    public void shouldRestartWhenRandomNumberIs5() {
        processInstance = runtimeService.createProcessInstanceByKey(PROCESS_KEY)
                .startBeforeActivity("task8")
                .execute();
        // task 8
        assertThat(processInstance).isStarted().isWaitingAt("task8");
        complete(externalTask("task8"), Collections.singletonMap("randomNumber", 5));
        // task 1
        assertThat(processInstance).isStarted().isWaitingAt("task1");
        assertThat(processInstance).isNotEnded();
    }
}
