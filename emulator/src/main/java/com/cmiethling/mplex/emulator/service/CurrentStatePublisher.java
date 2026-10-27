package com.cmiethling.mplex.emulator.service;

import org.springframework.stereotype.Service;

import com.cmiethling.mplex.device_events.api.DeviceEvent;
import com.cmiethling.mplex.device_events.api.brewer.ErrorEvent;
import com.cmiethling.mplex.device_events.api.brewer.StatesEvent;
import com.cmiethling.mplex.emulator.model.BrewerStatus;
import com.cmiethling.mplex.emulator.model.HeaterStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sends the current state of a topic as soon as a client subscribed to it. So a client never shows outdated or unknown
 * states after a (re)connect.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CurrentStatePublisher {
    private final BrewerStatus brewerStatus;
    private final HeaterStatus heaterStatus;
    private final WebSocketService webSocketService;

    public void publish(final String destination) {
        if (destination.equals(DeviceEvent.getDestination(StatesEvent.class))) {
            final var event = new StatesEvent(); // no commandId
            event.setWaterPumpOn(this.brewerStatus.isWaterPumpOn());
            this.webSocketService.sendEvent(event);
        } else if (destination.equals(DeviceEvent.getDestination(ErrorEvent.class))) {
            final var event = new ErrorEvent();
            event.setError(this.brewerStatus.getBrewerError());
            this.webSocketService.sendEvent(event);
        } else if (destination.equals(
                DeviceEvent.getDestination(com.cmiethling.mplex.device_events.api.heater.StatesEvent.class))) {
            final var event = new com.cmiethling.mplex.device_events.api.heater.StatesEvent();
            event.setWaterHeaterOn(this.heaterStatus.isWaterHeaterOn());
            this.webSocketService.sendEvent(event);
        } else if (destination.equals(
                DeviceEvent.getDestination(com.cmiethling.mplex.device_events.api.heater.ErrorEvent.class))) {
            final var event = new com.cmiethling.mplex.device_events.api.heater.ErrorEvent();
            event.setError(this.heaterStatus.getHeaterError());
            this.webSocketService.sendEvent(event);
        } else {
            log.debug("No current state for {}", destination);
        }
    }
}
