package com.cmiethling.mplex.device.api;

import org.springframework.lang.NonNull;

import com.cmiethling.mplex.device.DeviceCommandException;
import com.cmiethling.mplex.device.DeviceMessageException;
import com.cmiethling.mplex.device.message.RequestMessage;
import com.cmiethling.mplex.device.message.ResultMessage;
import com.cmiethling.mplex.device.message.Subsystem;

/**
 * This interface describes a command that could be sent to the device. It combines the {@link RequestMessage} with the
 * {@link ResultMessage}.
 */
public interface DeviceCommand {

    /**
     * Returns the subsystem of this command. The subsystem is used to configure the request message and validate the
     * result message.
     *
     * @return the subsystem this command is targeted to
     */
    Subsystem getSubsystem();

    /**
     * Returns the topic of the command which will be invoked at the subsystem. The topic is used to configure the
     * request message and validate the result message.
     *
     * @return the topic name of this command
     */
    String getTopic();

    /**
     * Create a request message from this command. The message will be created from the subsystem, the method and all
     * parameters.
     *
     * @return a request message to be sent to the device
     *
     * @throws DeviceMessageException if the request message could not be created, e.g. because of missing properties
     */
    @SuppressWarnings("RedundantThrows")
    RequestMessage toRequestMessage() throws DeviceMessageException;

    /**
     * Populates the results of this command from a result message received from the device. This method also checks if
     * the result message matches the request message.
     *
     * @param message the result message received from the device
     *
     * @throws DeviceMessageException if there is a problem evaluating the result message
     * @throws DeviceCommandException if the device returned with an error
     */
    void fromResultMessage(@NonNull ResultMessage message) throws DeviceMessageException, DeviceCommandException;
}

