package com.cmiethling.mplex.emulator.api.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.cmiethling.mplex.emulator.service.FluidicsService;
import com.cmiethling.mplex.server_api.api.FluidicsApi;
import com.cmiethling.mplex.server_api.model.SetGelPumpRequest;
import com.cmiethling.mplex.server_api.model.SetGelPumpResponse;
import com.cmiethling.mplex.server_api.model.SetGelPumpResponseAllOfResult;
import com.cmiethling.mplex.server_api.model.SetGelValveRequest;
import com.cmiethling.mplex.server_api.model.SetGelValveResponse;
import com.cmiethling.mplex.server_api.model.SetGelValveResponseAllOfResult;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class FluidicsRestController implements FluidicsApi {

    @Autowired
    private FluidicsService fluidicsService;

    @Override
    public ResponseEntity<SetGelPumpResponse> setGelPumpCommand(final SetGelPumpRequest req) {
        log.info("Received request: {}", req);
        // TODO handle request, eg with 25% error...
        final var isOn = req.getParameters().getIsOn();
        this.fluidicsService.setGelPump(isOn);

        final var response = new SetGelPumpResponse() //
                // TODO: validate subsystem, topic, parameters... (req.getSubsystem().getValue().equals
                //  (responseSubsystem)) >> false >> return 400 (BadRequest)
                .error(SetGelPumpResponse.ErrorEnum.NO_ERROR) //
                .result(new SetGelPumpResponseAllOfResult().isOn(isOn));
        log.info("Sending response: {}", response);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<SetGelValveResponse> setGelValveCommand(final SetGelValveRequest req) {
        log.info("Received request: {}", req);
        final var isOpen = req.getParameters().getIsOpen();
        this.fluidicsService.setGelValve(isOpen);

        final var response = new SetGelValveResponse() //
                .error(SetGelValveResponse.ErrorEnum.NO_ERROR) //
                .result(new SetGelValveResponseAllOfResult().isOpen(isOpen));
        log.info("Sending response: {}", response);

        return ResponseEntity.ok(response);
    }
}
