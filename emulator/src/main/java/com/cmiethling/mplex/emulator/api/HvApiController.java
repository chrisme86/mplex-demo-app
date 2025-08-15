package com.cmiethling.mplex.emulator.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.cmiethling.mplex.server_api.api.HvApi;
import com.cmiethling.mplex.server_api.model.ReadCapillaryDataRequest;
import com.cmiethling.mplex.server_api.model.ReadCapillaryDataResponse;
import com.cmiethling.mplex.server_api.model.ReadCapillaryDataResponseAllOfResult;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class HvApiController implements HvApi {

    @Override
    public ResponseEntity<ReadCapillaryDataResponse> readCapillaryDataCommand(final ReadCapillaryDataRequest req) {
        log.info("Received request: {}", req);
        final var response = new ReadCapillaryDataResponse() //
                .subsystem(ReadCapillaryDataResponse.SubsystemEnum.HV) //
                .topic(ReadCapillaryDataResponse.TopicEnum.READ_CAPILLARY_DATA) //
                .error(ReadCapillaryDataResponse.ErrorEnum.NO_ERROR) //
                .result(new ReadCapillaryDataResponseAllOfResult().badRuns(1)
                        .capState(ReadCapillaryDataResponseAllOfResult.CapStateEnum.GOOD));
        log.info("Sending response: {}", response);

        return ResponseEntity.ok(response);
    }
}