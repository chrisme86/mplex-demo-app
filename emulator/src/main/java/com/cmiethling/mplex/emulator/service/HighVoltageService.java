package com.cmiethling.mplex.emulator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.cmiethling.mplex.device.api.SubsystemError;
import com.cmiethling.mplex.device.api.hv.ErrorEvent;
import com.cmiethling.mplex.device.api.hv.HighVoltageError;
import com.cmiethling.mplex.device.message.Subsystem;
import com.cmiethling.mplex.emulator.model.HighVoltageStatus;

@Service
public class HighVoltageService extends AbstractSubsystem {

    @Autowired
    private HighVoltageStatus highVoltageStatus;

    protected HighVoltageService() {
        super(Subsystem.HIGH_VOLTAGE);
    }

    public SubsystemError getHighVoltageError() {
        return this.highVoltageStatus.getHighVoltageError();
    }

    public void processError(@NonNull final String newError) {
        final var error = HighVoltageError.valueOf(newError);
        final var event = createErrorEvent(error, ErrorEvent.TOPIC, ErrorEvent.ERRORCODE);
        this.highVoltageStatus.setHighVoltageError(error);
        sendEvent(event);
    }
}
