package com.cmiethling.mplex.emulator.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.cmiethling.mplex.server_api.api.FluidicsApi;
import com.cmiethling.mplex.server_api.model.fluidics.SetGelPumpRequest;
import com.cmiethling.mplex.server_api.model.fluidics.SetGelPumpResponse;
import com.cmiethling.mplex.server_api.model.fluidics.SetGelPumpResponseAllOfResult;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class FluidicsApiController implements FluidicsApi {

    @Override
    public ResponseEntity<SetGelPumpResponse> setGelPumpCommand(final SetGelPumpRequest req) {
        log.info("Received request: {}", req);
        final var response = new SetGelPumpResponse().id(req.getId())
                .subsystem(SetGelPumpResponse.SubsystemEnum.FLUIDICS).topic(SetGelPumpResponse.TopicEnum.SET_GEL_PUMP)
                .type(SetGelPumpResponse.TypeEnum.RESULT).error(SetGelPumpResponse.ErrorEnum.NO_ERROR)
                .result(new SetGelPumpResponseAllOfResult().isOn(req.getParameters().getIsOn()));
        log.info("Sending response: {}", response);

        return ResponseEntity.ok(response);
    }
}