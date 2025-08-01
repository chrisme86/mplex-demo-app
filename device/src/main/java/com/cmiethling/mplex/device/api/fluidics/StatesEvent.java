package com.cmiethling.mplex.device.api.fluidics;

import java.util.Optional;

import org.springframework.lang.NonNull;

import com.cmiethling.mplex.device.DeviceMessageException;
import com.cmiethling.mplex.device.message.EventMessage;

import lombok.Getter;

/**
 * If a state change is detected, this event will be transferred.
 */
@Getter
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

    // ######################################################################
    @Override
    public void fromEventMessage(@NonNull final EventMessage message) throws DeviceMessageException {
        super.fromEventMessage(message);
        this.gelPumpOn = message.parameters().getBoolean(GEL_PUMP_ON_RESULT).orElse(null);
        this.gelValveOpen = message.parameters().getBoolean(GEL_VALVE_OPEN_RESULT).orElse(null);
    }
}

