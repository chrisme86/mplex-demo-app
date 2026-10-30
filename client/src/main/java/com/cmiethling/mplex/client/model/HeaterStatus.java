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
    private final DeviceState errors;
    @Getter
    private final DeviceState waterHeater;

    // initialize so that subsystem and topic are not null
    public HeaterStatus() {
        final var error = new ErrorEvent();
        this.errors = new DeviceState(error.getSubsystem(), error.getTopic());
        this.waterHeater = new DeviceState(Subsystem.HEATER, "Water Heater");
    }

    public void setErrors(final ErrorEvent errorEvent) {
        this.errors.setCurrentState(errorEvent.getError().toString());
    }

    public void setWaterHeater(final boolean isOn) {
        this.waterHeater.setCurrentState(isOn ? "ON" : "OFF");
    }

    /**
     * The result event of a command arrived, so its device state is no longer pending.
     */
    public void completeCommand(final UUID commandId) {
        if (commandId.equals(this.waterHeater.getPendingCommandId()))
            this.waterHeater.setPendingCommandId(null);
    }
}
