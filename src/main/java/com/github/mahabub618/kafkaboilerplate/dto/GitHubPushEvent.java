package com.github.mahabub618.kafkaboilerplate.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubPushEvent implements PushEvent {
    @Override
    public String getRepoName() {
        return repository != null ? repository.getFullName() : null;
    }

    @Override
    public String getRepoUrl() {
        return repository != null ? repository.getHtmlUrl() : null;
    }

    @Override
    public String getBranchName() {
        return ref != null ? ref.replace("refs/heads/", "") : null;
    }

    @Override
    public String getAuthorName() {
        String authorName = headCommit != null ? headCommit.getCommitter().getName() : "";
        String email = headCommit != null && headCommit.getCommitter() != null ? headCommit.getCommitter().getEmail() : null;
        if (email != null && !email.isEmpty()) {
            authorName += " <" + email + ">";
        }
        return authorName;
    }

    @Override
    public String getAvatarUrl() {
        return "";
    }

    @Override
    public Map<String, String> getCommits() {
        Map<String, String> commitMap = new HashMap<>();
        if (commits != null) {
            for (Commit commit : commits) {
                commitMap.put(commit.getId().substring(0, 7), commit.getMessage());
            }
        }
        return commitMap;
    }

    @Override
    public String getSource() {
        return "github";
    }

    @JsonProperty("ref")
    private String ref;

    @JsonProperty("repository")
    private Repository repository;

    @JsonProperty("commits")
    private List<Commit> commits;


    @JsonProperty("head_commit")
    private HeadCommit headCommit;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        @JsonProperty("name")
        private String fullName;

        @JsonProperty("html_url")
        private String htmlUrl;

        // Getters and setters
        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getHtmlUrl() {
            return htmlUrl;
        }

        public void setHtmlUrl(String htmlUrl) {
            this.htmlUrl = htmlUrl;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Commit {
        @JsonProperty("id")
        private String id;

        @JsonProperty("message")
        private String message;

        // Getters and setters

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    // Represents the head commit in the push event
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HeadCommit {
        @JsonProperty("committer")
        private Committer committer;

        // Getters and setters

        public Committer getCommitter() {
            return committer;
        }

        public void setCommitter(Committer committer) {
            this.committer = committer;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Committer {
        @JsonProperty("name")
        private String name;

        @JsonProperty("email")
        private String email;

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
