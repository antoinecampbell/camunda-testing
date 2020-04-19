package com.antoinecampbell.camunda;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;

public class Sample1BpmnTest extends BaseBpmnTest {

    @Rule
    @ClassRule
    public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

    private static final String PROCESS_KEY = "sample1";

    @Override
    ProcessEngineRule getRule() {
        return rule;
    }

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
