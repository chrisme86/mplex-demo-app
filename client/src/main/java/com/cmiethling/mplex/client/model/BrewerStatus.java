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
    private final DeviceState errors;
    @Getter
    private final DeviceState waterPump;

    // initialize so that subsystem and topic are not null
    public BrewerStatus() {
        final var error = new ErrorEvent();
        this.errors = new DeviceState(error.getSubsystem(), error.getTopic());
        this.waterPump = new DeviceState(Subsystem.BREWER, "Water Pump");
    }

    public void setErrors(final ErrorEvent errorEvent) {
        this.errors.setCurrentState(errorEvent.getError().toString());
    }

    public void setWaterPump(final boolean isOn) {
        this.waterPump.setCurrentState(isOn ? "ON" : "OFF");
    }

    /**
     * The result event of a command arrived, so its device state is no longer pending.
     */
    public void completeCommand(final UUID commandId) {
        if (commandId.equals(this.waterPump.getPendingCommandId()))
            this.waterPump.setPendingCommandId(null);
    }
}
