package io.github.knibel.jiraproxy.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jira")
public record JiraProperties(
        @NotBlank String baseUrl,
        @NotBlank String username,
        @NotBlank String password
) {
}
