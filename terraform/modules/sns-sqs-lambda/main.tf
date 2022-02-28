# Required variables
variable "function_name" {}
variable "topic_arn" {}
variable "routing_key" {}
variable "environment" {}
variable "s3_bucket" {}
variable "s3_key" {}
variable "file_hash" {}

variable "runtime" {
  default = "nodejs14.x"
}
variable "handler" {
  default = "index.handler"
}
variable "timeout" {
  default = 15
}
variable "memory_size" {
  default = 128
}
variable "reserved_concurrent_executions" {
  default = -1
}
variable "tags" {
  type = map(string)
  default = {}
}
variable "max_receive_count" {
  default = 2
}
variable "retention_in_days" {
  default = 7
}
variable "batch_size" {
  default = 10
}

locals {
  function_name = "${var.function_name}-${var.environment}"
  input_queue = "${var.function_name}-input"
  dead_letter_queue = "${var.function_name}-dead"
  log_group = "/aws/lambda/${local.function_name}"
}

resource "aws_lambda_function" "lambda" {
  function_name = local.function_name
  runtime = var.runtime
  role = aws_iam_role.lambda_exec.arn

  handler = var.handler
  s3_bucket = var.s3_bucket
  s3_key = var.s3_key
  source_code_hash = var.file_hash

  timeout = var.timeout
  memory_size = var.memory_size
  reserved_concurrent_executions = var.reserved_concurrent_executions
  tags = merge(var.tags, { Name : local.function_name })
}

resource "aws_sqs_queue" "dead" {
  name = local.dead_letter_queue
  tags = merge(var.tags, { Name : local.dead_letter_queue })
}

resource "aws_sqs_queue" "input" {
  name = local.input_queue
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dead.arn
    maxReceiveCount = var.max_receive_count
  })
  visibility_timeout_seconds = var.timeout * 2
  tags = merge(var.tags, { Name : local.input_queue })
}

resource "aws_sqs_queue_policy" "policy" {
  queue_url = aws_sqs_queue.input.id
  policy = jsonencode({
    Version : "2012-10-17"
    Statement : [
      {
        Sid : "allow-sns-send"
        Effect : "Allow"
        Principal : "*"
        Action : ["sqs:SendMessage"]
        Resource : aws_sqs_queue.input.arn
        Condition : {
          ArnEquals : {
            "aws:SourceArn" : var.topic_arn
          }
        }
      }
    ]
  })
}

resource "aws_cloudwatch_log_group" "log" {
  name = local.log_group
  retention_in_days = var.retention_in_days
  tags = merge(var.tags, { Name : local.log_group })
}

resource "aws_lambda_event_source_mapping" "lambda" {
  function_name = aws_lambda_function.lambda.function_name
  event_source_arn = aws_sqs_queue.input.arn
  batch_size = var.batch_size
}

resource "aws_sns_topic_subscription" "sqs" {
  topic_arn = var.topic_arn
  protocol = "sqs"
  endpoint = aws_sqs_queue.input.arn
  raw_message_delivery = true
  filter_policy = jsonencode({
    routing-key = [var.routing_key]
  })
}
