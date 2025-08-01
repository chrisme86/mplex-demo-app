package com.cmiethling.mplex.emulator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.cmiethling.mplex.device.api.SubsystemError;
import com.cmiethling.mplex.device.api.fluidics.ErrorEvent;
import com.cmiethling.mplex.device.api.fluidics.FluidicsError;
import com.cmiethling.mplex.device.message.Subsystem;
import com.cmiethling.mplex.emulator.model.FluidicsStatus;

@Service
public class FluidicsService extends AbstractSubsystem {

    @Autowired
    private FluidicsStatus fluidicsStatus;

    protected FluidicsService() {
        super(Subsystem.FLUIDICS);
    }

    public SubsystemError getFluidicsError() {
        return this.fluidicsStatus.getFluidicsError();
    }

    public void processError(@NonNull final String newError) {
        final var error = FluidicsError.valueOf(newError);
        final var event = createErrorEvent(error, ErrorEvent.TOPIC, ErrorEvent.ERRORCODE);
        this.fluidicsStatus.setFluidicsError(error);
        sendEvent(event);
    }
}
