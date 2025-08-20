package com.github.mahabub618.kafkaboilerplate.service.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mahabub618.kafkaboilerplate.dto.GitHubPushEvent;
import com.github.mahabub618.kafkaboilerplate.dto.PushEventResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class GithubWebhookProcessor implements WebhookProcessor {
    private static final Logger logger = LoggerFactory.getLogger(GithubWebhookProcessor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Optional<PushEventResponse> parseWebhookPayload(String payload) {
        try {

            GitHubPushEvent gitHubPushEvent = objectMapper.readValue(payload, GitHubPushEvent.class);
            PushEventResponse pushEventResponse = new PushEventResponse(gitHubPushEvent);

            if (pushEventResponse.getCommits() != null && !pushEventResponse.getCommits().isEmpty()) {
                return Optional.of(pushEventResponse);
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.warn("Failed to parse GitHub webhook payload", e);
            return Optional.empty();
        }
    }
}
