package com.antoinecampbell.camunda;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Before;

public abstract class BaseBpmnTest {
    protected ProcessEngine processEngine;
    protected RuntimeService runtimeService;
    protected TaskService taskService;
    protected ProcessInstance processInstance;

    @Before
    public void setUp() {
        ProcessEngineRule rule = getRule();
        processEngine = rule.getProcessEngine();
        runtimeService = rule.getRuntimeService();
        taskService = rule.getTaskService();
    }

    abstract ProcessEngineRule getRule();
}
