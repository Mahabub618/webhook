package com.github.mahabub618.kafkaboilerplate.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommitNotification(
        String repoName,
        String repoUrl,
        String branchName,
        String authorName,
        String avatarUrl,
        String commitMessage,
        String commitUrl,
        String shortHash,
        String source
) {
    @Override
    public String toString() {
        return String.format(
                "New commit in [%s] to %s (%s) by %s: %s [%s]",
                source, repoName, branchName, authorName, commitMessage, shortHash
        );
    }
}
