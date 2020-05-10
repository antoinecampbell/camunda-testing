package com.antoinecampbell.camunda;

import com.antoinecampbell.camunda.external.task.ExternalTaskRepository;
import com.antoinecampbell.camunda.external.task.ExternalTaskWorkerService;
import com.antoinecampbell.camunda.model.MessageType;
import com.antoinecampbell.camunda.model.WorkflowMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.Map;

@Service
@Slf4j
public class ResponseService {

    private final ExternalTaskService externalTaskService;
    private final ExternalTaskRepository externalTaskRepository;
    private final ExternalTaskWorkerService externalTaskWorkerService;
    private final ObjectMapper objectMapper;
    private final SqsClient sqsClient;
    private final String responseQueueUrl;

    public ResponseService(ExternalTaskService externalTaskService,
                           ExternalTaskRepository externalTaskRepository,
                           ExternalTaskWorkerService externalTaskWorkerService,
                           ObjectMapper objectMapper,
                           SqsClient sqsClient,
                           @Value("${aws.response.queue:none}") String responseQueueUrl) {
        this.externalTaskService = externalTaskService;
        this.externalTaskRepository = externalTaskRepository;
        this.externalTaskWorkerService = externalTaskWorkerService;
        this.objectMapper = objectMapper;
        this.sqsClient = sqsClient;
        this.responseQueueUrl = responseQueueUrl;

        log.info("Queue: {}", responseQueueUrl);
    }

    @Scheduled(fixedDelay = 1000)
    @Async("responseQueueExecutor")
    public void pollResponseQueue() {
        try {
            log.debug("Long-polling queue: {}", responseQueueUrl);
            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(responseQueueUrl)
                    .maxNumberOfMessages(10)
                    .messageAttributeNames("All")
                    .build();
            ReceiveMessageResponse receiveMessageResponse = sqsClient.receiveMessage(receiveMessageRequest);
            for (Message message : receiveMessageResponse.messages()) {
                handleMessage(message);
            }
        } catch (Exception e) {
            log.error("Error receiving messages", e);
        }
    }

    private void handleMessage(Message message) throws Exception {
        log.debug(String.format("Message: %s", message.body()));
        WorkflowMessage workflowMessage = objectMapper.readValue(message.body(), WorkflowMessage.class);
        Map<String, MessageAttributeValue> attributes = message.messageAttributes();
        MessageType type = MessageType.fromString(attributes.getOrDefault(Constants.ATTRIBUTE_NAME_TYPE,
                MessageAttributeValue.builder().build()).stringValue());
        boolean success = Boolean.parseBoolean(attributes.getOrDefault(Constants.ATTRIBUTE_ROUTING_SUCCESS,
                MessageAttributeValue.builder().build()).stringValue());
        String routingKey = attributes.getOrDefault(Constants.ATTRIBUTE_ROUTING_KEY,
                MessageAttributeValue.builder().build()).stringValue();
        String externalTaskId = externalTaskRepository.getExternalTaskId(workflowMessage.getBusinessKey(),
                routingKey);
        log.debug("Message: {}", workflowMessage);
        log.debug("Type: {}", attributes.get(Constants.ATTRIBUTE_NAME_TYPE));
        log.debug("Success: {}", success);
        log.debug("Routing-key: {}", routingKey);
        log.debug("Task id: {}", externalTaskId);
        if (externalTaskId != null) {
            if (type == MessageType.EXTERNAL_TASK) {
                if (success) {
                    // Complete task
                    log.debug("Completing task: {}", externalTaskId);
                    externalTaskService.complete(externalTaskId,
                            Constants.WORKER_NAME,
                            workflowMessage.getVariables());

                } else {
                    // Fail task
                    log.debug("Failing task: {}", externalTaskId);
                    ExternalTask externalTask = externalTaskService.createExternalTaskQuery()
                            .externalTaskId(externalTaskId)
                            .singleResult();
                    externalTaskService.handleFailure(externalTaskId,
                            Constants.WORKER_NAME,
                            "TODO: Error message",
                            Math.max(0, externalTask.getRetries() - 1),
                            3000);
                }
                // Process external tasks
                log.debug("Calling process tasks from ResponseService");
                externalTaskWorkerService.processTasks();
            } else {
                log.error(String.format("Unknown message type: %s", message));
            }
        } else {
            log.warn("No external task found");
        }
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(responseQueueUrl)
                .receiptHandle(message.receiptHandle())
                .build());
    }
}
