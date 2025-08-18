package com.github.mahabub618.kafkaboilerplate.controller;

import com.github.mahabub618.kafkaboilerplate.service.BitbucketWebhookService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class BitbucketWebhookController {
    private final BitbucketWebhookService webhookService;
    private final String topicName;

    public BitbucketWebhookController(BitbucketWebhookService webhookService,
                                      @Value("${bitbucket.topic-name}") String topicName) {
        this.webhookService = webhookService;
        this.topicName = topicName;
    }

    @PostMapping("/webhooks/bitbucket")
    public ResponseEntity<Void> handleBitbucketWebhook(@RequestBody Map<String, Object> payload) {
        webhookService.processWebhookPayload(payload, topicName);
        return ResponseEntity.ok().build();
    }
}
