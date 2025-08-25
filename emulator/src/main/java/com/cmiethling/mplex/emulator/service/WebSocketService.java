package com.cmiethling.mplex.emulator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.cmiethling.mplex.device_events.api.DeviceEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WebSocketService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendEvent(@NonNull final DeviceEvent event) {
        this.messagingTemplate.convertAndSend(event.getDestination(), event);
        log.info("sent event {} to {}", event, event.getDestination());
    }
}
