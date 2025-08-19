package com.github.mahabub618.kafkaboilerplate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class PushEventResponse {
    private String source;
    private String repoName;
    private String repoUrl;
    private String branchName;
    private String authorName;
    private String avatarUrl;
    private Map<String, String> commits;

    public PushEventResponse(GitHubPushEvent pushEvent) {
        this.source = pushEvent.getSource();
        this.repoName = pushEvent.getRepoName();
        this.repoUrl = pushEvent.getRepoUrl();
        this.branchName = pushEvent.getBranchName();
        this.authorName = pushEvent.getAuthorName();
        this.avatarUrl = pushEvent.getAvatarUrl();
        this.commits = pushEvent.getCommits();
    }

    // Getters with @JsonProperty annotations
    @JsonProperty("source")
    public String getSource() { return source; }

    @JsonProperty("repoName")
    public String getRepoName() { return repoName; }

    @JsonProperty("repoUrl")
    public String getRepoUrl() { return repoUrl; }

    @JsonProperty("branchName")
    public String getBranchName() { return branchName; }

    @JsonProperty("authorName")
    public String getAuthorName() { return authorName; }

    @JsonProperty("avatarUrl")
    public String getAvatarUrl() { return avatarUrl; }

    @JsonProperty("commits")
    public Map<String, String> getCommits() { return commits; }
}
