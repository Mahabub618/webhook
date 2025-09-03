package com.github.mahabub618.kafkaboilerplate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNotification(String topic, String key, Object event) {
        try {
            kafkaTemplate.send(topic, key, event);
            logger.debug("Sent notification to Kafka: {}", event);
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
