package com.antoinecampbell.camunda.external.task;

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
public class ExternalTaskWorkerService {

    private final ExternalTaskService externalTaskService;
    private final ExternalTaskRepository externalTaskRepository;
    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;
    private String externalTaskTopicArn;
    private final String responseQueueUrl;

    public ExternalTaskWorkerService(ExternalTaskService externalTaskService,
                                     ExternalTaskRepository externalTaskRepository,
                                     ObjectMapper objectMapper,
                                     SnsClient snsClient,
                                     @Value("${aws.task.topic:none}") String externalTaskTopicArn,
                                     @Value("${aws.response.queue:none}") String responseQueueUrl) {
        this.externalTaskService = externalTaskService;
        this.externalTaskRepository = externalTaskRepository;
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
        List<String> availableTopics = externalTaskRepository.getAvailableTopics();
        try {
            log.debug("Topics: {}",
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(availableTopics));
            if (availableTopics.isEmpty()) {
                log.debug("No tasks to send");
                return;
            }
        } catch (JsonProcessingException e) {
            log.error("Error mapping list", e);
        }

        ExternalTaskQueryBuilder externalTaskQueryBuilder =
                externalTaskService.fetchAndLock(Constants.TASK_LOCK_COUNT, Constants.WORKER_NAME);
        for (String topic : availableTopics) {
            externalTaskQueryBuilder.topic(topic, Constants.TASK_TIMEOUT_MILLIS);
        }
        List<LockedExternalTask> externalTasks = externalTaskQueryBuilder.execute();
        for (LockedExternalTask lockedExternalTask : externalTasks) {
            try {
                sendMessage(lockedExternalTask.getTopicName(),
                        lockedExternalTask.getBusinessKey(),
                        lockedExternalTask.getProcessDefinitionKey(),
                        lockedExternalTask.getVariables());
            } catch (Exception e) {
                log.error("Error sending SQS message", e);
            }
        }
    }

    private void sendMessage(String topic, String businessKey, String workflow,
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

    private Map<String, Object> convertVariables(Map<String, Object> variables) {
        Map<String, Object> convertedVariables = new HashMap<>();

        return convertedVariables;
    }
}
