package com.cmiethling.mplex.emulator.model;

import org.springframework.stereotype.Component;

import com.cmiethling.mplex.device_events.api.heater.HeaterError;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Component
public class HeaterStatus {
    private HeaterError heaterError = HeaterError.NONE; // no error at start, so the current state can be sent
}
