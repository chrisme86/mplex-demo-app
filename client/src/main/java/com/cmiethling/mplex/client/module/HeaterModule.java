package com.cmiethling.mplex.client.module;

import org.springframework.stereotype.Service;

import com.cmiethling.mplex.client.model.HeaterStatus;
import com.cmiethling.mplex.client.service.BrowserUpdateService;
import com.cmiethling.mplex.client.stomp.StompModule;
import com.cmiethling.mplex.client.stomp.SubscriptionRegistry;
import com.cmiethling.mplex.device_events.api.heater.ErrorEvent;
import com.cmiethling.mplex.device_events.api.heater.HeaterError;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HeaterModule implements StompModule {
    private final HeaterStatus heaterStatus;
    private final BrowserUpdateService browserUpdateService;

    @Override
    public void register(final SubscriptionRegistry registry) {
        registry.subscribe(ErrorEvent.class, evt -> {
            if (evt.getError() == HeaterError.NONE)
                log.info("Heater error cleared");
            else
                log.warn("Heater error: {}", evt.getError());

            this.heaterStatus.setHeaterError(evt.getError());
            this.browserUpdateService.notifyBrowsers();
        });
    }
}
