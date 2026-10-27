package com.cmiethling.mplex.client.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cmiethling.mplex.client.model.HeaterStatus;
import com.cmiethling.mplex.device_events.api.SubsystemError;

@Service
public class HeaterService {

    @Autowired
    private HeaterStatus heaterStatus;

    public SubsystemError getHeaterError() {
        return this.heaterStatus.getHeaterError();
    }

    public HeaterStatus getHeaterStatus() {
        return this.heaterStatus;
    }
}
