package io.github.knibel.jiraproxy.jira;

import io.github.knibel.jiraproxy.config.JiraWebhookProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class JiraWebhookNotificationService {

    private final JiraWebhookProperties jiraWebhookProperties;
    private final RestClient callbackRestClient;

    public JiraWebhookNotificationService(JiraWebhookProperties jiraWebhookProperties, RestClient.Builder restClientBuilder) {
        this.jiraWebhookProperties = jiraWebhookProperties;
        this.callbackRestClient = restClientBuilder.build();
    }

    public boolean processWebhook(Map<String, Object> payload, String providedSecret) {
        if (!jiraWebhookProperties.enabled()) {
            return false;
        }
        validateSecret(providedSecret);
        if (!isUserStory(payload)) {
            return false;
        }
        Set<String> trackedChanges = trackedChangeFields(payload);
        if (trackedChanges.isEmpty()) {
            return false;
        }
        if (!StringUtils.hasText(jiraWebhookProperties.callbackUrl())) {
            return false;
        }
        sendNotification(buildNotification(payload, trackedChanges));
        return true;
    }

    private void validateSecret(String providedSecret) {
        String configuredSecret = jiraWebhookProperties.secret();
        if (StringUtils.hasText(configuredSecret) && !configuredSecret.equals(providedSecret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid webhook secret");
        }
    }

    private boolean isUserStory(Map<String, Object> payload) {
        String issueType = readString(payload, "issue", "fields", "issuetype", "name");
        if (issueType == null) {
            return false;
        }
        String normalizedIssueType = issueType.trim().toLowerCase();
        return normalizedIssueType.equals("story") || normalizedIssueType.equals("user story");
    }

    private Set<String> trackedChangeFields(Map<String, Object> payload) {
        Set<String> fields = new LinkedHashSet<>();
        for (Map<String, Object> item : readMapList(payload, "changelog", "items")) {
            String field = readString(item, "field");
            if (field == null) {
                continue;
            }
            String normalizedField = field.trim().toLowerCase();
            if (normalizedField.equals("assignee") || normalizedField.equals("labels")) {
                fields.add(normalizedField);
            }
        }
        return fields;
    }

    private Map<String, Object> buildNotification(Map<String, Object> payload, Set<String> trackedChanges) {
        Map<String, Object> notification = new java.util.LinkedHashMap<>();
        notification.put("eventType", payload.get("webhookEvent"));
        notification.put("issueKey", readString(payload, "issue", "key"));
        notification.put("issueType", readString(payload, "issue", "fields", "issuetype", "name"));
        notification.put("changedFields", trackedChanges.stream().toList());
        notification.put("labels", readObject(payload, "issue", "fields", "labels"));
        notification.put("assignee", readObject(payload, "issue", "fields", "assignee"));
        return notification;
    }

    private void sendNotification(Map<String, Object> notification) {
        try {
            callbackRestClient.post()
                    .uri(jiraWebhookProperties.callbackUrl())
                    .body(notification)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Notification callback failed with status: " + ex.getStatusCode().value()
            );
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> readMapList(Map<String, Object> payload, String... path) {
        Object node = readObject(payload, path);
        if (!(node instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, Object>> maps = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                maps.add((Map<String, Object>) map);
            }
        }
        return maps;
    }

    @SuppressWarnings("unchecked")
    private Object readObject(Map<String, Object> payload, String... path) {
        Object current = payload;
        for (String key : path) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = ((Map<String, Object>) map).get(key);
        }
        return current;
    }

    private String readString(Map<String, Object> payload, String... path) {
        Object value = readObject(payload, path);
        return value instanceof String text ? text : null;
    }
}
