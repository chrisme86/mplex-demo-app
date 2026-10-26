package com.cmiethling.mplex.emulator.api.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.cmiethling.mplex.emulator.service.FluidicsService;
import com.cmiethling.mplex.server_api.api.FluidicsApi;
import com.cmiethling.mplex.server_api.model.CommandAccepted;
import com.cmiethling.mplex.server_api.model.SetGelPumpRequest;
import com.cmiethling.mplex.server_api.model.SetGelValveRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * Commands are only confirmed here (202 + commandId), the result is sent as StatesEvent with the same commandId.
 */
@RestController
@Slf4j
public class FluidicsRestController implements FluidicsApi {

    @Autowired
    private FluidicsService fluidicsService;

    @Override
    public ResponseEntity<CommandAccepted> setGelPumpCommand(final SetGelPumpRequest req) {
        log.info("Received request: {}", req);
        // TODO handle request, eg with 25% error...
        final var commandId = this.fluidicsService.setGelPump(req.getParameters().getIsOn());
        log.info("Accepted SetGelPump: {}", commandId);
        return ResponseEntity.accepted().body(new CommandAccepted().commandId(commandId));
    }

    @Override
    public ResponseEntity<CommandAccepted> setGelValveCommand(final SetGelValveRequest req) {
        log.info("Received request: {}", req);
        final var commandId = this.fluidicsService.setGelValve(req.getParameters().getIsOpen());
        log.info("Accepted SetGelValve: {}", commandId);
        return ResponseEntity.accepted().body(new CommandAccepted().commandId(commandId));
    }
}
