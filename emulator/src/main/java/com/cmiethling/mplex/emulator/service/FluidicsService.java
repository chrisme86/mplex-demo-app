package com.cmiethling.mplex.emulator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.cmiethling.mplex.device_events.api.fluidics.ErrorEvent;
import com.cmiethling.mplex.device_events.api.fluidics.FluidicsError;
import com.cmiethling.mplex.device_events.api.fluidics.StatesEvent;
import com.cmiethling.mplex.emulator.model.FluidicsStatus;

@Service
public class FluidicsService {

    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private FluidicsStatus fluidicsStatus;
    @Autowired
    private BrowserUpdateService browserUpdateService;

    public FluidicsError getFluidicsError() {
        return this.fluidicsStatus.getFluidicsError();
    }

    public FluidicsStatus getFluidicsStatus() {
        return this.fluidicsStatus;
    }

    public void processError(@NonNull final String newError) {
        final var error = FluidicsError.valueOf(newError);
        this.fluidicsStatus.setFluidicsError(error);

        final var event = new ErrorEvent();
        event.setError(error);
        this.webSocketService.sendEvent(event);
    }

    public void setGelPump(final boolean isOn) {
        if (isOn && !this.fluidicsStatus.isGelValveOpen())
            throw new DeviceStateConflictException("Gel valve is closed");
        this.fluidicsStatus.setGelPumpOn(isOn);

        final var event = new StatesEvent();
        event.setGelPumpOn(isOn);
        this.webSocketService.sendEvent(event);
        this.browserUpdateService.notifyBrowsers();
    }

    public void setGelValve(final boolean isOpen) {
        if (!isOpen && this.fluidicsStatus.isGelPumpOn())
            throw new DeviceStateConflictException("Gel pump is running");
        this.fluidicsStatus.setGelValveOpen(isOpen);

        final var event = new StatesEvent();
        event.setGelValveOpen(isOpen);
        this.webSocketService.sendEvent(event);
        this.browserUpdateService.notifyBrowsers();
    }
}
