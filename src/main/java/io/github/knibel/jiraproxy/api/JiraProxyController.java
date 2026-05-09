package io.github.knibel.jiraproxy.api;

import io.github.knibel.jiraproxy.jira.JiraClient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Validated
@RequestMapping("/api/jira")
public class JiraProxyController {

    private final JiraClient jiraClient;

    public JiraProxyController(JiraClient jiraClient) {
        this.jiraClient = jiraClient;
    }

    @GetMapping("/tickets")
    public Map<String, Object> findTickets(@RequestParam @NotBlank @Size(max = 1000) String jql) {
        return jiraClient.findTickets(jql);
    }

    @GetMapping("/tickets/{issueKey}/comments")
    public Map<String, Object> getComments(@PathVariable @NotBlank String issueKey) {
        return jiraClient.getComments(issueKey);
    }

    @PostMapping("/tickets/{issueKey}/comments")
    public Map<String, Object> addComment(@PathVariable @NotBlank String issueKey, @Valid @RequestBody CommentCreateRequest request) {
        return jiraClient.addComment(issueKey, request.body());
    }
}
