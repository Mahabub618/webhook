package com.github.mahabub618.kafkaboilerplate.dto;

import java.util.Map;

public interface PushEvent {
    String getRepoName();
    String getRepoUrl();
    String getBranchName();
    String getAuthorName();
    String getAvatarUrl();
    Map<String, String> getCommits();
    String getSource();
}
