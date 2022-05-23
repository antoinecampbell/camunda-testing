# camunda-testing

![Alt Architecture Diagram](./images/aws-architecture-camunda-testing.png)

## Setup
Custom spring properties
- aws.response-queue (SQS queue URL)
- aws.task-topic (SNS topic ARN)
- com.antoinecampbell.camunda.enable-internal-services (Enable external task, and response services)
