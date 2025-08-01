package com.cmiethling.mplex.emulator.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.cmiethling.mplex.emulator.service.WebSocketServerService;

@Configuration
@EnableWebSocket
public class WebSocketServerConfig implements WebSocketConfigurer {
    private final WebSocketServerService webSocketServerService;

    @Autowired
    public WebSocketServerConfig(final WebSocketServerService webSocketServerService) {
        this.webSocketServerService = webSocketServerService;
    }

    @Override
    public void registerWebSocketHandlers(final WebSocketHandlerRegistry registry) {
        registry.addHandler(this.webSocketServerService, "/hwAPI").setAllowedOrigins("*");
    }
}