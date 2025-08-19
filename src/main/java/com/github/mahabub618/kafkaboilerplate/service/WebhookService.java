package com.github.mahabub618.kafkaboilerplate.service;

import com.github.mahabub618.kafkaboilerplate.service.processor.WebhookProcessor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class WebhookService {
    private final Map<String, WebhookProcessor> processors;
    private final KafkaProducerService kafkaProducer;

    public WebhookService(List<WebhookProcessor> processorList,
                          KafkaProducerService kafkaProducer) {
        this.processors = processorList.stream()
                .collect(Collectors.toMap(
                        p -> p.getClass().getSimpleName().replace("WebhookProcessor", "").toLowerCase(),
                        Function.identity()
                ));
        this.kafkaProducer = kafkaProducer;
    }

    public void processWebhook(String source, Map<String, Object> payload, String topic) {
        WebhookProcessor processor = processors.get(source.toLowerCase());
        if (processor != null) {
            processor.parseWebhookPayload(payload)
                    .ifPresent(notification -> kafkaProducer.sendNotification(topic, notification));
        }
    }
}
