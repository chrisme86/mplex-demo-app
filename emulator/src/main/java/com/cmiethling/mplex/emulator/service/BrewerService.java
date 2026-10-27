package com.cmiethling.mplex.emulator.service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.cmiethling.mplex.device_events.api.brewer.BrewerError;
import com.cmiethling.mplex.device_events.api.brewer.ErrorEvent;
import com.cmiethling.mplex.device_events.api.brewer.StatesEvent;
import com.cmiethling.mplex.device_events.api.heater.HeaterError;
import com.cmiethling.mplex.emulator.model.BrewerStatus;
import com.cmiethling.mplex.emulator.model.HeaterStatus;

@Service
public class BrewerService {

    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private BrewerStatus brewerStatus;
    @Autowired
    private HeaterStatus heaterStatus;
    @Autowired
    private BrowserUpdateService browserUpdateService;

    // the device needs some time to execute a command
    private final Executor device = CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS);

    public BrewerError getBrewerError() {
        return this.brewerStatus.getBrewerError();
    }

    public BrewerStatus getBrewerStatus() {
        return this.brewerStatus;
    }

    public void processError(@NonNull final String newError) {
        final var error = BrewerError.valueOf(newError);
        this.brewerStatus.setBrewerError(error);

        final var event = new ErrorEvent();
        event.setError(error);
        this.webSocketService.sendEvent(event);
        switchOffWaterPumpIfBlocked();
    }

    /**
     * The pump switches itself off on a blocking error, also of the heater (no cold coffee). Sent as StatesEvent
     * without commandId, as no command caused it.
     */
    public void switchOffWaterPumpIfBlocked() {
        if (waterPumpBlockedReason().isEmpty() || !this.brewerStatus.isWaterPumpOn())
            return;
        this.brewerStatus.setWaterPumpOn(false);
        final var event = new StatesEvent();
        event.setWaterPumpOn(false);
        sendStatesEvent(event);
    }

    // pump can't be switched on again until the error is cleared
    private Optional<String> waterPumpBlockedReason() {
        if (this.brewerStatus.getBrewerError() == BrewerError.PUMP_JAMMED)
            return Optional.of("Water pump is jammed");
        if (this.heaterStatus.getHeaterError() == HeaterError.TOO_COLD)
            return Optional.of("Water is too cold");
        return Optional.empty();
    }

    /**
     * Checks the command and returns its ID at once, the result follows as StatesEvent with this ID.
     */
    public UUID setWaterPump(final boolean isOn) {
        if (isOn)
            waterPumpBlockedReason().ifPresent(reason -> {
                throw new DeviceStateConflictException(reason);
            });
        if (isOn && !this.brewerStatus.isBrewValveOpen())
            throw new DeviceStateConflictException("Brew valve is closed");

        final var commandId = UUID.randomUUID();
        this.device.execute(() -> {
            // the error may have occurred while the command was executed: the result is the real state
            final var on = isOn && waterPumpBlockedReason().isEmpty();
            this.brewerStatus.setWaterPumpOn(on);
            final var event = new StatesEvent();
            event.setWaterPumpOn(on);
            event.setCommandId(commandId);
            sendStatesEvent(event);
        });
        return commandId;
    }

    /**
     * Checks the command and returns its ID at once, the result follows as StatesEvent with this ID.
     */
    public UUID setBrewValve(final boolean isOpen) {
        if (!isOpen && this.brewerStatus.isWaterPumpOn())
            throw new DeviceStateConflictException("Water pump is running");

        final var commandId = UUID.randomUUID();
        this.device.execute(() -> {
            this.brewerStatus.setBrewValveOpen(isOpen);
            final var event = new StatesEvent();
            event.setBrewValveOpen(isOpen);
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
