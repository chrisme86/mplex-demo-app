package com.cmiethling.mplex.client.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.cmiethling.mplex.client.model.FluidicsStatus;
import com.cmiethling.mplex.client_api.api.FluidicsApi;
import com.cmiethling.mplex.client_api.model.SetGelPumpRequest;
import com.cmiethling.mplex.client_api.model.SetGelPumpRequestAllOfParameters;
import com.cmiethling.mplex.client_api.model.SetGelValveRequest;
import com.cmiethling.mplex.client_api.model.SetGelValveRequestAllOfParameters;

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
            throw toDeviceCommandException("SetGelPump", e);
        }
    }

    public void setGelValveCommand(final boolean isOpen) {
        final var request = new SetGelValveRequest() //
                .parameters(new SetGelValveRequestAllOfParameters().isOpen(isOpen));
        log.info("Sending Request: {}", request);
        try {
            final var response = this.fluidicsApi.setGelValveCommand(request);
            log.info("Received Response: {}", response);
            this.fluidicsStatus.setGelValve(response.getResult().getIsOpen());
        } catch (final RestClientException e) {
            throw toDeviceCommandException("SetGelValve", e);
        }
    }

    // device answered with an error (eg 409 as ProblemDetail) or was not reachable
    private DeviceCommandException toDeviceCommandException(final String command, final RestClientException e) {
        if (e instanceof final RestClientResponseException responseException) {
            final var problem = responseException.getResponseBodyAs(ProblemDetail.class);
            final var detail = problem != null && problem.getDetail() != null ? problem.getDetail()
                    : responseException.getStatusText();
            log.warn("{} rejected with {}: {}", command, responseException.getStatusCode(), detail);
            return new DeviceCommandException("%s rejected: %s".formatted(command, detail), e);
        }
        log.error("{} failed: ", command, e);
        return new DeviceCommandException("%s failed: device not reachable".formatted(command), e);
    }
}
