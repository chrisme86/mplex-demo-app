package com.cmiethling.mplex.device.message;

import static com.cmiethling.mplex.device.message.AbstractDeviceMessage.*;

import java.util.Objects;
import java.util.UUID;

import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Base class for all device messages, such as commands messages, result messages and event messages.
 */
@JsonPropertyOrder({ TYPE, ID, SYSTEM, TOPIC, PARAMETERS })
@JsonInclude(JsonInclude.Include.NON_NULL) // at event ID=null >> exclude it from Json
// Property TYPE is used for determinating the correct Subclass
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = TYPE)
@JsonSubTypes({ @JsonSubTypes.Type(value = RequestMessage.class, name = REQUEST_TYPE),
        @JsonSubTypes.Type(value = ResultMessage.class, name = RESULT_TYPE),
        @JsonSubTypes.Type(value = EventMessage.class, name = EVENT_TYPE) })
public abstract sealed class AbstractDeviceMessage implements DeviceMessage permits RequestMessage, ResultMessage,
        EventMessage {

    public static final String REQUEST_TYPE = "request";
    public static final String RESULT_TYPE = "result";
    public static final String EVENT_TYPE = "event";
    public static final String DATA = "data";
    public static final String SYSTEM = "subsystem";
    public static final String PARAMETERS = "parameters";
    public static final String TOPIC = "topic";

    static final String TYPE = "type";
    static final String ID = "id";
    static final String RESULT = "result";
    static final String ERROR = "error";

    // no JsonProperty("name") as the "name" is different for all Subclasses
    protected final MessageParameters parameters = new MessageParametersImpl();

    @JsonProperty(value = ID)
    // can be null because EventMessage has no id
    private final UUID id;

    @JsonProperty(TOPIC)
    @NonNull
    private final String topic;

    @JsonProperty(SYSTEM)
    @NonNull
    private final Subsystem subsystem;

    /**
     * Creates a new message with the specified unique id, subsystem and topic.
     *
     * @param id        the unique id for this message, can be null because {@link EventMessage} doesn't have an id
     * @param subsystem the system this message is targeted to or originated from
     * @param topic     the topic of this message
     */
    protected AbstractDeviceMessage(final UUID id, @NonNull final Subsystem subsystem, @NonNull final String topic) {
        this.id = id;
        this.subsystem = subsystem;
        this.topic = topic;
    }

    // abstract so JsonProperty("name") can be called in all Subclasses as the "name" is always different
    @Override
    public abstract MessageParameters parameters();

    @Override
    public final UUID getId() {
        return this.id;
    }

    @Override
    public final Subsystem getSubsystem() {
        return this.subsystem;
    }

    @Override
    public final String getTopic() {
        return this.topic;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.subsystem, this.topic, this.parameters());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        final var other = (AbstractDeviceMessage) obj;
        return Objects.equals(this.id, other.id) && this.subsystem == other.subsystem && Objects.equals(this.topic,
                other.topic) && Objects.equals(this.parameters(), other.parameters());
    }

    @Override
    public String toString() {
        final var id1 = this.id != null ? "%s=%s, ".formatted(ID, this.id) : "";
        return "%s%s=%s, %s=%s".formatted(id1, SYSTEM, this.subsystem, TOPIC, this.topic);
    }
}
