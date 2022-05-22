const AWSXRay = require('aws-xray-sdk');
const {SQSClient, SendMessageCommand} = require('@aws-sdk/client-sqs');

const region = process.env['AWS_REGION'] || 'us-east-1'
const sqsClient = AWSXRay.captureAWSv3Client(new SQSClient({region}));

exports.handler = async function (event, context) {
  console.log('Event:', JSON.stringify(event));
  for (const sqsEvent of event.Records) {
    // Log input and body
    console.log('Body:', sqsEvent.body);
    const messageAttributes = sqsEvent.messageAttributes;
    const replyQueue = messageAttributes['reply-to'].stringValue;

    try {
      // Add a random number to the output
      const randomNumber = Math.floor(Math.random() * 10)
      let body = JSON.parse(sqsEvent.body);
      const outputName = body?.variables?.outputName || 'randomNumber'
      body = Object.assign(body, {variables: {}})
      body.variables[outputName] = randomNumber

      // Sleep
      const sleepSegment = AWSXRay.getSegment().addNewSubsegment('sleep');
      await sleep(200)
      sleepSegment.close()

      // Throw  an error id the random number is 3
      if (randomNumber === 3) {
        throw new Error('The random number was 3 🤷‍')
      }

      // Send success response to the reply-to queue
      const response = await sendResponse(replyQueue, body, messageAttributes, true);
      console.log('Response: ', response);
    } catch (e) {
      // Send error response to the reply-to queue
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
