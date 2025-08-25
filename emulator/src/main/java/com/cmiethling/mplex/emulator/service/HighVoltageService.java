package com.cmiethling.mplex.emulator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.cmiethling.mplex.device_events.api.hv.ErrorEvent;
import com.cmiethling.mplex.device_events.api.hv.HighVoltageError;
import com.cmiethling.mplex.emulator.model.HighVoltageStatus;

@Service
public class HighVoltageService {
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private HighVoltageStatus highVoltageStatus;

    public HighVoltageError getHighVoltageError() {
        return this.highVoltageStatus.getHighVoltageError();
    }

    public void processError(@NonNull final String newError) {
        final var error = HighVoltageError.valueOf(newError);
        this.highVoltageStatus.setHighVoltageError(error);
        final var event = new ErrorEvent();
        event.setError(error);
        this.webSocketService.sendEvent(event);
    }
}
