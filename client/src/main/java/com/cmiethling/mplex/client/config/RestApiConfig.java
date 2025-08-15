package com.cmiethling.mplex.client.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.cmiethling.mplex.client_api.api.FluidicsApi;
import com.cmiethling.mplex.client_api.api.HvApi;
import com.cmiethling.mplex.client_api.invoker.ApiClient;

import lombok.val;

@Configuration
public class RestApiConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()//
                .connectTimeout(Duration.ofSeconds(3))//
                .readTimeout(Duration.ofSeconds(10)).build();
    }

    @Bean
    public ApiClient apiclient(@Value("${restapiUrl}") final String basePath, final RestTemplate restTemplate) {
        val apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(basePath);
        return apiClient;
    }

    @Bean
    public FluidicsApi fluidicsApi(final ApiClient apiClient) {
        return new FluidicsApi(apiClient);
    }

    @Bean
    public HvApi hvApi(final ApiClient apiClient) {
        return new HvApi(apiClient);
    }
}
