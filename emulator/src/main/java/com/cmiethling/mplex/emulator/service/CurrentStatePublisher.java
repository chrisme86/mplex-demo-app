package com.cmiethling.mplex.emulator.service;

import org.springframework.stereotype.Service;

import com.cmiethling.mplex.device_events.api.DeviceEvent;
import com.cmiethling.mplex.device_events.api.fluidics.ErrorEvent;
import com.cmiethling.mplex.device_events.api.fluidics.StatesEvent;
import com.cmiethling.mplex.emulator.model.FluidicsStatus;
import com.cmiethling.mplex.emulator.model.HighVoltageStatus;

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
    private final FluidicsStatus fluidicsStatus;
    private final HighVoltageStatus highVoltageStatus;
    private final WebSocketService webSocketService;

    public void publish(final String destination) {
        if (destination.equals(DeviceEvent.getDestination(StatesEvent.class))) {
            final var event = new StatesEvent(); // all states, no commandId
            event.setGelPumpOn(this.fluidicsStatus.isGelPumpOn());
            event.setGelValveOpen(this.fluidicsStatus.isGelValveOpen());
            this.webSocketService.sendEvent(event);
        } else if (destination.equals(DeviceEvent.getDestination(ErrorEvent.class))) {
            final var event = new ErrorEvent();
            event.setError(this.fluidicsStatus.getFluidicsError());
            this.webSocketService.sendEvent(event);
        } else if (destination.equals(
                DeviceEvent.getDestination(com.cmiethling.mplex.device_events.api.hv.ErrorEvent.class))) {
            final var event = new com.cmiethling.mplex.device_events.api.hv.ErrorEvent();
            event.setError(this.highVoltageStatus.getHighVoltageError());
            this.webSocketService.sendEvent(event);
        } else {
            log.debug("No current state for {}", destination);
        }
    }
}
