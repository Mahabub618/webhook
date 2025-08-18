package com.github.mahabub618.kafkaboilerplate.exception;

public class WebhookProcessingException extends RuntimeException {
    public WebhookProcessingException(String message) {
        super(message);
    }    public WebhookProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
