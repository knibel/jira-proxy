package io.github.knibel.jiraproxy.api;

import io.github.knibel.jiraproxy.jira.JiraWebhookNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/webhooks/jira")
public class JiraWebhookController {

    private final JiraWebhookNotificationService jiraWebhookNotificationService;

    public JiraWebhookController(JiraWebhookNotificationService jiraWebhookNotificationService) {
        this.jiraWebhookNotificationService = jiraWebhookNotificationService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> receiveWebhook(
            @RequestHeader(value = "X-Jira-Webhook-Secret", required = false) String providedSecret,
            @RequestBody Map<String, Object> payload) {
        boolean forwarded = jiraWebhookNotificationService.processWebhook(payload, providedSecret);
        return ResponseEntity.accepted().body(Map.of("forwarded", forwarded));
    }
}
