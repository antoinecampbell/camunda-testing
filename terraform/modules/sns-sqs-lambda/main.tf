# Required variables
variable "function_name" {}
variable "role" {}
variable "zip_location" {}
variable "topic_arn" {}
variable "routing_key" {}

variable "runtime" {
  default = "nodejs12.x"
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

resource "aws_lambda_function" "lambda" {
  function_name = var.function_name
  runtime = var.runtime
  role = var.role

  handler = var.handler
  filename = var.zip_location
  source_code_hash = filebase64sha256(var.zip_location)

  timeout = var.timeout
  memory_size = var.memory_size
  reserved_concurrent_executions = var.reserved_concurrent_executions
  tags = var.tags
}

resource "aws_sqs_queue" "dead" {
  name = "${var.function_name}-dead"
  tags = var.tags
}

resource "aws_sqs_queue" "input" {
  name = "${var.function_name}-input"
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dead.arn
    maxReceiveCount = var.max_receive_count
  })
  visibility_timeout_seconds = var.timeout * 2
  tags = var.tags
}

resource "aws_sqs_queue_policy" "policy" {
  queue_url = aws_sqs_queue.input.id
  policy = jsonencode({
    Version: "2012-10-17"
    Statement: [{
      Sid: "allow-sns-send"
      Effect: "Allow"
      Principal: "*"
      Action: ["sqs:SendMessage"]
      Resource: aws_sqs_queue.input.arn
      Condition: {
        ArnEquals: {
          "aws:SourceArn": var.topic_arn
        }
      }
    }]
  })
}

resource "aws_cloudwatch_log_group" "log" {
  name = "/aws/lambda/${var.function_name}"
  retention_in_days = var.retention_in_days
  tags = var.tags
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