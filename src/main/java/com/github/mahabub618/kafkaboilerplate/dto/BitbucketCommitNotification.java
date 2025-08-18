package com.github.mahabub618.kafkaboilerplate.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BitbucketCommitNotification(
        String repoName,
        String repoUrl,
        String branchName,
        String authorName,
        String avatarUrl,
        String commitMessage,
        String commitUrl,
        String shortHash
) {
    @Override
    public String toString() {
        return String.format(
                "New commit in %s (%s) by %s: %s [%s]",
                repoName, branchName, authorName, commitMessage, shortHash
        );
    }
}
