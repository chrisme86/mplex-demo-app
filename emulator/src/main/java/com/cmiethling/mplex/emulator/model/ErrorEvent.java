package com.cmiethling.mplex.emulator.model;

import com.cmiethling.mplex.device.api.SubsystemError;
import com.cmiethling.mplex.device.message.Subsystem;

import lombok.Data;

@Data
public class ErrorEvent {
    private Subsystem subsystem;
    private SubsystemError error;

    public ErrorEvent(final Subsystem subsystem, final SubsystemError error) {
        this.subsystem = subsystem;
        this.error = error;
    }
}
