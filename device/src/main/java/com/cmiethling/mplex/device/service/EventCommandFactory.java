package com.cmiethling.mplex.device.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.cmiethling.mplex.device.api.DeviceCommand;
import com.cmiethling.mplex.device.api.DeviceEvent;

/**
 * Service to return a Command or an Event.
 */
@Service
public class EventCommandFactory {
    /**
     * Returns the desired command.
     *
     * @param commandClass the class for the desired command
     *
     * @return the command
     */
    public <T extends DeviceCommand> T command(@NonNull final Class<T> commandClass) {
        try {
            return commandClass.getDeclaredConstructor().newInstance();
        } catch (final Exception ex) {
            throw new RuntimeException("Error creating command instance", ex);
        }
    }

    /**
     * Returns the desired event.
     *
     * @param eventClass the class for the desired event
     *
     * @return the event
     */
    public <T extends DeviceEvent> T event(@NonNull final Class<T> eventClass) {
        try {
            return eventClass.getDeclaredConstructor().newInstance();
        } catch (final Exception ex) {
            throw new RuntimeException("Error creating event instance", ex);
        }
    }
}
