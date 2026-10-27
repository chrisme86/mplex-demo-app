package com.cmiethling.mplex.client.model;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cmiethling.mplex.device_events.api.Subsystem;
import com.cmiethling.mplex.device_events.api.heater.ErrorEvent;

import lombok.Getter;

@Component
public class HeaterStatus {
    // one row each in the status table
    @Getter
    private final EventEntry errorsEvent;
    @Getter
    private final EventEntry waterHeater;

    // initialize so that subsystem and topic are not null
    public HeaterStatus() {
        final var error = new ErrorEvent();
        this.errorsEvent = new EventEntry(error.getSubsystem(), error.getTopic());
        this.waterHeater = new EventEntry(Subsystem.HEATER, "Water Heater");
    }

    public void setErrorsEvent(final ErrorEvent errorEvent) {
        this.errorsEvent.setCurrentState(errorEvent.getError().toString());
    }

    public void setWaterHeater(final boolean isOn) {
        this.waterHeater.setCurrentState(isOn ? "ON" : "OFF");
    }

    /**
     * The result event of a command arrived, so its row is no longer pending.
     */
    public void completeCommand(final UUID commandId) {
        if (commandId.equals(this.waterHeater.getPendingCommandId()))
            this.waterHeater.setPendingCommandId(null);
    }
}
