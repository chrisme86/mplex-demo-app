package com.cmiethling.mplex.client.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.cmiethling.mplex.client.model.FluidicsStatus;
import com.cmiethling.mplex.client_api.api.FluidicsApi;
import com.cmiethling.mplex.client_api.model.SetGelPumpRequest;
import com.cmiethling.mplex.client_api.model.SetGelPumpRequestAllOfParameters;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@Service
public class FluidicsService {

    @Autowired
    private FluidicsApi fluidicsApi;

    // ################# current states ##########################
    @Autowired
    private FluidicsStatus fluidicsStatus;

    // ################# commands ##########################
    public void setGelPumpCommand(final boolean isOn) {
        final var request = new SetGelPumpRequest() //
                .parameters(new SetGelPumpRequestAllOfParameters().isOn(isOn));
        log.info("Sending Request: {}", request);
        try {
            final var response = this.fluidicsApi.setGelPumpCommand(request);
            log.info("Received Response: {}", response);
            this.fluidicsStatus.setGelPump(response.getResult().getIsOn());
        } catch (final RestClientException e) {
            log.error("error while receiving response: ", e);
        }
    }
}
