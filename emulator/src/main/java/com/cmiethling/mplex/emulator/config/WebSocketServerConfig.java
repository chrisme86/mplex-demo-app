package com.cmiethling.mplex.emulator.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.cmiethling.mplex.device_events.api.DeviceEvent;
import com.cmiethling.mplex.emulator.service.CurrentStatePublisher;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketServerConfig implements WebSocketMessageBrokerConfigurer {

    // lazy: the publisher needs the messaging template, which is created by this config
    @Autowired
    @Lazy
    private CurrentStatePublisher currentStatePublisher;

    @Bean
    ThreadPoolTaskScheduler brokerScheduler() {
        final var scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void configureMessageBroker(final MessageBrokerRegistry cfg) {
        // messages to these prefixes go to the broker (pub/sub + p2p)
        cfg.enableSimpleBroker(DeviceEvent.prefix) //
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

    @Override
    public void configureClientInboundChannel(final ChannelRegistration registration) {
        registration.interceptors(new ExecutorChannelInterceptor() {
            // after the broker registered the subscription, so the client surely receives the current state
            @Override
            public void afterMessageHandled(final Message<?> message, final MessageChannel channel,
                    final MessageHandler handler, final Exception ex) {
                if (ex == null && handler instanceof SimpleBrokerMessageHandler
                        && SimpMessageHeaderAccessor.getMessageType(message.getHeaders()) == SimpMessageType.SUBSCRIBE)
                    WebSocketServerConfig.this.currentStatePublisher
                            .publish(SimpMessageHeaderAccessor.getDestination(message.getHeaders()));
            }
        });
    }
}