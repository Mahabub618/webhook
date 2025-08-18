package com.github.mahabub618.kafkaboilerplate.controller;

import com.github.mahabub618.kafkaboilerplate.dto.messageRequest;
import com.github.mahabub618.kafkaboilerplate.event.WikimediaEvent;
import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@RestController
public class KafkaController {
    private static final Logger logger =
            LoggerFactory.getLogger(KafkaController.class);

    private final KafkaTemplate<String, Object> template;
    private final String topicName;
    private final String wikiTopic;
    private final int messagesPerRequest;
    private final String wikiUrl;
    private CountDownLatch latch;

    public KafkaController(
            final KafkaTemplate<String, Object> template,
            @Value("${tpd.topic-name}") final String topicName,
            @Value("${wiki.topic-name}") final  String wikiTopic,
            @Value("${tpd.messages-per-request}") final int messagesPerRequest,
            @Value("${wiki.url}") final String wikiUrl) {
        this.template = template;
        this.topicName = topicName;
        this.wikiTopic = wikiTopic;
        this.messagesPerRequest = messagesPerRequest;
        this.wikiUrl = wikiUrl;
    }

    @GetMapping("/hello")
    public String hello() throws Exception {
        latch = new CountDownLatch(messagesPerRequest);
        IntStream.range(0, messagesPerRequest)
                .forEach(i -> this.template.send(topicName, String.valueOf(i),
                        new messageRequest("A Practical Advice", i))
                );
        latch.await(60, TimeUnit.SECONDS);
        logger.info("All messages received");
        return "Hello Kafka!";
    }

    @GetMapping("/wikimedia")
    public String wikimedia() throws Exception {
        EventHandler eventHandler = new WikimediaEvent(this.template, this.wikiTopic);
        EventSource.Builder builder = new EventSource.Builder(eventHandler, URI.create(this.wikiUrl));
        EventSource eventSource = builder.build();

        // start the producer in another thred
        eventSource.start();

        // we produce for 5 seconds and block the program until then
        TimeUnit.SECONDS.sleep(5);

        return "Wikimedia Event";
    }

    @PostMapping("/webhooks/bitbucket")
    public ResponseEntity<Void> handleBitbucketWebhook(@RequestBody Map<String, Object> payload) {
        try {
            // Extract repository information
            Map<String, Object> repository = (Map<String, Object>) payload.get("repository");
            String repoName = (String) repository.get("name");
            // Properly cast nested maps for links
            Map<String, Object> repoLinks = (Map<String, Object>) repository.get("links");
            Map<String, Object> repoHtmlLink = (Map<String, Object>) repoLinks.get("html");
            String repoUrl = (String) repoHtmlLink.get("href");

            // Extract push information
            Map<String, Object> push = (Map<String, Object>) payload.get("push");
            List<Map<String, Object>> changes = (List<Map<String, Object>>) push.get("changes");

            for (Map<String, Object> change : changes) {
                Map<String, Object> newObj = (Map<String, Object>) change.get("new");
                String branchName = (String) newObj.get("name");
                String branchType = (String) newObj.get("type");

                List<Map<String, Object>> commits = (List<Map<String, Object>>) change.get("commits");

                for (Map<String, Object> commit : commits) {
                    // Extract commit information
                    String hash = (String) commit.get("hash");
                    String message = (String) commit.get("message");

                    Map<String, Object> author = (Map<String, Object>) commit.get("author");
                    String authorName = (String) author.get("raw");

                    // Extract avatar URL
                    String avatarUrl = "";
                    if (author.containsKey("user")) {
                        Map<String, Object> user = (Map<String, Object>) author.get("user");
                        if (user.containsKey("links")) {
                            Map<String, Object> userLinks = (Map<String, Object>) user.get("links");
                            if (userLinks.containsKey("avatar")) {
                                avatarUrl = (String) ((Map<String, Object>) userLinks.get("avatar")).get("href");
                            }
                        }
                    }

                    Map<String, Object> links = (Map<String, Object>) commit.get("links");
                    String commitUrl = (String) ((Map<String, Object>) links.get("html")).get("href");

                    // Create a formatted message with avatar
                    String formattedMessage = String.format(
                            "{\n" +
                                    "  \"repo\": \"%s\",\n" +
                                    "  \"repoUrl\": \"%s\",\n" +
                                    "  \"branch\": \"%s\",\n" +
                                    "  \"author\": \"%s\",\n" +
                                    "  \"avatar\": \"%s\",\n" +
                                    "  \"message\": \"%s\",\n" +
                                    "  \"commitUrl\": \"%s\",\n" +
                                    "  \"shortHash\": \"%s\"\n" +
                                    "}",
                            repoName, repoUrl, branchName,
                            authorName, avatarUrl,
                            message.trim().replace("\"", "'"),  // Escape quotes in message
                            commitUrl, hash.substring(0, 7)
                    );

                    // Send to Kafka (or wherever you want to send it)
//                    this.template.send(topicName, "bitbucket-webhook", formattedMessage);

                    logger.info("Processed Bitbucket commit with avatar: {}", formattedMessage);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing Bitbucket webhook", e);
        }
        return ResponseEntity.ok().build();
    }

    @KafkaListener(topics = "advice-topic", clientIdPrefix = "json",
            containerFactory = "kafkaListenerContainerFactory")
    public void listenAsObject(ConsumerRecord<String, messageRequest> cr,
                               @Payload messageRequest payload) {
        logger.info("Logger 1 [JSON] received key {}: Type [{}] | Payload: {} | Record: {}", cr.key(),
                typeIdHeader(cr.headers()), payload, cr.toString());
        latch.countDown();
    }

    @KafkaListener(topics = "advice-topic", clientIdPrefix = "string",
            containerFactory = "kafkaListenerStringContainerFactory")
    public void listenasString(ConsumerRecord<String, String> cr,
                               @Payload String payload) {
        logger.info("Logger 2 [String] received key {}: Type [{}] | Payload: {} | Record: {}", cr.key(),
                typeIdHeader(cr.headers()), payload, cr.toString());
        latch.countDown();
    }

    @KafkaListener(topics = "advice-topic", clientIdPrefix = "bytearray",
            containerFactory = "kafkaListenerByteArrayContainerFactory")
    public void listenAsByteArray(ConsumerRecord<String, byte[]> cr,
                                  @Payload byte[] payload) {
        logger.info("Logger 3 [ByteArray] received key {}: Type [{}] | Payload: {} | Record: {}", cr.key(),
                typeIdHeader(cr.headers()), payload, cr.toString());
        latch.countDown();
    }

    private static String typeIdHeader(Headers headers) {
        return StreamSupport.stream(headers.spliterator(), false)
                .filter(header -> header.key().equals("__TypeId__"))
                .findFirst().map(header -> new String(header.value())).orElse("N/A");
    }
}
