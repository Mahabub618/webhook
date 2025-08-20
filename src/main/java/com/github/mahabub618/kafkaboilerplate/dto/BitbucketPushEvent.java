package com.github.mahabub618.kafkaboilerplate.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BitbucketPushEvent implements PushEvent {
    @Override
    public String getRepoName() {
        return repository != null ? repository.getName() : null;
    }

    @Override
    public String getRepoUrl() {
        return repository != null ? repository.getLinks().getHtml().getHref() : null;
    }

    @Override
    public String getBranchName() {
        return push.getChanges().stream()
                .findFirst()
                .map(change -> change.getNewBranch().getName())
                .orElse(null);
    }

    @Override
    public String getAuthorName() {
        return push.getChanges().stream()
                .findFirst()
                .flatMap(change -> change.getCommits().stream().findFirst())
                .map(commit -> commit.getAuthor().getRaw())
                .orElse(null);
    }

    @Override
    public String getAvatarUrl() {
        return push.getChanges().stream()
                .findFirst()
                .flatMap(change -> change.getCommits().stream().findFirst())
                .map(commit -> commit.getAuthor().getUser().getLinks().getAvatar().getHref())
                .orElse(null);
    }

    @Override
    public Map<String, CommitInfo> getCommits() {
        Map<String, CommitInfo> commitMap = new HashMap<>();
        if (push != null && push.getChanges() != null) {
            for (Change change : push.getChanges()) {
                if (change.getCommits() != null) {
                    for (Commit commit : change.getCommits()) {
                        String shortHash = commit.getHash().substring(0, 7);
                        String cleanMessage = commit.getMessage() != null
                                ? commit.getMessage().trim()
                                : "";
                        String commitUrl = commit.getLinks() != null && commit.getLinks().getHtml() != null
                                ? commit.getLinks().getHtml().getHref()
                                : null;
                        commitMap.put(shortHash, new CommitInfo(cleanMessage, commitUrl));
                    }
                }
            }
        }
        return commitMap;
    }

    @Override
    public String getSource() {
        return "bitbucket";
    }

    @JsonProperty("push")
    private Push push;

    @JsonProperty("repository")
    private Repository repository;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Push {
        @JsonProperty("changes")
        private List<Change> changes;

        public List<Change> getChanges() {
            return changes;
        }

        public void setChanges(List<Change> changes) {
            this.changes = changes;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Change {
        @JsonProperty("new")
        private Branch newBranch;

        @JsonProperty("commits")
        private List<Commit> commits;

        // Getters and setters

        public Branch getNewBranch() {
            return newBranch;
        }

        public void setNewBranch(Branch newBranch) {
            this.newBranch = newBranch;
        }

        public List<Commit> getCommits() {
            return commits;
        }

        public void setCommits(List<Commit> commits) {
            this.commits = commits;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Branch {
        @JsonProperty("name")
        private String name;

        // Getters and setters

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Commit {
        @JsonProperty("hash")
        private String hash;

        @JsonProperty("message")
        private String message;

        @JsonProperty("author")
        private Author author;

        @JsonProperty("links")
        private Links links;

        // Getters and setters

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Author getAuthor() {
            return author;
        }

        public void setAuthor(Author author) {
            this.author = author;
        }

        public Links getLinks() {
            return links;
        }

        public void setLinks(Links links) {
            this.links = links;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {
        @JsonProperty("raw")
        private String raw;

        @JsonProperty("user")
        private User user;

        // Getters and setters

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public String getRaw() {
            return raw;
        }

        public void setRaw(String raw) {
            this.raw = raw;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        @JsonProperty("links")
        private UserLinks links;

        // Getters and setters

        public UserLinks getLinks() {
            return links;
        }

        public void setLinks(UserLinks links) {
            this.links = links;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserLinks {
        @JsonProperty("avatar")
        private Avatar avatar;

        // Getters and setters

        public Avatar getAvatar() {
            return avatar;
        }

        public void setAvatar(Avatar avatar) {
            this.avatar = avatar;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Avatar {
        @JsonProperty("href")
        private String href;

        // Getters and setters

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        @JsonProperty("name")
        private String name;

        @JsonProperty("links")
        private Links links;

        // Getters and setters

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Links getLinks() {
            return links;
        }

        public void setLinks(Links links) {
            this.links = links;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Links {
        @JsonProperty("html")
        private Html html;

        // Getters and setters

        public Html getHtml() {
            return html;
        }

        public void setHtml(Html html) {
            this.html = html;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Html {
        @JsonProperty("href")
        private String href;

        // Getters and setters

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }
    }
}
