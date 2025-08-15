package com.github.mahabub618.kafkaboilerplate.event;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.MessageEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;



public class WikimediaEvent implements EventHandler {
    private static final Logger logger =
            LoggerFactory.getLogger(WikimediaEvent.class);
    private final KafkaTemplate<String, Object> template;
    private final String topicName;

    public WikimediaEvent(KafkaTemplate<String, Object> template, String topicName) {
        this.template = template;
        this.topicName = topicName;
    }

    @Override
    public void onOpen() {
        // nothing here
    }

    @Override
    public void onClosed() {

    }

    @Override
    public void onMessage(String event, MessageEvent messageEvent) {
        logger.info(messageEvent.getData());
        // asynchronous
        template.send(new ProducerRecord<>(topicName, messageEvent.getData()));
    }

    @Override
    public void onComment(String comment) {
        // nothing here
    }

    @Override
    public void onError(Throwable t) {
        logger.error("Error in Stream Reading", t);
    }
}
