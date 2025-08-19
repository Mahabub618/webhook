package com.github.mahabub618.kafkaboilerplate.service.processor;

import com.github.mahabub618.kafkaboilerplate.dto.CommitNotification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class GithubWebhookProcessor implements WebhookProcessor {
    private static final Logger logger = LoggerFactory.getLogger(GithubWebhookProcessor.class);

    @Override
    public Optional<CommitNotification> parseWebhookPayload(Map<String, Object> payload) {
        try {
            // Extract repository information
            if (payload.get("repository") instanceof Map<?, ?> repo &&
                    payload.get("ref") instanceof String ref) {

                String repoName = getString(repo, "name");
                String repoUrl = getString(repo, "html_url");
                String branchName = ref.replace("refs/heads/", "");

                // Get the head commit (most recent commit in the push)
                if (payload.get("head_commit") instanceof Map<?, ?> headCommit) {
                    return parseCommit(headCommit, repoName, repoUrl, branchName);
                }
                else if (payload.get("commits") instanceof List<?> commits && !commits.isEmpty()) {
                    Object lastCommit = commits.get(commits.size() - 1);
                    if (lastCommit instanceof Map<?, ?>) {
                        return parseCommit((Map<?, ?>) lastCommit, repoName, repoUrl, branchName);
                    }
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.warn("Failed to parse GitHub webhook payload", e);
            return Optional.empty();
        }
    }
    private Optional<CommitNotification> parseCommit(Map<?, ?> commit,
                                                     String repoName,
                                                     String repoUrl,
                                                     String branchName) {
        String hash = getString(commit, "id");
        String message = getString(commit, "message").trim();
        String commitUrl = getString(commit, "url");
        String authorName = "";
        String avatarUrl = null;

        if (commit.get("committer") instanceof Map<?, ?> author) {
            authorName = getString(author, "name");
            // If email is available, add with authorname like <email>
            authorName = authorName.isEmpty() ? authorName : authorName + " <" + getString(author, "email") + ">";
            avatarUrl = getAvatarUrl(author);
        }

        // Fallback to pusher/sender if author not available
        if (authorName.isEmpty()) {
            authorName = getString(commit, "username");
        }

        CommitNotification notification = new CommitNotification(
                repoName,
                repoUrl,
                branchName,
                authorName,
                avatarUrl,
                message,
                commitUrl,
                hash.substring(0, 7),
                "github"
        );

        logger.info("""
        🚀 Successfully parsed Github webhook:
        ┌───────────────────────────────────────
        │ Source: GitHub
        | Repository: {}
        │ Branch: {}
        │ Commit: {} by {}
        │ Message: {}
        │ Avatar: {}
        │ URL: {}
        └───────────────────────────────────────""",
                repoName, branchName,
                hash.substring(0, 7), authorName,
                message, avatarUrl, commitUrl);

        return Optional.of(notification);
    }

    private String getAvatarUrl(Map<?, ?> userMap) {
        if (userMap.containsKey("avatar_url")) {
            return getString(userMap, "avatar_url");
        }
        return null;
    }

    private String getString(Map<?, ?> map, String key) {
        return map.get(key) instanceof String s ? s : "";
    }
}
