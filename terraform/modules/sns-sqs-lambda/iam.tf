resource "aws_iam_role" "lambda_exec" {
  name = "lambda-vpc-sqs-${local.function_name}"
  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow"
    }
  ]
}
EOF
}

resource "aws_iam_policy" "lambda_sqs" {
  name = "lambda-sqs-${var.function_name}"
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid" : "ProcessMessage",
      "Effect": "Allow",
      "Action": [
        "sqs:ReceiveMessage",
        "sqs:DeleteMessage",
        "sqs:GetQueueAttributes"
      ],
      "Resource": "${aws_sqs_queue.input.arn}"
    },
    {
      "Sid" : "SendMessage",
      "Effect": "Allow",
      "Action": [
        "sqs:SendMessage*"
      ],
      "Resource": "*"
    }
  ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "lambda_sqs" {
  role = aws_iam_role.lambda_exec.name
  policy_arn = aws_iam_policy.lambda_sqs.arn
}

resource "aws_iam_role_policy_attachment" "lambda" {
  role = aws_iam_role.lambda_exec.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"
}
