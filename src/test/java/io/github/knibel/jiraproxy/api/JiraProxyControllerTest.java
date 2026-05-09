package io.github.knibel.jiraproxy.api;

import io.github.knibel.jiraproxy.config.SecurityConfig;
import io.github.knibel.jiraproxy.jira.JiraClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = JiraProxyController.class)
@Import(SecurityConfig.class)
class JiraProxyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JiraClient jiraClient;

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/jira/tickets").param("jql", "project = TEST"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void findTicketsDelegatesToJiraClient() throws Exception {
        Mockito.when(jiraClient.findTickets("project = TEST"))
                .thenReturn(Map.of("total", 1));

        mockMvc.perform(get("/api/jira/tickets")
                        .param("jql", "project = TEST")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void addCommentDelegatesToJiraClient() throws Exception {
        Mockito.when(jiraClient.addComment("ABC-1", "hello"))
                .thenReturn(Map.of("id", "10001"));

        mockMvc.perform(post("/api/jira/tickets/ABC-1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"hello\"}")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("10001"));
    }
}
