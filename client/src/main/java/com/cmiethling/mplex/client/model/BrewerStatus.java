package com.cmiethling.mplex.client.model;

import java.util.List;
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
    @Getter
    private final EventEntry brewValve;

    // initialize so that subsystem and topic are not null
    public BrewerStatus() {
        final var error = new ErrorEvent();
        this.errorsEvent = new EventEntry(error.getSubsystem(), error.getTopic());
        // the states from StatesEvent separately, as it is possible to receive only 1 state with 1 StatesEvent
        this.waterPump = new EventEntry(Subsystem.BREWER, "Water Pump");
        this.brewValve = new EventEntry(Subsystem.BREWER, "Brew Valve");
    }

    public void setErrorsEvent(final ErrorEvent errorEvent) {
        this.errorsEvent.setCurrentState(errorEvent.getError().toString());
    }

    public void setWaterPump(final boolean isOn) {
        this.waterPump.setCurrentState(isOn ? "ON" : "OFF");
    }

    public void setBrewValve(final boolean isOpen) {
        this.brewValve.setCurrentState(isOpen ? "OPEN" : "CLOSED");
    }

    /**
     * The result event of a command arrived, so its row is no longer pending.
     */
    public void completeCommand(final UUID commandId) {
        for (final var entry : List.of(this.waterPump, this.brewValve))
            if (commandId.equals(entry.getPendingCommandId()))
                entry.setPendingCommandId(null);
    }
}
