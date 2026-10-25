package com.cmiethling.mplex.client.module;

import org.springframework.stereotype.Service;

import com.cmiethling.mplex.client.model.FluidicsStatus;
import com.cmiethling.mplex.client.service.BrowserUpdateService;
import com.cmiethling.mplex.client.stomp.StompModule;
import com.cmiethling.mplex.client.stomp.SubscriptionRegistry;
import com.cmiethling.mplex.device_events.api.fluidics.ErrorEvent;
import com.cmiethling.mplex.device_events.api.fluidics.FluidicsError;
import com.cmiethling.mplex.device_events.api.fluidics.StatesEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FluidicsModule implements StompModule {
    private final FluidicsStatus fluidicsStatus;
    private final BrowserUpdateService browserUpdateService;

    @Override
    public void register(final SubscriptionRegistry registry) {
        registry.subscribe(ErrorEvent.class, evt -> {
            if (evt.getError() == FluidicsError.NONE)
                log.info("Fluidics error cleared");
            else
                log.warn("Fluidics error: {}", evt.getError());
            this.fluidicsStatus.setErrorsEvent(evt);
            this.browserUpdateService.notifyBrowsers();
        });
        registry.subscribe(StatesEvent.class, evt -> {
            log.info("STATES event: {}", evt);
            // a StatesEvent may contain only one of the states
            evt.isGelPumpOn().ifPresent(this.fluidicsStatus::setGelPump);
            evt.isGelValveOpen().ifPresent(this.fluidicsStatus::setGelValve);
            this.browserUpdateService.notifyBrowsers();
        });
    }
}
