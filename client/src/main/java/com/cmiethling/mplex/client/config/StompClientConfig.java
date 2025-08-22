package com.cmiethling.mplex.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

@Configuration
public class StompClientConfig {
    @Bean
    public ThreadPoolTaskScheduler stompScheduler() {
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("stomp-");
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    public WebSocketStompClient stompClient(final ThreadPoolTaskScheduler stompScheduler) {
        final WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new MappingJackson2MessageConverter()); // JSON <-> POJO
        client.setTaskScheduler(stompScheduler);  // enables heartbeats
        return client;
    }
}
