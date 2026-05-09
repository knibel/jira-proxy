package io.github.knibel.jiraproxy.jira;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class JiraClient {

    private final RestClient jiraRestClient;

    public JiraClient(RestClient jiraRestClient) {
        this.jiraRestClient = jiraRestClient;
    }

    public Map<String, Object> findTickets(String jql) {
        return jiraRestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/rest/api/2/search").queryParam("jql", jql).build())
                .retrieve()
                .body(Map.class);
    }

    public Map<String, Object> getComments(String issueKey) {
        return jiraRestClient.get()
                .uri("/rest/api/2/issue/{issueKey}/comment", issueKey)
                .retrieve()
                .body(Map.class);
    }

    public Map<String, Object> addComment(String issueKey, String commentBody) {
        return jiraRestClient.post()
                .uri("/rest/api/2/issue/{issueKey}/comment", issueKey)
                .body(Map.of("body", commentBody))
                .retrieve()
                .body(Map.class);
    }
}
