package com.cmiethling.mplex.device.message;

import java.util.UUID;

import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a request that is sent from the application to the device interface.
 */
public final class RequestMessage extends AbstractDeviceMessage {

    /**
     * Creates a new request message.
     *
     * @param id        a unique id for this request
     * @param subsystem the subsystem
     * @param topic     the message topic
     */
    public RequestMessage(@NonNull final UUID id, @NonNull final Subsystem subsystem, @NonNull final String topic) {
        super(id, subsystem, topic);
    }

    // for deserialization
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RequestMessage(@NonNull @JsonProperty(ID) final UUID id,
            @NonNull @JsonProperty(SYSTEM) final Subsystem subsystem, @NonNull @JsonProperty(TOPIC) final String topic,
            @NonNull @JsonProperty(PARAMETERS) final MessageParameters params) {
        super(id, subsystem, topic);
        this.parameters().putAll(params);
    }

    @Override
    @JsonProperty(PARAMETERS)
    public MessageParameters parameters() {
        return super.parameters;
    }

    @Override
    public String toString() {
        return String.format("%s [%s, %s=%s]", //
                getClass().getSimpleName(), //
                super.toString(), //
                PARAMETERS, //
                this.parameters());
    }
}

