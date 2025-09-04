package com.github.mahabub618.kafkaboilerplate.service.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuleBasedWebhookProcessor implements WebhookProcessor {
    private static final Logger logger = LoggerFactory.getLogger(RuleBasedWebhookProcessor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([^}]+)\\}");

    private final Map<String, Object> rules;
    private final String source;

    public RuleBasedWebhookProcessor(String source, InputStream ruleJsonStream) throws Exception {
        this.source = source;
        if (ruleJsonStream == null) {
            throw new IllegalArgumentException("ruleJsonStream for " + source + " is null");
        }
        this.rules = objectMapper.readValue(ruleJsonStream, Map.class);
    }

    @Override
    public Optional<Object> parseWebhookPayload(String payload) {
        try {
            Object json = Configuration.defaultConfiguration().jsonProvider().parse(payload);

            // Build root object first for simple (non-commits) fields
            ObjectNode root = objectMapper.createObjectNode();
            for (Map.Entry<String, Object> e : rules.entrySet()) {
                String key = e.getKey();
                Object ruleVal = e.getValue();

                if ("commits".equals(key)) {
                    // skip commits for now
                    continue;
                }

                // This condition for github json to concate author name with email
                // like "Mahabub <mahabub.rahman@tigerit.com>"
                if (ruleVal instanceof Map) {
                    Map<String, Object> maybeMap = (Map<String, Object>) ruleVal;
                    if (maybeMap.containsKey("compose")) {
                        Object composeObj = maybeMap.get("compose");
                        if (composeObj instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<?> tokens = (List<?>) composeObj;
                            String composed = evaluateCompose(tokens, json);
                            putValueToNode(root, key, composed);
                            continue; // done for this key
                        } // else fallthrough to literal mapping if compose isn't a list
                    }
                    // If not a compose map, treat it as a literal JSON node (see below)
                }

                if (ruleVal instanceof String) {
                    String pathOrLiteral = (String) ruleVal;
                    if (pathOrLiteral.isEmpty()) {
                        root.put(key, "");
                        continue;
                    }

                    if (pathOrLiteral.startsWith("$")) {
                        try {
                            Object v = JsonPath.read(json, pathOrLiteral);
                            // Special-case: if this is branchName and value is a full ref like "refs/heads/dev",
                            // convert to the short branch name ("dev").
                            if ("branchName".equals(key) && v instanceof String) {
                                String s = (String) v;
                                // If it looks like a ref, take the text after the last '/'
                                if (s.contains("/")) {
                                    s = s.substring(s.lastIndexOf('/') + 1);
                                }
                                putValueToNode(root, key, s);
                            } else {
                                putValueToNode(root, key, v);
                            }
                        } catch (Exception ex) {
                            root.putNull(key);
                        }
                    } else {
                        // literal
                        root.put(key, pathOrLiteral);
                    }
                } else {
                    // non-string literal
                    JsonNode node = objectMapper.convertValue(ruleVal, JsonNode.class);
                    root.set(key, node);
                }
            }

            // Now process commits rule (if any)
            Object commitsRuleObj = rules.get("commits");
            ObjectNode commitsNode = objectMapper.createObjectNode();

            if (commitsRuleObj instanceof Map) {
                Map<String, Object> commitsRule = (Map<String, Object>) commitsRuleObj;
                String listPath = (String) commitsRule.getOrDefault("listPath", "$.commits[*]");
                String idField = (String) commitsRule.getOrDefault("idField", "id");
                Object fieldsObj = commitsRule.get("fields"); // Map<String,Object>

                List<?> items;
                try {
                    items = JsonPath.read(json, listPath);
                } catch (Exception ex) {
                    items = null;
                }

                if (items != null) {
                    for (Object item : items) {
                        Map<String, Object> itemMap = objectMapper.convertValue(
                                item,
                                new TypeReference<Map<String, Object>>() {}
                        );

                        // determine commit id (full)
                        String id = null;
                        if (itemMap.containsKey(idField)) id = String.valueOf(itemMap.get(idField));
                        else if (itemMap.containsKey("hash")) id = String.valueOf(itemMap.get("hash"));
                        else if (itemMap.containsKey("id")) id = String.valueOf(itemMap.get("id"));

                        if (id == null) continue;
                        String shortId = id.length() > 7 ? id.substring(0, 7) : id;

                        ObjectNode commitObj = objectMapper.createObjectNode();

                        if (fieldsObj instanceof Map) {
                            Map<String, Object> fields = (Map<String, Object>) fieldsObj;
                            for (Map.Entry<String, Object> fm : fields.entrySet()) {
                                String outField = fm.getKey();
                                Object mapping = fm.getValue();

                                if (mapping instanceof String) {
                                    String m = (String) mapping;

                                    // JsonPath against commit item
                                    if (m.startsWith("$")) {
                                        try {
                                            Object val = JsonPath.read(item, m);
                                            putValueToNode(commitObj, outField, val);
                                        } catch (Exception ex) {
                                            commitObj.putNull(outField);
                                        }
                                    } else if (containsPlaceholder(m)) {
                                        // template: replace placeholders using root or commitMap; {hash} uses full id
                                        String resolved = resolveTemplate(m, root, itemMap, id);
                                        commitObj.put(outField, resolved == null ? "" : resolved);
                                    } else {
                                        // treat as relative key in commit item, fallback to literal
                                        if (itemMap.containsKey(m)) {
                                            Object val = itemMap.get(m);
                                            putValueToNode(commitObj, outField, val);
                                        } else {
                                            // literal fallback
                                            commitObj.put(outField, m);
                                        }
                                    }
                                } else {
                                    // non-string mapping -> convert as node
                                    JsonNode node = objectMapper.convertValue(mapping, JsonNode.class);
                                    commitObj.set(outField, node);
                                }
                            }
                        } else {
                            // default: put 'message' if available
                            Object messageObj = itemMap.get("message");
                            commitObj.put("message", messageObj == null ? "" : String.valueOf(messageObj));
                        }

                        commitsNode.set(shortId, commitObj);
                    }
                }
            } else {
                // no commits rule: try default extraction $.commits[*]
                try {
                    List<?> defaultItems = JsonPath.read(json, "$.commits[*]");
                    if (defaultItems != null) {
                        for (Object item : defaultItems) {
                            Map<String, Object> itemMap = objectMapper.convertValue(
                                    item,
                                    new TypeReference<Map<String, Object>>() {}
                            );

                            String id = itemMap.containsKey("id") ? String.valueOf(itemMap.get("id")) :
                                    (itemMap.containsKey("hash") ? String.valueOf(itemMap.get("hash")) : null);
                            if (id == null) continue;
                            String shortId = id.length() > 7 ? id.substring(0,7) : id;
                            ObjectNode commitObj = objectMapper.createObjectNode();
                            Object messageObj = itemMap.get("message");
                            commitObj.put("message", messageObj == null ? "" : String.valueOf(messageObj));
                            commitsNode.set(shortId, commitObj);
                        }
                    }
                } catch (Exception ignored) {}
            }

            root.set("commits", commitsNode);

            // convert root to LinkedHashMap and return
            Map<String, Object> resultMap = objectMapper.convertValue(root, LinkedHashMap.class);
            return Optional.of(resultMap);
        } catch (Exception e) {
            logger.warn("Failed to build rulesJson for source=" + source, e);
            return Optional.empty();
        }
    }

    private String evaluateCompose(List<?> tokens, Object json) {
        StringBuilder sb = new StringBuilder();
        for (Object t : tokens) {
            if (t == null) continue;
            if (t instanceof String) {
                String tok = (String) t;
                if (tok.startsWith("$")) {
                    try {
                        Object val = JsonPath.read(json, tok);
                        if (val != null) sb.append(String.valueOf(val));
                    } catch (Exception ignored) {
                        // treat missing path as empty
                    }
                } else {
                    // literal token
                    sb.append(tok);
                }
            } else {
                // non-string token -> convert to string
                sb.append(String.valueOf(t));
            }
        }
        // collapse multiple spaces into one and trim leading/trailing space
        String composed = sb.toString().replaceAll("\\s+", " ").trim();
        return composed;
    }

    private void putValueToNode(ObjectNode node, String key, Object v) {
        if (v == null) {
            node.putNull(key);
        } else if (v instanceof Number) {
            node.put(key, String.valueOf(v));
        } else if (v instanceof Boolean) {
            node.put(key, (Boolean) v);
        } else if (v instanceof List || v instanceof Map) {
            JsonNode converted = objectMapper.convertValue(v, JsonNode.class);
            node.set(key, converted);
        } else {
            node.put(key, String.valueOf(v));
        }
    }

    private boolean containsPlaceholder(String s) {
        return s != null && s.contains("{") && s.contains("}");
    }

    /**
     * Replace placeholders like {repoUrl} or {hash} with value from root or commitMap.
     * Lookup order:
     *  - if placeholder == "hash" or "id" then use full commitId
     *  - try root.get(placeholder) as text
     *  - try commitMap.get(placeholder)
     */
    private String resolveTemplate(String template, ObjectNode root, Map<?,?> commitMap, String commitId) {
        Matcher m = PLACEHOLDER.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            String replacement = "";

            if ("hash".equalsIgnoreCase(key) || "id".equalsIgnoreCase(key)) {
                replacement = commitId;
            } else {
                // try root
                if (root.has(key) && root.get(key).isTextual()) {
                    replacement = root.get(key).asText();
                } else if (commitMap.containsKey(key)) {
                    Object val = commitMap.get(key);
                    replacement = val == null ? "" : String.valueOf(val);
                } else {
                    replacement = "";
                }
            }

            String esc = Matcher.quoteReplacement(replacement == null ? "" : replacement);
            m.appendReplacement(sb, esc);
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
