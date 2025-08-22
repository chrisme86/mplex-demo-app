package com.cmiethling.mplex.emulator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketServerConfig implements WebSocketMessageBrokerConfigurer {
    public static final String prefix = "/topic";

    @Bean
    ThreadPoolTaskScheduler brokerScheduler() {
        final var scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void configureMessageBroker(final MessageBrokerRegistry cfg) {
        // messages to these prefixes go to the broker (pub/sub + p2p)
        cfg.enableSimpleBroker(prefix) //
                .setHeartbeatValue(new long[] { 10000, 10000 }) //
                .setTaskScheduler(brokerScheduler());
        // Not needed as client doesn't send messages via WebSocket
        // cfg.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        registry.addEndpoint("/stomp");
        /*.setAllowedOriginPatterns("https://app.example.com") // tighten CORS */
    }
}