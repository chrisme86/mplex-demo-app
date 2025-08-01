package com.cmiethling.mplex.device.message;

import static com.cmiethling.mplex.device.message.AbstractDeviceMessage.*;

import java.util.Objects;
import java.util.UUID;

import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.Setter;

/**
 * This class represents a result that is sent from the device interface to the application. It is a response to a
 * request message.
 */
@Setter
@Getter
@JsonPropertyOrder({ TYPE, ID, SYSTEM, TOPIC, ERROR, RESULT })
public final class ResultMessage extends AbstractDeviceMessage {

    /**
     * Name of the parameter used for the command return value.
     */
    public static final String COMMAND_RETURN_VALUE = "retVal";

    @JsonProperty(ERROR)
    @NonNull
    private ResultError error = ResultError.NONE;

    /**
     * Creates a new result message object.
     *
     * @param id     a unique id
     * @param system the subsystem
     * @param topic  the topic
     */
    public ResultMessage(@NonNull final UUID id, @NonNull final Subsystem system, @NonNull final String topic) {
        super(id, system, topic);
    }

    // for deserialization
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ResultMessage(@NonNull @JsonProperty(ID) final UUID id,
            @NonNull @JsonProperty(SYSTEM) final Subsystem subsystem, @NonNull @JsonProperty(TOPIC) final String topic,
            @NonNull @JsonProperty(ERROR) final ResultError error,
            @NonNull @JsonProperty(RESULT) final MessageParameters params) {
        super(id, subsystem, topic);
        this.parameters().putAll(params);
        this.error = error;
    }

    @Override
    @JsonProperty(RESULT)
    public MessageParameters parameters() {
        return super.parameters;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.parameters, this.error);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!super.equals(obj))
            return false;
        final var other = (ResultMessage) obj;
        return this.error == other.error;
    }

    @Override
    public String toString() {
        return String.format("%s [%s, %s=%s, %s=%s]", getClass().getSimpleName(), super.toString(), ERROR, this.error,
                RESULT, this.parameters());
    }
}

