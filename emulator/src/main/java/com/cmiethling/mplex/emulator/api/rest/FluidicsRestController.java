package com.cmiethling.mplex.emulator.api.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.cmiethling.mplex.server_api.api.FluidicsApi;
import com.cmiethling.mplex.server_api.model.SetGelPumpRequest;
import com.cmiethling.mplex.server_api.model.SetGelPumpResponse;
import com.cmiethling.mplex.server_api.model.SetGelPumpResponseAllOfResult;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class FluidicsRestController implements FluidicsApi {

    @Override
    public ResponseEntity<SetGelPumpResponse> setGelPumpCommand(final SetGelPumpRequest req) {
        log.info("Received request: {}", req);
        // TODO handle request, eg with 25% error...
        final var response = new SetGelPumpResponse() //
                // TODO: validate subsystem, topic, parameters... (req.getSubsystem().getValue().equals
                //  (responseSubsystem)) >> false >> return 400 (BadRequest)
                .error(SetGelPumpResponse.ErrorEnum.NO_ERROR) //
                .result(new SetGelPumpResponseAllOfResult().isOn(req.getParameters().getIsOn()));
        log.info("Sending response: {}", response);

        return ResponseEntity.ok(response);
    }
}