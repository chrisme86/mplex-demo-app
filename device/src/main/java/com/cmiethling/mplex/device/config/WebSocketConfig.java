package com.cmiethling.mplex.device.config;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import com.cmiethling.mplex.device.api.DeviceCommand;
import com.cmiethling.mplex.device.service.DeviceMessageService;
import com.cmiethling.mplex.device.service.WebSocketServiceImpl;
import com.cmiethling.mplex.device.websocket.CommandTaskInfo;
import com.cmiethling.mplex.device.websocket.MyWebSocketListener;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableAsync // for DeviceEvents sent by device
@Slf4j
public class WebSocketConfig {
    private static final String INVALID_DEFAULT_VALUE = "someInvalidDefaultValue";

    @Value("${websocket.uri:" + INVALID_DEFAULT_VALUE + "}")
    private String webSocketUri;

    @Bean("uri")
    public URI getWebSocketUri() {
        if (INVALID_DEFAULT_VALUE.equals(this.webSocketUri))
            log.info("######## no websocket uri >> no websocket client #############");
        return URI.create(this.webSocketUri);
    }

    @Bean
    public ConcurrentMap<UUID, CommandTaskInfo<? extends DeviceCommand>> commandTasks() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public MyWebSocketListener myWebSocketListener2(
            final ConcurrentMap<UUID, CommandTaskInfo<? extends DeviceCommand>> commandTasks,
            final DeviceMessageService deviceMessageService, final ApplicationEventPublisher eventPublisher) {
        return new MyWebSocketListener(commandTasks, deviceMessageService, eventPublisher);
    }

    @Bean
    public WebSocketServiceImpl webSocketServiceImpl2(final URI uri,
            final ConcurrentMap<UUID, CommandTaskInfo<? extends DeviceCommand>> commandTasks,
            final DeviceMessageService deviceMessageService, final MyWebSocketListener myWebSocketListener) {
        return new WebSocketServiceImpl(uri, commandTasks, deviceMessageService, myWebSocketListener);
    }
}
