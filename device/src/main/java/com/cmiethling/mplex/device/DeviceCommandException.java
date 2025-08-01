package com.cmiethling.mplex.device;

import org.springframework.lang.NonNull;

import com.cmiethling.mplex.device.message.AbstractDeviceMessage;
import com.cmiethling.mplex.device.message.Subsystem;

/**
 * Creates a new exception for a known command error.
 */
public final class DeviceCommandException extends DeviceException {

    /**
     * Creates a new exception for a known command error.
     *
     * @param subsystem the subsystem
     * @param topic     the topic
     * @param code      the command error
     */
    public DeviceCommandException(@NonNull final Subsystem subsystem, @NonNull final String topic,
            @NonNull final String code, final Object... args) {
        super(createMessage(subsystem, topic, code), args);
    }

    private static String createMessage(final Subsystem subsystem, final String topic, final String code) {
        return "%s=%s, %s=%s: %s".formatted(AbstractDeviceMessage.SYSTEM, subsystem, AbstractDeviceMessage.TOPIC, topic,
                code);
    }

    @Override
    public String getId() {
        return "CMD";
    }
}