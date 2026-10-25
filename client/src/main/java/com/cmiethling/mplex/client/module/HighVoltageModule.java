package com.cmiethling.mplex.client.module;

import org.springframework.stereotype.Service;

import com.cmiethling.mplex.client.model.HighVoltageStatus;
import com.cmiethling.mplex.client.stomp.StompModule;
import com.cmiethling.mplex.client.stomp.SubscriptionRegistry;
import com.cmiethling.mplex.device_events.api.hv.ErrorEvent;
import com.cmiethling.mplex.device_events.api.hv.HighVoltageError;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HighVoltageModule implements StompModule {
    private final HighVoltageStatus highVoltageStatus;

    @Override
    public void register(final SubscriptionRegistry registry) {
        registry.subscribe(ErrorEvent.class, evt -> {
            if (evt.getError() == HighVoltageError.NONE)
                log.info("High voltage error cleared");
            else
                log.warn("High voltage error: {}", evt.getError());

            this.highVoltageStatus.setHighVoltageError(evt.getError());
        });
    }
}
