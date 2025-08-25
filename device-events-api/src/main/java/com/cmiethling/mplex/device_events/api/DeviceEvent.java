package com.cmiethling.mplex.device_events.api;

import lombok.val;

/**
 * This interface describes an event that could be sent by the device.
 */
public interface DeviceEvent {
    String prefix = "/topic";

    static <T extends DeviceEvent> String getDestination(final Class<T> clazz) {
        val destination = clazz.getAnnotation(Destination.class);
        if (destination == null || destination.value().isBlank())
            throw new IllegalArgumentException("Missing @Destination on " + clazz.getName());
        return destination.value();
    }
}
