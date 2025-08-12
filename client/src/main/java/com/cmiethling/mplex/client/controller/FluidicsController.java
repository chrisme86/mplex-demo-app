package com.cmiethling.mplex.client.controller;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class FluidicsController /* implements FluidicsApi */ {
    // public ResponseEntity<SetGelPumpResponse> setGelPumpCommand(final boolean isOn) {
    //     final var request = new SetGelPumpRequest();
    //     request.setId(UUID.randomUUID());
    //     request.getParameters().setIsOn(isOn);
    //     request.setSubsystem(SetGelPumpRequest.SubsystemEnum.FLUIDICS);
    //     return setGelPumpCommand(request);
    // }

    // @Override
    // public ResponseEntity<SetGelPumpResponse> setGelPumpCommand(final SetGelPumpRequest setGelPumpRequest) {
    //     return ResponseEntity.ok(FluidicsApi.super.setGelPumpCommand(setGelPumpRequest).getBody());
    // }
}
