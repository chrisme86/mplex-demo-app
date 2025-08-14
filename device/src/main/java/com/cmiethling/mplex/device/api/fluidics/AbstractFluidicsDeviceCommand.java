package com.cmiethling.mplex.device.api.fluidics;

import java.util.Optional;

import org.springframework.lang.NonNull;

import com.cmiethling.mplex.device.api.AbstractDeviceCommand;
import com.cmiethling.mplex.device.message.Subsystem;

/**
 * Abstract base class for all fluidics commands.
 */
abstract class AbstractFluidicsDeviceCommand extends AbstractDeviceCommand<FluidicsError> {
    // TODO: remove all command related stuff

    /**
     * Creates a new command with the specified name.
     *
     * @param topic the topic name as used in the device communication
     */
    protected AbstractFluidicsDeviceCommand(@NonNull final String topic) {
        super(Subsystem.FLUIDICS, topic);
    }

    @Override
    protected Optional<FluidicsError> getSubsystemError(final int code) {
        return FluidicsError.ofCode(code);
    }
}

