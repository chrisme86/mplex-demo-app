package com.cmiethling.mplex.device.api.fluidics;

import org.springframework.lang.NonNull;

import com.cmiethling.mplex.device.api.AbstractDeviceEvent;
import com.cmiethling.mplex.device.message.Subsystem;

abstract class AbstractFluidicsDeviceEvent extends AbstractDeviceEvent {

    /**
     * Creates a new event object.
     *
     * @param topic the event topic
     */
    AbstractFluidicsDeviceEvent(@NonNull final String topic) {
        super(Subsystem.FLUIDICS, topic);
    }
}
