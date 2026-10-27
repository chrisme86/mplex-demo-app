package com.cmiethling.mplex.client.model;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cmiethling.mplex.device_events.api.Subsystem;
import com.cmiethling.mplex.device_events.api.brewer.ErrorEvent;

import lombok.Getter;

@Component
public class BrewerStatus {
    // one row each in the status table
    @Getter
    private final EventEntry errorsEvent;
    @Getter
    private final EventEntry waterPump;

    // initialize so that subsystem and topic are not null
    public BrewerStatus() {
        final var error = new ErrorEvent();
        this.errorsEvent = new EventEntry(error.getSubsystem(), error.getTopic());
        this.waterPump = new EventEntry(Subsystem.BREWER, "Water Pump");
    }

    public void setErrorsEvent(final ErrorEvent errorEvent) {
        this.errorsEvent.setCurrentState(errorEvent.getError().toString());
    }

    public void setWaterPump(final boolean isOn) {
        this.waterPump.setCurrentState(isOn ? "ON" : "OFF");
    }

    /**
     * The result event of a command arrived, so its row is no longer pending.
     */
    public void completeCommand(final UUID commandId) {
        if (commandId.equals(this.waterPump.getPendingCommandId()))
            this.waterPump.setPendingCommandId(null);
    }
}
