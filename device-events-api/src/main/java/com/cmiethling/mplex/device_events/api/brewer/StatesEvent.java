package com.cmiethling.mplex.device_events.api.brewer;

import static com.cmiethling.mplex.device_events.api.brewer.StatesEvent.TOPIC;

import java.util.Optional;
import java.util.UUID;

import com.cmiethling.mplex.device_events.api.Destination;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * If a state change is detected, this event will be transferred.
 */
@Getter
@Setter
@ToString(callSuper = true)
@Destination(AbstractBrewerDeviceEvent.destination + TOPIC)
public final class StatesEvent extends AbstractBrewerDeviceEvent {

    public static final String TOPIC = "states";

    private Boolean waterPumpOn;
    private Boolean brewValveOpen;
    // ID of the command that caused this change, null for spontaneous changes of the device
    private UUID commandId;

    /**
     * Creates a new event object.
     */
    public StatesEvent() {
        super(TOPIC);
    }

    public Optional<Boolean> isWaterPumpOn() {
        return Optional.ofNullable(this.waterPumpOn);
    }

    public Optional<Boolean> isBrewValveOpen() {
        return Optional.ofNullable(this.brewValveOpen);
    }
}

