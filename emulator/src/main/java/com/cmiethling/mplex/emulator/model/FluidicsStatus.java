package com.cmiethling.mplex.emulator.model;

import org.springframework.stereotype.Component;

import com.cmiethling.mplex.device_events.api.fluidics.FluidicsError;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Component
public class FluidicsStatus {
    private FluidicsError fluidicsError;
}
