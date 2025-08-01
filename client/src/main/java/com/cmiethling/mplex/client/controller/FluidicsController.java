package com.cmiethling.mplex.client.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.cmiethling.mplex.client_api.openapi.api.FluidicsApi;
import com.cmiethling.mplex.client_api.openapi.model.fluidics.SetGelPumpRequest;
import com.cmiethling.mplex.client_api.openapi.model.fluidics.SetGelPumpResponse;

@RestController
public class FluidicsController implements FluidicsApi {
    public ResponseEntity<SetGelPumpResponse> setGelPumpCommand(final boolean isOn) {
        final var request = new SetGelPumpRequest();
        request.setId(UUID.randomUUID());
        request.getParameters().setIsOn(isOn);
        return sendSetGelPumpCommand(request);
    }

    @Override
    public ResponseEntity<SetGelPumpResponse> sendSetGelPumpCommand(final SetGelPumpRequest setGelPumpRequest) {
        return ResponseEntity.ok(FluidicsApi.super.sendSetGelPumpCommand(setGelPumpRequest).getBody());
    }
}
