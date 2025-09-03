package com.github.mahabub618.kafkaboilerplate.config;

import com.github.mahabub618.kafkaboilerplate.service.processor.RuleBasedWebhookProcessor;
import com.github.mahabub618.kafkaboilerplate.service.processor.WebhookProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class WebhookProcessorConfig {
    @Bean("github")
    public WebhookProcessor githubProcessor() throws Exception {
        ClassPathResource r = new ClassPathResource("rules/github.json");
        return new RuleBasedWebhookProcessor("github", r.getInputStream());
    }

    @Bean("bitbucket")
    public WebhookProcessor bitbucketProcessor() throws Exception {
        ClassPathResource r = new ClassPathResource("rules/bitbucket.json");
        return new RuleBasedWebhookProcessor("bitbucket", r.getInputStream());
    }
}
