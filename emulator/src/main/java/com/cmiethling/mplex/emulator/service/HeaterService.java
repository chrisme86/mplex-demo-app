package com.cmiethling.mplex.emulator.service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.cmiethling.mplex.device_events.api.heater.ErrorEvent;
import com.cmiethling.mplex.device_events.api.heater.HeaterError;
import com.cmiethling.mplex.device_events.api.heater.StatesEvent;
import com.cmiethling.mplex.emulator.model.BrewerStatus;
import com.cmiethling.mplex.emulator.model.HeaterStatus;

@Service
public class HeaterService {
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private HeaterStatus heaterStatus;
    @Autowired
    private BrewerStatus brewerStatus;
    @Autowired
    private BrewerService brewerService;
    @Autowired
    private BrowserUpdateService browserUpdateService;

    // the device needs some time to execute a command
    private final Executor device = CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS);

    public HeaterError getHeaterError() {
        return this.heaterStatus.getHeaterError();
    }

    public HeaterStatus getHeaterStatus() {
        return this.heaterStatus;
    }

    public void processError(@NonNull final String newError) {
        final var error = HeaterError.valueOf(newError);
        this.heaterStatus.setHeaterError(error);
        final var event = new ErrorEvent();
        event.setError(error);
        this.webSocketService.sendEvent(event);
        switchOffWaterHeaterIfOverheated();
    }

    /**
     * The heater switches itself off when overheated, and with it the pump (no cold coffee). Sent as StatesEvent
     * without commandId, as no command caused it.
     */
    private void switchOffWaterHeaterIfOverheated() {
        if (this.heaterStatus.getHeaterError() != HeaterError.OVERHEATED || !this.heaterStatus.isWaterHeaterOn())
            return;
        this.heaterStatus.setWaterHeaterOn(false);
        final var event = new StatesEvent();
        event.setWaterHeaterOn(false);
        sendStatesEvent(event);
        this.brewerService.switchOffWaterPumpIfBlocked();
    }

    /**
     * Checks the command and returns its ID at once, the result follows as StatesEvent with this ID.
     */
    public UUID setWaterHeater(final boolean isOn) {
        if (isOn && this.heaterStatus.getHeaterError() == HeaterError.OVERHEATED)
            throw new DeviceStateConflictException("Water heater is overheated");
        if (!isOn && this.brewerStatus.isWaterPumpOn())
            throw new DeviceStateConflictException("Water pump is running");

        final var commandId = UUID.randomUUID();
        this.device.execute(() -> {
            // the state may have changed while the command was executed: the result is the real state
            final var on = isOn && this.heaterStatus.getHeaterError() != HeaterError.OVERHEATED;
            this.heaterStatus.setWaterHeaterOn(on);
            final var event = new StatesEvent();
            event.setWaterHeaterOn(on);
            event.setCommandId(commandId);
            sendStatesEvent(event);
            this.brewerService.switchOffWaterPumpIfBlocked(); // eg pump was switched on in the meantime
        });
        return commandId;
    }

    private void sendStatesEvent(final StatesEvent event) {
        this.webSocketService.sendEvent(event);
        this.browserUpdateService.notifyBrowsers();
    }
}
