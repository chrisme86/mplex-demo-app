package com.cmiethling.mplex.client.model;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cmiethling.mplex.device_events.api.Subsystem;
import com.cmiethling.mplex.device_events.api.fluidics.ErrorEvent;

import lombok.Getter;

@Component
public class FluidicsStatus {
    // one row each in the status table
    @Getter
    private final EventEntry errorsEvent;
    @Getter
    private final EventEntry gelPump;
    @Getter
    private final EventEntry gelValve;

    // initialize so that subsystem and topic are not null
    public FluidicsStatus() {
        final var error = new ErrorEvent();
        this.errorsEvent = new EventEntry(error.getSubsystem(), error.getTopic());
        // the states from StatesEvent separately, as it is possible to receive only 1 state with 1 StatesEvent
        this.gelPump = new EventEntry(Subsystem.FLUIDICS, "Gel Pump");
        this.gelValve = new EventEntry(Subsystem.FLUIDICS, "Gel Valve");
    }

    public void setErrorsEvent(final ErrorEvent errorEvent) {
        this.errorsEvent.setCurrentState(errorEvent.getError().toString());
    }

    public void setGelPump(final boolean isOn) {
        this.gelPump.setCurrentState(isOn ? "ON" : "OFF");
    }

    public void setGelValve(final boolean isOpen) {
        this.gelValve.setCurrentState(isOpen ? "OPEN" : "CLOSED");
    }

    /**
     * The result event of a command arrived, so its row is no longer pending.
     */
    public void completeCommand(final UUID commandId) {
        for (final var entry : List.of(this.gelPump, this.gelValve))
            if (commandId.equals(entry.getPendingCommandId()))
                entry.setPendingCommandId(null);
    }
}
