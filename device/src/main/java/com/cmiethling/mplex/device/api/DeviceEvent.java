package com.cmiethling.mplex.device.api;

import org.springframework.lang.NonNull;

import com.cmiethling.mplex.device.DeviceMessageException;
import com.cmiethling.mplex.device.api.fluidics.ErrorEvent;
import com.cmiethling.mplex.device.api.fluidics.StatesEvent;
import com.cmiethling.mplex.device.api.test.ExampleEvent;
import com.cmiethling.mplex.device.message.EventMessage;
import com.cmiethling.mplex.device.message.Subsystem;

/**
 * This interface describes an event that could be sent by the device.
 */
public interface DeviceEvent {

    /**
     * Returns an event implementation for the given subsystem and topic.
     *
     * @param subsystem the subsystem
     * @param topic     the topic
     *
     * @return a new event implementation
     *
     * @throws DeviceMessageException if no event implementation could be found for this subsystem and topic
     */
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    static DeviceEvent of(@NonNull final Subsystem subsystem, @NonNull final String topic)
            throws DeviceMessageException {
        return switch (subsystem) {
            case FLUIDICS -> switch (topic) {
                case ErrorEvent.TOPIC -> new ErrorEvent();
                case StatesEvent.TOPIC -> new StatesEvent();
                default -> throwInvalidTopicDMException(subsystem, topic);
            };
            case HIGH_VOLTAGE -> switch (topic) {
                case com.cmiethling.mplex.device.api.hv.ErrorEvent.TOPIC ->
                        new com.cmiethling.mplex.device.api.hv.ErrorEvent();
                default -> throwInvalidTopicDMException(subsystem, topic);
            };
            case TEST -> switch (topic) {
                case ExampleEvent.TOPIC -> new ExampleEvent();
                default -> throwInvalidTopicDMException(subsystem, topic);
            };
            default -> throw new DeviceMessageException("no event exists for subsystem:%s".formatted(subsystem));
        };
    }

    private static DeviceEvent throwInvalidTopicDMException(final Subsystem subsystem, final String topic)
            throws DeviceMessageException {
        throw new DeviceMessageException("eventUnknownTopic: subsystem=%s, topic=%s".formatted(subsystem, topic));
    }

    /**
     * Returns the subsystem of this event. The subsystem is used to validate the event message.
     *
     * @return the subsystem this event is originated from
     */
    Subsystem getSubsystem();

    /**
     * Returns the topic of the event The topic is used to validate the event message.
     *
     * @return the topic name of the event
     */
    String getTopic();

    /**
     * Create an event message for this event <strong>for testing only</strong>. This behavior is not need at runtime.
     *
     * @return a new event message
     *
     * @throws DeviceMessageException if there is a problem while creating the message
     */
    @SuppressWarnings("RedundantThrows")
    EventMessage toEventMessage() throws DeviceMessageException;

    /**
     * Populates the parameters of this event from an event message received from the device. This method also checks if
     * the event message matches the subsystem and topic.
     *
     * @param eventMessage the event message received from the device
     *
     * @throws DeviceMessageException if there is a problem evaluating the event message
     */
    void fromEventMessage(@NonNull EventMessage eventMessage) throws DeviceMessageException;
}
