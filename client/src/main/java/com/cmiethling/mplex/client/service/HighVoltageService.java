package com.cmiethling.mplex.client.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.cmiethling.mplex.client.model.HighVoltageStatus;
import com.cmiethling.mplex.client_api.api.HvApi;
import com.cmiethling.mplex.client_api.model.ReadCapillaryDataRequest;
import com.cmiethling.mplex.client_api.model.ReadCapillaryDataRequestAllOfParameters;
import com.cmiethling.mplex.device_events.api.SubsystemError;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HighVoltageService {

    @Autowired
    private HighVoltageStatus highVoltageStatus;

    @Autowired
    private HvApi api;

    public SubsystemError getHighVoltageError() {
        return this.highVoltageStatus.getHighVoltageError();
    }

    // ################# commands ##########################
    public void readCapillaryDataCommand(final int index) {
        final var request = new ReadCapillaryDataRequest() //
                .subsystem(ReadCapillaryDataRequest.SubsystemEnum.HV) //
                .topic(ReadCapillaryDataRequest.TopicEnum.READ_CAPILLARY_DATA) //
                .parameters(new ReadCapillaryDataRequestAllOfParameters().index(index));
        log.debug("Sending Request: {}", request);
        try {
            final var response = this.api.readCapillaryDataCommand(request);
            log.debug("Received Response: {}", response);
        } catch (final RestClientException e) {
            log.error("error while receiving response: ", e);
        }
    }
}
