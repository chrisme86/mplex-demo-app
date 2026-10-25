package com.cmiethling.mplex.client.model;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cmiethling.mplex.device_events.api.hv.ErrorEvent;
import com.cmiethling.mplex.device_events.api.hv.HighVoltageError;

import lombok.Getter;

@Component
public class HighVoltageStatus {
    private final EventEntry errorsEvent;
    @Getter
    private final List<EventEntry> entries;
    @Getter
    private HighVoltageError highVoltageError;

    // initialize so that subsystem and topic are not null
    public HighVoltageStatus() {
        final var error = new ErrorEvent();
        this.errorsEvent = new EventEntry(error.getSubsystem(), error.getTopic());
        this.entries = List.of(this.errorsEvent);
    }

    public void setHighVoltageError(final HighVoltageError error) {
        this.errorsEvent.setCurrentState(error.toString());
        this.highVoltageError = error;
    }
}
