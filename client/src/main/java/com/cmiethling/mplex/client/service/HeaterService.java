package com.cmiethling.mplex.client.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cmiethling.mplex.client.model.HeaterStatus;
import com.cmiethling.mplex.client_api.api.HeaterApi;
import com.cmiethling.mplex.client_api.model.SetWaterHeaterRequest;
import com.cmiethling.mplex.client_api.model.SetWaterHeaterRequestAllOfParameters;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@Service
public class HeaterService {

    @Autowired
    private HeaterApi heaterApi;
    @Autowired
    private CommandSender commandSender;

    // ################# current states ##########################
    @Autowired
    private HeaterStatus heaterStatus;

    // ################# commands ##########################
    public void setWaterHeaterCommand(final boolean isOn) {
        final var request = new SetWaterHeaterRequest() //
                .parameters(new SetWaterHeaterRequestAllOfParameters().isOn(isOn));
        log.info("Sending Request: {}", request);
        this.commandSender.send("SetWaterHeater", this.heaterStatus.getWaterHeater(),
                () -> this.heaterApi.setWaterHeaterCommand(request));
    }
}
