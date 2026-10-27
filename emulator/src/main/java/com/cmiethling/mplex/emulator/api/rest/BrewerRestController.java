package com.cmiethling.mplex.emulator.api.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.cmiethling.mplex.emulator.service.BrewerService;
import com.cmiethling.mplex.server_api.api.BrewerApi;
import com.cmiethling.mplex.server_api.model.CommandAccepted;
import com.cmiethling.mplex.server_api.model.SetWaterPumpRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * Commands are only confirmed here (202 + commandId), the result is sent as StatesEvent with the same commandId.
 */
@RestController
@Slf4j
public class BrewerRestController implements BrewerApi {

    @Autowired
    private BrewerService brewerService;

    @Override
    public ResponseEntity<CommandAccepted> setWaterPumpCommand(final SetWaterPumpRequest req) {
        log.info("Received request: {}", req);
        final var commandId = this.brewerService.setWaterPump(req.getParameters().getIsOn());
        log.info("Accepted SetWaterPump: {}", commandId);
        return ResponseEntity.accepted().body(new CommandAccepted().commandId(commandId));
    }
}
