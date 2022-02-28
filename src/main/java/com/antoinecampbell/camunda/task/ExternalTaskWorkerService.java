package com.antoinecampbell.camunda.task;

import com.antoinecampbell.camunda.Constants;
import com.antoinecampbell.camunda.model.MessageType;
import com.antoinecampbell.camunda.model.WorkflowMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryBuilder;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@ConditionalOnProperty("com.antoinecampbell.camunda.enable-internal-services")
public class ExternalTaskWorkerService {

    private final ExternalTaskService externalTaskService;
    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;
    private final String externalTaskTopicArn;
    private final String responseQueueUrl;

    public ExternalTaskWorkerService(ExternalTaskService externalTaskService,
                                     ObjectMapper objectMapper,
                                     SnsClient snsClient,
                                     @Value("${aws.task-topic:none}") String externalTaskTopicArn,
                                     @Value("${aws.response-queue:none}") String responseQueueUrl) {
        this.externalTaskService = externalTaskService;
        this.objectMapper = objectMapper;
        this.snsClient = snsClient;
        this.externalTaskTopicArn = externalTaskTopicArn;
        this.responseQueueUrl = responseQueueUrl;

        log.info("Topic: {}", externalTaskTopicArn);
        log.info("Queue: {}", responseQueueUrl);
    }

    @Scheduled(fixedRate = Constants.TASK_POLL_MILLIS)
    @Async("externalTaskExecutor")
    public void processTasks() {
        // Fetch topics
        List<String> availableTopics = externalTaskService.getTopicNames(false, true, true);
        try {
            log.debug("Topics: {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(availableTopics));
            if (availableTopics.isEmpty()) {
                log.debug("No tasks to send");
                return;
            }
        } catch (JsonProcessingException e) {
            log.error("Error mapping list", e);
        }

        // Fetch tasks for each topic
        ExternalTaskQueryBuilder externalTaskQueryBuilder =
                externalTaskService.fetchAndLock(Constants.TASK_LOCK_COUNT, Constants.WORKER_NAME);
        for (String topic : availableTopics) {
            externalTaskQueryBuilder.topic(topic, Constants.TASK_TIMEOUT_MILLIS);
        }
        List<LockedExternalTask> externalTasks = externalTaskQueryBuilder.execute();
        for (LockedExternalTask lockedExternalTask : externalTasks) {
            // Send messages for each task to topic
            try {
                sendMessage(lockedExternalTask.getTopicName(),
                        lockedExternalTask.getId(),
                        lockedExternalTask.getBusinessKey(),
                        lockedExternalTask.getProcessDefinitionKey(),
                        lockedExternalTask.getVariables());
            } catch (Exception e) {
                log.error("Error sending SQS message", e);
            }
        }
    }

    private void sendMessage(String topic, String taskId, String businessKey, String workflow,
                             Map<String, Object> variables) throws Exception {
        log.debug("Sending SNS message for topic: {}", topic);
        Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put(Constants.ATTRIBUTE_NAME_TYPE, MessageAttributeValue.builder()
                .stringValue(MessageType.EXTERNAL_TASK.getName())
                .dataType("String")
                .build());
        messageAttributes.put(Constants.ATTRIBUTE_ROUTING_KEY, MessageAttributeValue.builder()
                .stringValue(topic)
                .dataType("String")
                .build());
        messageAttributes.put(Constants.ATTRIBUTE_REPLY_TO, MessageAttributeValue.builder()
                .stringValue(responseQueueUrl)
                .dataType("String")
                .build());
        messageAttributes.put(Constants.ATTRIBUTE_TASK_ID, MessageAttributeValue.builder()
                .stringValue(taskId)
                .dataType("String")
                .build());
        WorkflowMessage workflowMessage = WorkflowMessage.builder()
                .businessKey(businessKey)
                .workflow(workflow)
                .variables(variables)
                .build();
        snsClient.publish(PublishRequest.builder()
                .topicArn(externalTaskTopicArn)
                .message(objectMapper.writeValueAsString(workflowMessage))
                .messageAttributes(messageAttributes)
                .build());
    }
}
