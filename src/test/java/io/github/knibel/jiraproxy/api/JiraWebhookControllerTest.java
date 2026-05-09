package io.github.knibel.jiraproxy.api;

import io.github.knibel.jiraproxy.config.SecurityConfig;
import io.github.knibel.jiraproxy.jira.JiraWebhookNotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = JiraWebhookController.class)
@Import(SecurityConfig.class)
class JiraWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JiraWebhookNotificationService jiraWebhookNotificationService;

    @Test
    void receivesWebhookWithoutAuthentication() throws Exception {
        Mockito.when(jiraWebhookNotificationService.processWebhook(anyMap(), eq(null))).thenReturn(false);

        mockMvc.perform(post("/webhooks/jira")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"webhookEvent\":\"jira:issue_updated\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.forwarded").value(false));
    }

    @Test
    void forwardsSecretHeaderToService() throws Exception {
        Mockito.when(jiraWebhookNotificationService.processWebhook(anyMap(), eq("s3cret"))).thenReturn(true);

        mockMvc.perform(post("/webhooks/jira")
                        .header("X-Jira-Webhook-Secret", "s3cret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"webhookEvent\":\"jira:issue_updated\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.forwarded").value(true));
    }

    @Test
    void returnsUnauthorizedForInvalidWebhookSecret() throws Exception {
        Mockito.when(jiraWebhookNotificationService.processWebhook(anyMap(), eq("wrong")))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid webhook secret"));

        mockMvc.perform(post("/webhooks/jira")
                        .header("X-Jira-Webhook-Secret", "wrong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"webhookEvent\":\"jira:issue_updated\"}"))
                .andExpect(status().isUnauthorized());
    }
}
