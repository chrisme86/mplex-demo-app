package com.cmiethling.mplex.client.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cmiethling.mplex.client.model.BrewerStatus;
import com.cmiethling.mplex.client_api.api.BrewerApi;
import com.cmiethling.mplex.client_api.model.SetWaterPumpRequest;
import com.cmiethling.mplex.client_api.model.SetWaterPumpRequestAllOfParameters;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@Service
public class BrewerService {

    @Autowired
    private BrewerApi brewerApi;
    @Autowired
    private CommandSender commandSender;

    // ################# current states ##########################
    @Autowired
    private BrewerStatus brewerStatus;

    // ################# commands ##########################
    public void setWaterPumpCommand(final boolean isOn) {
        final var request = new SetWaterPumpRequest() //
                .parameters(new SetWaterPumpRequestAllOfParameters().isOn(isOn));
        log.info("Sending Request: {}", request);
        this.commandSender.send("SetWaterPump", this.brewerStatus.getWaterPump(),
                () -> this.brewerApi.setWaterPumpCommand(request));
    }
}
