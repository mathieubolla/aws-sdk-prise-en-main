package com.projet.awssdk;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;

public class SQSManager {
    private static final int TIME_PER_CALL_SECONDS = 20;
    private static final int TOTAL_TIME_WAITED_SECONDS = 5 * 60;
    private static final int MAX_RETRIES = TOTAL_TIME_WAITED_SECONDS / TIME_PER_CALL_SECONDS;

    private final AmazonSQS sqs;

    public SQSManager(AmazonSQS sqs) {
        this.sqs = sqs;
    }

    public void sendMessage(String message, String topic) {
        sqs.sendMessage(
                new SendMessageRequest()
                        .withMessageBody(message)
                        .withQueueUrl(getQueueUrl(topic))
        );
    }

    public boolean processInTransaction(TransactionalProcess process,
                                     String topic) {
        return processInTransaction(process, topic, MAX_RETRIES);
    }

    private boolean processInTransaction(TransactionalProcess process,
                                         String topic, int remainingRetries) {
        if (remainingRetries < 0) {
            return false;
        }

        ReceiveMessageResult receiveMessageResult = sqs.receiveMessage(
                new ReceiveMessageRequest()
                        .withQueueUrl(getQueueUrl(topic))
                        .withMaxNumberOfMessages(1)
                        .withWaitTimeSeconds(TIME_PER_CALL_SECONDS)
        );

        if (receiveMessageResult.getMessages().size() != 1) {
            return processInTransaction(process, topic, remainingRetries - 1);
        }

        Message message = receiveMessageResult.getMessages().get(0);

        try {
            process.process(message.getBody());

            sqs.deleteMessage(new DeleteMessageRequest()
                    .withQueueUrl(getQueueUrl(topic))
                    .withReceiptHandle(message.getReceiptHandle()));
        } catch (Exception e) {
            // Nothing to do: SQS will resend on its own
            e.printStackTrace();
        }

        return true;
    }

    private String getQueueUrl(String topic) {
        return
                sqs.getQueueUrl(
                        new GetQueueUrlRequest().withQueueName(topic)
                ).getQueueUrl();
    }

    public String create(String topic) {
        if (!exists(topic)) {
            sqs.createQueue(new CreateQueueRequest()
                    .withQueueName(topic)
            );
        }

        return sqs.getQueueAttributes(
                new GetQueueAttributesRequest()
                        .withAttributeNames("QueueArn")
                        .withQueueUrl(getQueueUrl(topic)))
                .getAttributes()
                .get("QueueArn");
    }

    private boolean exists(String topic) {
        return sqs.listQueues(
                new ListQueuesRequest()
                        .withQueueNamePrefix(topic))
                .getQueueUrls()
                .contains(topic);
    }

    public static interface TransactionalProcess {
        void process(String input);
    }
}
