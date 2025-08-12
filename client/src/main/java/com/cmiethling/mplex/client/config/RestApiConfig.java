package com.cmiethling.mplex.client.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.cmiethling.mplex.client_api.ApiClient;
import com.cmiethling.mplex.client_api.api.FluidicsApi;

@Configuration
public class RestApiConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()//
                .setConnectTimeout(Duration.ofSeconds(3))//
                .setReadTimeout(Duration.ofSeconds(10)).build();
    }

    @Bean
    public ApiClient apiclient(final RestTemplate restTemplate) {
        return new ApiClient(restTemplate);
    }

    @Bean
    public FluidicsApi accountApi(final ApiClient apiClient) {
        return new FluidicsApi(apiClient);
    }
}
