package com.cmiethling.mplex.emulator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.cmiethling.mplex.device_events.api.heater.ErrorEvent;
import com.cmiethling.mplex.device_events.api.heater.HeaterError;
import com.cmiethling.mplex.emulator.model.HeaterStatus;

@Service
public class HeaterService {
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private HeaterStatus heaterStatus;
    @Autowired
    private BrewerService brewerService;

    public HeaterError getHeaterError() {
        return this.heaterStatus.getHeaterError();
    }

    public void processError(@NonNull final String newError) {
        final var error = HeaterError.valueOf(newError);
        this.heaterStatus.setHeaterError(error);
        final var event = new ErrorEvent();
        event.setError(error);
        this.webSocketService.sendEvent(event);
        this.brewerService.switchOffWaterPumpIfBlocked(); // eg TOO_COLD
    }
}
