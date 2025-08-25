package com.cmiethling.mplex.device_events.api.fluidics;

import static com.cmiethling.mplex.device_events.api.fluidics.StatesEvent.TOPIC;

import java.util.Optional;

import com.cmiethling.mplex.device_events.api.Destination;

import lombok.Getter;

/**
 * If a state change is detected, this event will be transferred.
 */
@Getter
@Destination(AbstractFluidicsDeviceEvent.destination + TOPIC)
public final class StatesEvent extends AbstractFluidicsDeviceEvent {

    public static final String TOPIC = "states";
    public static final String GEL_PUMP_ON_RESULT = "GelPumpOn";
    public static final String GEL_VALVE_OPEN_RESULT = "GelValveOpen";

    private Boolean gelPumpOn;
    private Boolean gelValveOpen;

    /**
     * Creates a new event object.
     */
    public StatesEvent() {
        super(TOPIC);
    }

    public Optional<Boolean> isGelPumpOn() {
        return Optional.ofNullable(this.gelPumpOn);
    }

    public Optional<Boolean> isGelValveOpen() {
        return Optional.ofNullable(this.gelValveOpen);
    }
}

