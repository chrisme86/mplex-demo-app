package com.cmiethling.mplex.emulator.api.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.cmiethling.mplex.emulator.service.HeaterService;
import com.cmiethling.mplex.server_api.api.HeaterApi;
import com.cmiethling.mplex.server_api.model.CommandAccepted;
import com.cmiethling.mplex.server_api.model.SetWaterHeaterRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * Commands are only confirmed here (202 + commandId), the result is sent as StatesEvent with the same commandId.
 */
@RestController
@Slf4j
public class HeaterRestController implements HeaterApi {

    @Autowired
    private HeaterService heaterService;

    @Override
    public ResponseEntity<CommandAccepted> setWaterHeaterCommand(final SetWaterHeaterRequest req) {
        log.info("Received request: {}", req);
        final var commandId = this.heaterService.setWaterHeater(req.getParameters().getIsOn());
        log.info("Accepted SetWaterHeater: {}", commandId);
        return ResponseEntity.accepted().body(new CommandAccepted().commandId(commandId));
    }
}
