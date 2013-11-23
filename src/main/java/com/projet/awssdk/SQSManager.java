package com.projet.awssdk;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class SQSManager {
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

    private String getQueueUrl(String topic) {
        return
                sqs.getQueueUrl(
                    new GetQueueUrlRequest().withQueueName(topic)
                ).getQueueUrl();
    }
}
