package com.github.mahabub618.kafkaboilerplate.dto;

import java.util.Map;

public interface PushEvent {
    String getRepoName();
    String getRepoUrl();
    String getBranchName();
    String getAuthorName();
    String getAvatarUrl();
    Map<String, CommitInfo> getCommits();
    String getSource();

    class CommitInfo {
        private String message;
        private String commitUrl;

        public CommitInfo(String message, String commitUrl) {
            this.message = message;
            this.commitUrl = commitUrl;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getCommitUrl() {
            return commitUrl;
        }

        public void setCommitUrl(String commitUrl) {
            this.commitUrl = commitUrl;
        }
    }
}
