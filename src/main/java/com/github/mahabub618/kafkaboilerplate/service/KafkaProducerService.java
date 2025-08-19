package com.github.mahabub618.kafkaboilerplate.service;

import com.github.mahabub618.kafkaboilerplate.dto.CommitNotification;
import com.github.mahabub618.kafkaboilerplate.dto.PushEvent;
import com.github.mahabub618.kafkaboilerplate.dto.PushEventResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, PushEventResponse> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, PushEventResponse> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNotification(String topic, PushEventResponse event) {
        try {
            kafkaTemplate.send(topic, event.getRepoName(), event);
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
