package com.cmiethling.mplex.device_events.api.heater;

import org.springframework.lang.NonNull;

import com.cmiethling.mplex.device_events.api.AbstractDeviceEvent;
import com.cmiethling.mplex.device_events.api.Subsystem;

abstract class AbstractHeaterDeviceEvent extends AbstractDeviceEvent {
    static final String destination = prefix + "/HEATER.";

    /**
     * Creates a new event object.
     *
     * @param topic the event topic
     */
    AbstractHeaterDeviceEvent(@NonNull final String topic) {
        super(Subsystem.HEATER, topic);
    }
}
