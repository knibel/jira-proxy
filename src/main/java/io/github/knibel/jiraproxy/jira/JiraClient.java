package io.github.knibel.jiraproxy.jira;

import org.springframework.stereotype.Service;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class JiraClient {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_RESPONSE = new ParameterizedTypeReference<>() {
    };
    private final RestClient jiraRestClient;

    public JiraClient(RestClient jiraRestClient) {
        this.jiraRestClient = jiraRestClient;
    }

    public Map<String, Object> findTickets(String jql) {
        return execute(jiraRestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/rest/api/2/search").queryParam("jql", jql).build())
                .retrieve());
    }

    public Map<String, Object> getComments(String issueKey) {
        return execute(jiraRestClient.get()
                .uri("/rest/api/2/issue/{issueKey}/comment", issueKey)
                .retrieve());
    }

    public Map<String, Object> addComment(String issueKey, String commentBody) {
        return execute(jiraRestClient.post()
                .uri("/rest/api/2/issue/{issueKey}/comment", issueKey)
                .body(Map.of("body", commentBody))
                .retrieve());
    }

    private Map<String, Object> execute(RestClient.ResponseSpec responseSpec) {
        try {
            return responseSpec.body(MAP_RESPONSE);
        } catch (RestClientResponseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Jira API request failed");
        }
    }
}
