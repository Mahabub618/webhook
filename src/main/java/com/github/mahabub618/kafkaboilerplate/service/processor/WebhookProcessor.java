package com.github.mahabub618.kafkaboilerplate.service.processor;

import com.github.mahabub618.kafkaboilerplate.dto.CommitNotification;

import java.util.Map;
import java.util.Optional;

public interface WebhookProcessor {
    Optional<CommitNotification> parseWebhookPayload(Map<String, Object> payload);
}
