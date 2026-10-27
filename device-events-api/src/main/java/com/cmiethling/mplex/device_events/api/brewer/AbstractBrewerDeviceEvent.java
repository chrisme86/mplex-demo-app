package com.cmiethling.mplex.device_events.api.brewer;

import org.springframework.lang.NonNull;

import com.cmiethling.mplex.device_events.api.AbstractDeviceEvent;
import com.cmiethling.mplex.device_events.api.Subsystem;

abstract class AbstractBrewerDeviceEvent extends AbstractDeviceEvent {
    static final String destination = prefix + "/BREWER.";

    /**
     * Creates a new event object.
     *
     * @param topic the event topic
     */
    AbstractBrewerDeviceEvent(@NonNull final String topic) {
        super(Subsystem.BREWER, topic);
    }
}
