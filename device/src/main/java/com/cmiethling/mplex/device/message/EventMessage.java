package com.cmiethling.mplex.device.message;

import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents an event that is sent from the device interface to the application. Event message do not have
 * an ID during communication. This default ID is set to fulfill the message implementation specification. In future
 * versions event might have a random ID to uniquely identify the incoming events.
 */
public final class EventMessage extends AbstractDeviceMessage {

    /**
     * Creates a new event message object.
     *
     * @param system the subsystem
     * @param topic  the topic
     */
    public EventMessage(@NonNull final Subsystem system, @NonNull final String topic) {
        super(null, system, topic);
    }

    // for deserialization
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public EventMessage(@NonNull @JsonProperty(SYSTEM) final Subsystem subsystem,
            @NonNull @JsonProperty(TOPIC) final String topic,
            @NonNull @JsonProperty(DATA) final MessageParameters params) {
        super(null, subsystem, topic);
        this.parameters().putAll(params);
    }

    @Override
    @JsonProperty(DATA)
    public MessageParameters parameters() {
        return super.parameters;
    }

    @Override
    public String toString() {
        return String.format("%s [%s, %s=%s]", //
                getClass().getSimpleName(), //
                super.toString(), DATA, this.parameters);
    }
}

