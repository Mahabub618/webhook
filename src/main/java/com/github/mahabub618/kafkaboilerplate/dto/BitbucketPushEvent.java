package com.github.mahabub618.kafkaboilerplate.dto;

import java.util.Map;

public class BitbucketPushEvent implements PushEvent {
    @Override
    public String getRepoName() {
        return "";
    }

    @Override
    public String getRepoUrl() {
        return "";
    }

    @Override
    public String getBranchName() {
        return "";
    }

    @Override
    public String getAuthorName() {
        return "";
    }

    @Override
    public String getAvatarUrl() {
        return "";
    }

    @Override
    public Map<String, String> getCommits() {
        return Map.of();
    }

    @Override
    public String getSource() {
        return "";
    }
}
