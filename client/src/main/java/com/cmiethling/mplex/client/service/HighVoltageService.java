package com.cmiethling.mplex.client.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cmiethling.mplex.client.model.HighVoltageStatus;
import com.cmiethling.mplex.device.api.SubsystemError;

@Service
public class HighVoltageService {

    @Autowired
    private HighVoltageStatus highVoltageStatus;

    public SubsystemError getHighVoltageError() {
        return this.highVoltageStatus.getHighVoltageError();
    }
}
