package com.github.mahabub618.kafkaboilerplate.service.processor;
import com.github.mahabub618.kafkaboilerplate.dto.PushEventResponse;

import java.util.Optional;

public interface WebhookProcessor {
    Optional<PushEventResponse> parseWebhookPayload(String payload);
}
