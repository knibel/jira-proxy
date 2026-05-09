package io.github.knibel.jiraproxy.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(JiraProperties.class)
public class JiraClientConfig {

    @Bean
    RestClient jiraRestClient(RestClient.Builder restClientBuilder, JiraProperties jiraProperties) {
        return restClientBuilder
                .baseUrl(jiraProperties.baseUrl())
                .defaultHeaders(headers -> headers.setBasicAuth(jiraProperties.username(), jiraProperties.password()))
                .build();
    }
}
