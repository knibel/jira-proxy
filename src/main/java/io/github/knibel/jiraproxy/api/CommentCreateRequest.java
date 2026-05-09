package io.github.knibel.jiraproxy.api;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(@NotBlank String body) {
}
