package io.github.knibel.jiraproxy.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(JiraProperties.class)
public class JiraClientConfig {

    @Bean
    RestClient jiraRestClient(RestClient.Builder restClientBuilder, JiraProperties jiraProperties) {
        var credentials = jiraProperties.username() + ":" + jiraProperties.password();
        var encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        return restClientBuilder
                .baseUrl(jiraProperties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
                .build();
    }
}
