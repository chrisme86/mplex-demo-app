package com.cmiethling.mplex.emulator.model;

import org.springframework.stereotype.Component;

import com.cmiethling.mplex.device_events.api.brewer.BrewerError;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Component
public class BrewerStatus {
    private BrewerError brewerError = BrewerError.NONE; // no error at start, so the current state can be sent
    private boolean waterPumpOn;
}
