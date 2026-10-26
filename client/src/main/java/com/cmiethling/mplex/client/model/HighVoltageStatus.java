package com.cmiethling.mplex.client.model;

import org.springframework.stereotype.Component;

import com.cmiethling.mplex.device_events.api.hv.ErrorEvent;
import com.cmiethling.mplex.device_events.api.hv.HighVoltageError;

import lombok.Getter;

@Component
public class HighVoltageStatus {
    // row in the status table
    @Getter
    private final EventEntry errorsEvent;
    @Getter
    private HighVoltageError highVoltageError;

    // initialize so that subsystem and topic are not null
    public HighVoltageStatus() {
        final var error = new ErrorEvent();
        this.errorsEvent = new EventEntry(error.getSubsystem(), error.getTopic());
    }

    public void setHighVoltageError(final HighVoltageError error) {
        this.errorsEvent.setCurrentState(error.toString());
        this.highVoltageError = error;
    }
}
