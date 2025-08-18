package com.github.mahabub618.kafkaboilerplate.service;

import com.github.mahabub618.kafkaboilerplate.dto.BitbucketCommitNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, BitbucketCommitNotification> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, BitbucketCommitNotification> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNotification(String topic, BitbucketCommitNotification notification) {
        try {
            kafkaTemplate.send(topic, notification.repoName(), notification);
            logger.debug("Sent notification to Kafka: {}", notification);
        } catch (Exception e) {
            logger.error("Failed to send notification to Kafka", e);
            throw new KafkaProducerException("Failed to send Kafka message", e);
        }
    }

    public static class KafkaProducerException extends RuntimeException {
        public KafkaProducerException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
