package com.cmiethling.mplex.device.api;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.lang.NonNull;

import com.cmiethling.mplex.device.DeviceCommandException;
import com.cmiethling.mplex.device.DeviceMessageException;
import com.cmiethling.mplex.device.message.RequestMessage;
import com.cmiethling.mplex.device.message.ResultMessage;
import com.cmiethling.mplex.device.message.Subsystem;

/**
 * Base class for implementing device commands. Implementing classes of device commands should extend this class and add
 * properties for the parameters and read-only properties for the results.
 * <p>
 * These properties should be named as appropriate for using the command by the application. This naming must be
 * decoupled from the parameter names in the message. The goal is to prevent application code changes when the naming in
 * the device interface is changing. However, in most cases the names will be the same.
 * </p>
 *
 * @param <E> the type of the subsystem error
 */
public abstract class AbstractDeviceCommand<E extends SubsystemError> implements DeviceCommand {

    private final Subsystem subsystem;
    private final String topic;
    private UUID id;
    private Supplier<UUID> idGenerator;

    /**
     * Creates a new device command for the specified subsystem and with the specified topic.
     *
     * @param subsystem the subsystem for this command
     * @param topic     the topic as used in the device communication
     */
    protected AbstractDeviceCommand(@NonNull final Subsystem subsystem, @NonNull final String topic) {
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
     * Only for unit testing thus package private.
     */
    void setIdGenerator(@NonNull final Supplier<UUID> idGenerator) {
        this.idGenerator = idGenerator;
    }

    /**
     * This implementation creates a request message with the unique id, the subsystem and the topic. Subclasses should
     * override this method to set the parameters.
     */
    @Override
    public RequestMessage toRequestMessage() {
        this.id = this.idGenerator != null ? this.idGenerator.get() : UUID.randomUUID();
        return new RequestMessage(this.id, this.subsystem, this.topic);
    }

    /**
     * This implementation fails if the result does not match the request result. Subclasses should override this method
     * to evaluate the results.
     */
    @Override
    public void fromResultMessage(@NonNull final ResultMessage result)
            throws DeviceMessageException, DeviceCommandException {

        // If this command has an id, check if the result matches.
        // A command may not have an id if it was not send before (like in test cases).
        if (this.id != null && !this.id.equals(result.getId()))
            throw new DeviceMessageException("incompatibleUUID", this.id, result.getId());
        if (this.subsystem != result.getSubsystem())
            throw new DeviceMessageException("incompatibleSubsystem", this.subsystem, result.getSubsystem());
        if (!this.topic.equals(result.getTopic()))
            throw new DeviceMessageException("incompatibleTopic", this.topic, result.getTopic());

        // check if there is an error
        final var error = result.getError();
        switch (error) {
        case NONE -> {
            // there is no error, just continue
        }
        case EMULATOR_BUSY -> throw new DeviceMessageException("general", "EmulatorBusy for request " + this.topic);
        case UNKNOWN -> throw new DeviceMessageException("resultUnknown", this);
        case INVALID_SUBSYSTEM -> handleInvalidSubsystem();
        case INVALID_TOPIC -> handleInvalidTopic();
        case INVALID_PARAMETERS -> handleInvalidParameters();
        case COMMAND_ERROR -> handleCommandError(result);
        default -> throw new IllegalArgumentException(error.name());
        }
    }

    private void handleInvalidSubsystem() throws DeviceMessageException {
        throw new DeviceMessageException("resultInvalidSubsystem", this);
    }

    private void handleInvalidTopic() throws DeviceMessageException {
        throw new DeviceMessageException("resultInvalidTopic", this);
    }

    private void handleInvalidParameters() throws DeviceMessageException {
        throw new DeviceMessageException("resultInvalidParameters", this);
    }

    private void handleCommandError(final ResultMessage message) throws DeviceMessageException, DeviceCommandException {
        // there should be a field "retVal" in the parameters.
        final var optRetVal = message.parameters().getInt(ResultMessage.COMMAND_RETURN_VALUE);

        // convert the retVal to a text
        if (optRetVal.isPresent()) {
            final var errorCode = optRetVal.getAsInt();
            final var error = getSubsystemError(errorCode).orElse(null);
            if (error != null)
                throw new DeviceCommandException(message.getSubsystem(), message.getTopic(), error.toString());
            throw new DeviceCommandException(message.getSubsystem(), message.getTopic(),
                    "no SubsystemError for: " + errorCode);
        }

        throw new DeviceCommandException(message.getSubsystem(), message.getTopic(), "UNKNOWN_ERROR");
    }

    /**
     * Returns an implementation-specific subsystem error. The method {@link #fromResultMessage(ResultMessage)} has
     * detected an error and uses this method to allow the command implementation to return a subsystem error object for
     * the retVal parameter. Usually this is an implementation-specific enum value.
     *
     * @param code the code, extracted from the retVal of the message
     *
     * @return an optional subsystem error
     */
    protected Optional<E> getSubsystemError(final int code) {
        return Optional.empty();
    }

    /**
     * Checks if a required value is present. This method can be used by implementations of {@link #toRequestMessage()}
     * to ensure the required values are set by the application.
     *
     * @param <T>   the type of the value
     * @param name  the name of the value
     * @param value the value to check
     *
     * @return the valid value
     *
     * @throws DeviceMessageException if the required value is not present
     */
    @SuppressWarnings("static-method")
    protected <T> T requireValue(final String name, final T value) throws DeviceMessageException {
        if (value == null)
            throw new DeviceMessageException("valueRequired", name);
        return value;
    }

    @Override
    public String toString() { // TODO id von AbstractDeviceMessage
        return String.format("%s [id=%s, subsystem=%s, topic=%s]", getClass().getSimpleName(), this.id, this.subsystem,
                this.topic);
    }
}

