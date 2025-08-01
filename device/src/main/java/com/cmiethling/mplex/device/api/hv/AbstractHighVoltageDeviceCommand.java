package com.cmiethling.mplex.device.api.hv;

import java.util.Optional;

import org.springframework.lang.NonNull;

import com.cmiethling.mplex.device.api.AbstractDeviceCommand;
import com.cmiethling.mplex.device.message.Subsystem;

/**
 * Abstract base class for all cartridge commands.
 */
abstract class AbstractHighVoltageDeviceCommand extends AbstractDeviceCommand<HighVoltageError> {

    /**
     * Creates a new command with the specified name.
     *
     * @param topic the topic name as used in the device communication
     */
    protected AbstractHighVoltageDeviceCommand(@NonNull final String topic) {
        super(Subsystem.HIGH_VOLTAGE, topic);
    }

    @Override
    protected Optional<HighVoltageError> getSubsystemError(final int code) {
        return HighVoltageError.ofCode(code);
    }
}

