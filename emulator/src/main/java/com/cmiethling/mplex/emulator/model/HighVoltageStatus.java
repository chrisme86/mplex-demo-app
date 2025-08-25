package com.cmiethling.mplex.emulator.model;

import org.springframework.stereotype.Component;

import com.cmiethling.mplex.device_events.api.hv.HighVoltageError;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Component
public class HighVoltageStatus {
    private HighVoltageError highVoltageError;
}
