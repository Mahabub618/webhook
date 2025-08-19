package com.github.mahabub618.kafkaboilerplate.controller;

import com.github.mahabub618.kafkaboilerplate.service.WebhookService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {
    private final WebhookService webhookService;
    private final String topicName;

    public WebhookController(WebhookService webhookService,
                             @Value("${webhhok.topic}") String topicName) {
        this.webhookService = webhookService;
        this.topicName = topicName;
    }

    @PostMapping("/bitbucket")
    public ResponseEntity<Void> handleBitbucket(@RequestBody Map<String, Object> payload) {
//        webhookService.processWebhook("bitbucket", payload, topicName);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/github")
    public ResponseEntity<Void> handleGithub(@RequestBody String payload) {
        webhookService.processWebhook("github", payload, topicName);
        return ResponseEntity.ok().build();
    }
}
