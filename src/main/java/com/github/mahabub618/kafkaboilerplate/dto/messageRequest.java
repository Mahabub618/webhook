package com.github.mahabub618.kafkaboilerplate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record messageRequest(@JsonProperty("message") String message,
                             @JsonProperty("identifier") int identifier) {
}
