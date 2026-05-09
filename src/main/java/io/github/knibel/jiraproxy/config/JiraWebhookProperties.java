package io.github.knibel.jiraproxy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jira.webhook")
public record JiraWebhookProperties(
        boolean enabled,
        String secret,
        String callbackUrl
) {
}
