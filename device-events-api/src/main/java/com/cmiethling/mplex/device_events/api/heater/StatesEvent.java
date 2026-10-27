package com.cmiethling.mplex.device_events.api.heater;

import static com.cmiethling.mplex.device_events.api.heater.StatesEvent.TOPIC;

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
@Destination(AbstractHeaterDeviceEvent.destination + TOPIC)
public final class StatesEvent extends AbstractHeaterDeviceEvent {

    public static final String TOPIC = "states";

    private Boolean waterHeaterOn;
    // ID of the command that caused this change, null for spontaneous changes of the device
    private UUID commandId;

    /**
     * Creates a new event object.
     */
    public StatesEvent() {
        super(TOPIC);
    }

    public Optional<Boolean> isWaterHeaterOn() {
        return Optional.ofNullable(this.waterHeaterOn);
    }
}
