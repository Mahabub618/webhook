package com.github.mahabub618.kafkaboilerplate.service.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mahabub618.kafkaboilerplate.dto.BitbucketPushEvent;
import com.github.mahabub618.kafkaboilerplate.dto.PushEventResponse;
import com.github.mahabub618.kafkaboilerplate.service.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BitbucketWebhookProcessor implements WebhookProcessor {
    private static final Logger logger = LoggerFactory.getLogger(BitbucketWebhookProcessor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Optional<PushEventResponse> parseWebhookPayload(String payload) {
        try {
            BitbucketPushEvent bitbucketPushEvent = objectMapper.readValue(payload, BitbucketPushEvent.class);
            PushEventResponse pushEventResponse = new PushEventResponse(bitbucketPushEvent);

            if (pushEventResponse.getCommits() != null && !pushEventResponse.getCommits().isEmpty()) {
                return Optional.of(pushEventResponse);
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.warn("Failed to parse webhook payload", e);
            return Optional.empty();
        }
    }
}
