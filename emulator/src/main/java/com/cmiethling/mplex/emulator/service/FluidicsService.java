package com.cmiethling.mplex.emulator.service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

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

    // the device needs some time to execute a command
    private final Executor device = CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS);

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

    /**
     * Checks the command and returns its ID at once, the result follows as StatesEvent with this ID.
     */
    public UUID setGelPump(final boolean isOn) {
        if (isOn && !this.fluidicsStatus.isGelValveOpen())
            throw new DeviceStateConflictException("Gel valve is closed");

        final var commandId = UUID.randomUUID();
        this.device.execute(() -> {
            this.fluidicsStatus.setGelPumpOn(isOn);
            final var event = new StatesEvent();
            event.setGelPumpOn(isOn);
            event.setCommandId(commandId);
            sendStatesEvent(event);
        });
        return commandId;
    }

    /**
     * Checks the command and returns its ID at once, the result follows as StatesEvent with this ID.
     */
    public UUID setGelValve(final boolean isOpen) {
        if (!isOpen && this.fluidicsStatus.isGelPumpOn())
            throw new DeviceStateConflictException("Gel pump is running");

        final var commandId = UUID.randomUUID();
        this.device.execute(() -> {
            this.fluidicsStatus.setGelValveOpen(isOpen);
            final var event = new StatesEvent();
            event.setGelValveOpen(isOpen);
            event.setCommandId(commandId);
            sendStatesEvent(event);
        });
        return commandId;
    }

    private void sendStatesEvent(final StatesEvent event) {
        this.webSocketService.sendEvent(event);
        this.browserUpdateService.notifyBrowsers();
    }
}
