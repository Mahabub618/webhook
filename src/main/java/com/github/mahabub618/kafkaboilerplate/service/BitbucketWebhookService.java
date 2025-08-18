package com.github.mahabub618.kafkaboilerplate.service;

import com.github.mahabub618.kafkaboilerplate.dto.BitbucketCommitNotification;
import com.github.mahabub618.kafkaboilerplate.util.WebhookPayloadParser;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Service
public class BitbucketWebhookService {
    private final WebhookPayloadParser payloadParser;
    private final KafkaProducerService kafkaProducer;

    public BitbucketWebhookService(WebhookPayloadParser payloadParser,
                                   KafkaProducerService kafkaProducer) {
        this.payloadParser = payloadParser;
        this.kafkaProducer = kafkaProducer;
    }

    public void processWebhookPayload(Map<String, Object> payload, String topic) {
        Optional<BitbucketCommitNotification> notification = payloadParser.parsePayload(payload);
        notification.ifPresent(n -> kafkaProducer.sendNotification(topic, n));
    }
}
