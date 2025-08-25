package com.cmiethling.mplex.emulator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.cmiethling.mplex.device_events.api.fluidics.ErrorEvent;
import com.cmiethling.mplex.device_events.api.fluidics.FluidicsError;
import com.cmiethling.mplex.emulator.model.FluidicsStatus;

@Service
public class FluidicsService {

    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private FluidicsStatus fluidicsStatus;

    public FluidicsError getFluidicsError() {
        return this.fluidicsStatus.getFluidicsError();
    }

    public void processError(@NonNull final String newError) {
        final var error = FluidicsError.valueOf(newError);
        this.fluidicsStatus.setFluidicsError(error);

        final var event = new ErrorEvent();
        event.setError(error);
        this.webSocketService.sendEvent(event);
    }
}
