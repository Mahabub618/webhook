package com.github.mahabub618.kafkaboilerplate.service;

import com.github.mahabub618.kafkaboilerplate.service.processor.WebhookProcessor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WebhookService {
    private final Map<String, WebhookProcessor> processors;
    private final KafkaProducerService kafkaProducer;

    // Spring will inject beans keyed by bean name into this map
    public WebhookService(Map<String, WebhookProcessor> processors,
                          KafkaProducerService kafkaProducer) {
        this.processors = processors;
        this.kafkaProducer = kafkaProducer;
    }

    public void processWebhook(String source, String payload, String topic) {
        if (source == null) return;
        WebhookProcessor processor = processors.get(source.toLowerCase());
        if (processor != null) {
            processor.parseWebhookPayload(payload)
                    .ifPresent(event -> kafkaProducer.sendNotification(topic, source, event));
        } else {
            // optional: log unknown source
        }
    }
}
