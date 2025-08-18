package com.github.mahabub618.kafkaboilerplate.util;

import com.github.mahabub618.kafkaboilerplate.dto.BitbucketCommitNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class WebhookPayloadParser {
    private static final Logger logger = LoggerFactory.getLogger(WebhookPayloadParser.class);

    public Optional<BitbucketCommitNotification> parsePayload(Map<String, Object> payload) {
        try {
            if (payload.get("repository") instanceof Map<?, ?> repo &&
                    payload.get("push") instanceof Map<?, ?> push &&
                    push.get("changes") instanceof List<?> changes) {

                String repoName = getString(repo, "name");
                String repoUrl = getNestedString(repo, "links", "html", "href");

                for (Object changeObj : changes) {
                    if (changeObj instanceof Map<?, ?> change &&
                            change.get("new") instanceof Map<?, ?> newObj &&
                            change.get("commits") instanceof List<?> commits) {

                        String branchName = getString(newObj, "name");

                        for (Object commitObj : commits) {
                            if (commitObj instanceof Map<?, ?> commit) {
                                return parseCommit(commit, repoName, repoUrl, branchName);
                            }
                        }
                    }
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.warn("Failed to parse webhook payload", e);
            return Optional.empty();
        }
    }

    private Optional<BitbucketCommitNotification> parseCommit(Map<?, ?> commit,
                                                              String repoName,
                                                              String repoUrl,
                                                              String branchName) {
        String hash = getString(commit, "hash");
        String message = getString(commit, "message").trim();
        String commitUrl = getNestedString(commit, "links", "html", "href");

        String authorName = "";
        String avatarUrl = null;
        if (commit.get("author") instanceof Map<?, ?> author) {
            authorName = getString(author, "raw");
            avatarUrl = getNestedString(author, "user", "links", "avatar", "href");
        }

        BitbucketCommitNotification notification = new BitbucketCommitNotification(
                repoName,
                repoUrl,
                branchName,
                authorName,
                avatarUrl,
                message,
                commitUrl,
                hash.substring(0, 7)
        );

        // Detailed logging of parsed data
        logger.info("""
        🚀 Successfully parsed Bitbucket webhook:
        ┌───────────────────────────────────────
        │ Repository: {}
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

    private String getString(Map<?, ?> map, String key) {
        return map.get(key) instanceof String s ? s : "";
    }

    private String getNestedString(Map<?, ?> map, String... keys) {
        Map<?, ?> current = map;
        for (int i = 0; i < keys.length - 1; i++) {
            Object next = current.get(keys[i]);
            if (!(next instanceof Map<?, ?>)) return null;
            current = (Map<?, ?>) next;
        }
        return current.get(keys[keys.length - 1]) instanceof String s ? s : null;
    }
}
