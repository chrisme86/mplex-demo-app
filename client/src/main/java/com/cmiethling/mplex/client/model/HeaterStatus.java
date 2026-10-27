package com.cmiethling.mplex.client.model;

import org.springframework.stereotype.Component;

import com.cmiethling.mplex.device_events.api.heater.ErrorEvent;
import com.cmiethling.mplex.device_events.api.heater.HeaterError;

import lombok.Getter;

@Component
public class HeaterStatus {
    // row in the status table
    @Getter
    private final EventEntry errorsEvent;
    @Getter
    private HeaterError heaterError;

    // initialize so that subsystem and topic are not null
    public HeaterStatus() {
        final var error = new ErrorEvent();
        this.errorsEvent = new EventEntry(error.getSubsystem(), error.getTopic());
    }

    public void setHeaterError(final HeaterError error) {
        this.errorsEvent.setCurrentState(error.toString());
        this.heaterError = error;
    }
}
