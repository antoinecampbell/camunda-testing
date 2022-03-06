variable "environment" {
  default = "test"
}
variable "alerts_enabled" {
  default = false
}

locals {
  node_lambda_zip_location = "../node-external-service/build/distributions/node-external-service.zip"
  app = "camunda-testing-${var.environment}"
  tags = {
    Owner : "acampbell"
    Environment : var.environment
    App : local.app
  }
  topic = "external-task-${var.environment}"
  response_queue = "camunda-response-${var.environment}"
  dead_letter_queue = "camunda-response-dead-${var.environment}"
  bucket_name = "camunda-testing-lambda-${var.environment}-${random_string.bucket_suffix.id}"
  file_hash = filebase64sha256(local.node_lambda_zip_location)
}

resource "random_string" "bucket_suffix" {
  length = 7
  special = false
  upper = false
}

resource "aws_s3_bucket" "lambda" {
  bucket = local.bucket_name
  tags = merge(merge(local.tags, { Name : local.bucket_name }))
}

resource "aws_s3_bucket_acl" "lambda" {
  bucket = aws_s3_bucket.lambda.id
  acl = "private"
}

resource "aws_s3_object" "lambda_zip" {
  bucket = aws_s3_bucket.lambda.id
  key = "external-task-service.zip"
  source = local.node_lambda_zip_location
  etag = filemd5(local.node_lambda_zip_location)
  tags = local.tags
}

module "service_1" {
  source = "./modules/sns-sqs-lambda"

  function_name = "external-task-service-1"
  s3_bucket = aws_s3_bucket.lambda.id
  s3_key = aws_s3_object.lambda_zip.id
  file_hash = local.file_hash
  topic_arn = aws_sns_topic.external_task.arn
  routing_key = "task1"
  tags = local.tags
  environment = var.environment
  alerts_enabled = var.alerts_enabled
}

module "service_2" {
  source = "./modules/sns-sqs-lambda"

  function_name = "external-task-service-2"
  s3_bucket = aws_s3_bucket.lambda.id
  s3_key = aws_s3_object.lambda_zip.id
  file_hash = local.file_hash
  topic_arn = aws_sns_topic.external_task.arn
  routing_key = "task2"
  tags = local.tags
  environment = var.environment
  alerts_enabled = var.alerts_enabled
}

module "service_3" {
  source = "./modules/sns-sqs-lambda"

  function_name = "external-task-service-3"
  s3_bucket = aws_s3_bucket.lambda.id
  s3_key = aws_s3_object.lambda_zip.id
  file_hash = local.file_hash
  topic_arn = aws_sns_topic.external_task.arn
  routing_key = "task3"
  tags = local.tags
  environment = var.environment
  alerts_enabled = var.alerts_enabled
}

module "service_4" {
  source = "./modules/sns-sqs-lambda"

  function_name = "external-task-service-4"
  s3_bucket = aws_s3_bucket.lambda.id
  s3_key = aws_s3_object.lambda_zip.id
  file_hash = local.file_hash
  topic_arn = aws_sns_topic.external_task.arn
  routing_key = "task4"
  tags = local.tags
  environment = var.environment
  alerts_enabled = var.alerts_enabled
}

module "service_5" {
  source = "./modules/sns-sqs-lambda"

  function_name = "external-task-service-5"
  s3_bucket = aws_s3_bucket.lambda.id
  s3_key = aws_s3_object.lambda_zip.id
  file_hash = local.file_hash
  topic_arn = aws_sns_topic.external_task.arn
  routing_key = "task5"
  tags = local.tags
  environment = var.environment
  alerts_enabled = var.alerts_enabled
}

module "service_6" {
  source = "./modules/sns-sqs-lambda"

  function_name = "external-task-service-6"
  s3_bucket = aws_s3_bucket.lambda.id
  s3_key = aws_s3_object.lambda_zip.id
  file_hash = local.file_hash
  topic_arn = aws_sns_topic.external_task.arn
  routing_key = "task6"
  tags = local.tags
  environment = var.environment
  alerts_enabled = var.alerts_enabled
}

module "service_7" {
  source = "./modules/sns-sqs-lambda"

  function_name = "external-task-service-7"
  s3_bucket = aws_s3_bucket.lambda.id
  s3_key = aws_s3_object.lambda_zip.id
  file_hash = local.file_hash
  topic_arn = aws_sns_topic.external_task.arn
  routing_key = "task7"
  tags = local.tags
  environment = var.environment
  alerts_enabled = var.alerts_enabled
}

module "service_8" {
  source = "./modules/sns-sqs-lambda"

  function_name = "external-task-service-8"
  s3_bucket = aws_s3_bucket.lambda.id
  s3_key = aws_s3_object.lambda_zip.id
  file_hash = local.file_hash
  topic_arn = aws_sns_topic.external_task.arn
  routing_key = "task8"
  tags = local.tags
  environment = var.environment
  alerts_enabled = var.alerts_enabled
}

module "service_9" {
  source = "./modules/sns-sqs-lambda"

  function_name = "external-task-service-9"
  s3_bucket = aws_s3_bucket.lambda.id
  s3_key = aws_s3_object.lambda_zip.id
  file_hash = local.file_hash
  topic_arn = aws_sns_topic.external_task.arn
  routing_key = "task9"
  tags = local.tags
  environment = var.environment
  alerts_enabled = var.alerts_enabled
}

resource "aws_sns_topic" "external_task" {
  name = local.topic
  tags = merge(local.tags, { Name : local.topic })
}

resource "aws_sqs_queue" "camunda_response_queue_dead" {
  name = local.dead_letter_queue
  tags = merge(local.tags, { Name : local.dead_letter_queue })
}

resource "aws_sqs_queue" "camunda_response_queue" {
  name = local.response_queue
  receive_wait_time_seconds = 20
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.camunda_response_queue_dead.arn
    maxReceiveCount = 3
  })
  tags = merge(local.tags, { Name : local.response_queue })
}

resource "aws_resourcegroups_group" "group" {
  name = "camunda-testing-group-${var.environment}"
  description = "camunda-testing app resources"
  resource_query {
    query = <<EOF
{
  "ResourceTypeFilters": ["AWS::AllSupported"],
  "TagFilters": [
    {
      "Key": "App",
      "Values": ["${local.app}"]
    }
  ]
}
EOF
  }
}

output "camunda_response_queue" {
  value = aws_sqs_queue.camunda_response_queue.id
}

output "external_task_topic" {
  value = aws_sns_topic.external_task.arn
}

output "random_suffix" {
  value = random_string.bucket_suffix.id
}
