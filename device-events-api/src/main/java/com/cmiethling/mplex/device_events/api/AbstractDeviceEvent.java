package com.cmiethling.mplex.device_events.api;

import org.springframework.lang.NonNull;

import lombok.Getter;

/**
 * Base class for implementing device events. Implementing classes of device events should extend this class and add
 * properties read-only properties for the event parameters.
 */
@Getter
public abstract class AbstractDeviceEvent implements DeviceEvent {

    private final Subsystem subsystem;
    private final String topic;

    /**
     * Creates a new device event for the specified subsystem and with the specified topic name.
     *
     * @param subsystem the subsystem for this event
     * @param topic     the topic name as used in the device communication
     */
    protected AbstractDeviceEvent(@NonNull final Subsystem subsystem, @NonNull final String topic) {
        this.subsystem = subsystem;
        this.topic = topic;
    }

    @Override
    public String toString() {
        return String.format("%s[subsystem=%s, topic=%s]", getClass().getSimpleName(), this.subsystem, this.topic);
    }
}