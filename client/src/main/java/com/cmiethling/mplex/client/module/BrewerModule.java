package com.cmiethling.mplex.client.module;

import org.springframework.stereotype.Service;

import com.cmiethling.mplex.client.model.BrewerStatus;
import com.cmiethling.mplex.client.service.BrowserUpdateService;
import com.cmiethling.mplex.client.stomp.StompModule;
import com.cmiethling.mplex.client.stomp.SubscriptionRegistry;
import com.cmiethling.mplex.device_events.api.brewer.BrewerError;
import com.cmiethling.mplex.device_events.api.brewer.ErrorEvent;
import com.cmiethling.mplex.device_events.api.brewer.StatesEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrewerModule implements StompModule {
    private final BrewerStatus brewerStatus;
    private final BrowserUpdateService browserUpdateService;

    @Override
    public void register(final SubscriptionRegistry registry) {
        registry.subscribe(ErrorEvent.class, evt -> {
            if (evt.getError() == BrewerError.NONE)
                log.info("Brewer error cleared");
            else
                log.warn("Brewer error: {}", evt.getError());
            this.brewerStatus.setErrorsEvent(evt);
            this.browserUpdateService.notifyBrowsers();
        });
        registry.subscribe(StatesEvent.class, evt -> {
            log.info("STATES event: {}", evt);
            evt.isWaterPumpOn().ifPresent(this.brewerStatus::setWaterPump);
            // result of a command: correlates with the commandId from the 202 confirmation
            if (evt.getCommandId() != null)
                this.brewerStatus.completeCommand(evt.getCommandId());
            this.browserUpdateService.notifyBrowsers();
        });
    }
}
