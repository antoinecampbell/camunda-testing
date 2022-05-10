const AWSXRay = require('aws-xray-sdk');
const {SQSClient, SendMessageCommand} = require('@aws-sdk/client-sqs');

const region = process.env['AWS_REGION'] || 'us-east-1'
const sqsClient = AWSXRay.captureAWSv3Client(new SQSClient({region}));

exports.handler = async function (event, context) {
  console.log('Event:', JSON.stringify(event));
  for (const sqsEvent of event.Records) {
    console.log('Input:', sqsEvent.body, context);
    const messageAttributes = sqsEvent.messageAttributes;
    const replyQueue = messageAttributes['reply-to'].stringValue;
    console.log('Reply-to:', replyQueue);

    try {
      const randomNumber = Math.floor(Math.random() * 10)
      let body = JSON.parse(sqsEvent.body);
      const outputName = body?.variables?.outputName || 'randomNumber'
      body = Object.assign(body, {variables: {}})
      body.variables[outputName] = randomNumber
      const sleepSegment = AWSXRay.getSegment().addNewSubsegment('sleep');
      await sleep(200)
      sleepSegment.close()
      if (randomNumber === 3) {
        throw new Error('The random number was 3 🤷‍')
      }

      const response = await sendResponse(replyQueue, body, messageAttributes, true);
      console.log('Response: ', response);
    } catch (e) {
      console.error(e);
      await sendResponse(replyQueue, {error: e.message, errorDescription: e.stack}, messageAttributes, false);
    }
  }
};

async function sendResponse(queueUrl, body, attributes, success) {
  return sqsClient.send(new SendMessageCommand({
    QueueUrl: queueUrl,
    MessageBody: JSON.stringify(body),
    MessageAttributes: {
      ...convertMessagesAttributes(attributes),
      success: {
        DataType: 'String',
        StringValue: `${success}`
      }
    }
  }));
}

function sleep(ms) {
  return new Promise((resolve) => {
    setTimeout(resolve, ms);
  });
}

function convertMessagesAttributes(attributes) {
  const convertedAttributes = {};
  Object.entries(attributes).forEach(([key, attribute]) => {
    convertedAttributes[key] = {
      DataType: attribute.dataType,
      StringValue: attribute.stringValue
    }
  });
  return convertedAttributes;
}
