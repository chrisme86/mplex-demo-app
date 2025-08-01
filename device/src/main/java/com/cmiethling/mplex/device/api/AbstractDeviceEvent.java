package com.cmiethling.mplex.device.api;

import org.springframework.lang.NonNull;

import com.cmiethling.mplex.device.DeviceMessageException;
import com.cmiethling.mplex.device.message.EventMessage;
import com.cmiethling.mplex.device.message.Subsystem;

/**
 * Base class for implementing device events. Implementing classes of device events should extend this class and add
 * properties read-only properties for the event parameters.
 */
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
    public Subsystem getSubsystem() {
        return this.subsystem;
    }

    @Override
    public String getTopic() {
        return this.topic;
    }

    /**
     * This implementation creates an event message with the subsystem and the topic set. Subclasses should override
     * this method to set the parameters <strong>if there are required for testing</strong>. Otherwise subclass should
     * not do anything here.
     */
    @Override
    public EventMessage toEventMessage() {
        return new EventMessage(this.subsystem, this.topic);
    }

    /**
     * This implementation fails if the event message does not match the subsystem and topic. Subclasses should override
     * this method to evaluate the results.
     */
    @Override
    public void fromEventMessage(@NonNull final EventMessage message) throws DeviceMessageException {
        if (this.subsystem != message.getSubsystem())
            throw new DeviceMessageException("incompatibleSubsystem: this=%s, from message=%s".formatted(this.subsystem,
                    message.getSubsystem()));
        if (!this.topic.equals(message.getTopic()))
            throw new DeviceMessageException(
                    "incompatibleTopic: this=%s, from message=%s".formatted(this.topic, message.getTopic()));
    }

    @Override
    public String toString() {
        return String.format("%s[subsystem=%s, topic=%s]", getClass().getSimpleName(), this.subsystem, this.topic);
    }
}