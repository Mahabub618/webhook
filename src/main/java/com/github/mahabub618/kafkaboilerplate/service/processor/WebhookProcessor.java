package com.github.mahabub618.kafkaboilerplate.service.processor;

import java.util.Optional;

public interface WebhookProcessor {
    Optional<Object> parseWebhookPayload(String payload);
}
