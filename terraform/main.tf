locals {
  exetrnal_service_name = "node-external-service"
  node_lambda_zip_location = "../node-external-service/build/distributions/node-external-service.zip"
  tags = {
    Owner: "acampbell"
  }
}

module "service_1" {
  source = "./modules/sns-sqs-lambda"

  function_name = "external-task-service-1"
  role = aws_iam_role.lambda_exec.arn
  zip_location = local.node_lambda_zip_location
  topic_arn = aws_sns_topic.external_task.arn
  routing_key = "task1"
  tags = local.tags
}

module "service_2" {
  source = "./modules/sns-sqs-lambda"

  function_name = "external-task-service-2"
  role = aws_iam_role.lambda_exec.arn
  zip_location = local.node_lambda_zip_location
  topic_arn = aws_sns_topic.external_task.arn
  routing_key = "task2"
  tags = local.tags
}

module "service_3" {
  source = "./modules/sns-sqs-lambda"

  function_name = "external-task-service-3"
  role = aws_iam_role.lambda_exec.arn
  zip_location = local.node_lambda_zip_location
  topic_arn = aws_sns_topic.external_task.arn
  routing_key = "task3"
  tags = local.tags
}

module "service_4" {
  source = "./modules/sns-sqs-lambda"

  function_name = "external-task-service-4"
  role = aws_iam_role.lambda_exec.arn
  zip_location = local.node_lambda_zip_location
  topic_arn = aws_sns_topic.external_task.arn
  routing_key = "task4"
  tags = local.tags
}

module "service_5" {
  source = "./modules/sns-sqs-lambda"

  function_name = "external-task-service-5"
  role = aws_iam_role.lambda_exec.arn
  zip_location = local.node_lambda_zip_location
  topic_arn = aws_sns_topic.external_task.arn
  routing_key = "task5"
  tags = local.tags
}

module "service_6" {
  source = "./modules/sns-sqs-lambda"

  function_name = "external-task-service-6"
  role = aws_iam_role.lambda_exec.arn
  zip_location = local.node_lambda_zip_location
  topic_arn = aws_sns_topic.external_task.arn
  routing_key = "task6"
  tags = local.tags
}

module "service_7" {
  source = "./modules/sns-sqs-lambda"

  function_name = "external-task-service-7"
  role = aws_iam_role.lambda_exec.arn
  zip_location = local.node_lambda_zip_location
  topic_arn = aws_sns_topic.external_task.arn
  routing_key = "task7"
  tags = local.tags
}

module "service_8" {
  source = "./modules/sns-sqs-lambda"

  function_name = "external-task-service-8"
  role = aws_iam_role.lambda_exec.arn
  zip_location = local.node_lambda_zip_location
  topic_arn = aws_sns_topic.external_task.arn
  routing_key = "task8"
  tags = local.tags
}

module "service_9" {
  source = "./modules/sns-sqs-lambda"

  function_name = "external-task-service-9"
  role = aws_iam_role.lambda_exec.arn
  zip_location = local.node_lambda_zip_location
  topic_arn = aws_sns_topic.external_task.arn
  routing_key = "task9"
  tags = local.tags
}

resource "aws_sns_topic" "external_task" {
  name = "external-task"
  tags = local.tags
}

resource "aws_sqs_queue" "camunda_response_queue_dead" {
  name = "camunda-response-dead"
  tags = local.tags
}

resource "aws_sqs_queue" "camunda_response_queue" {
  name = "camunda-response"
  receive_wait_time_seconds = 20
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.camunda_response_queue_dead.arn
    maxReceiveCount = 3
  })
  tags = local.tags
}

output "camunda_response_queue" {
  value = aws_sqs_queue.camunda_response_queue.id
}

output "external_task_topic" {
  value = aws_sns_topic.external_task.arn
}