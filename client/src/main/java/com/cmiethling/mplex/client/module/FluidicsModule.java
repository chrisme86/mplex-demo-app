package com.cmiethling.mplex.client.module;

import org.springframework.stereotype.Service;

import com.cmiethling.mplex.client.stomp.StompModule;
import com.cmiethling.mplex.client.stomp.SubscriptionRegistry;
import com.cmiethling.mplex.device_events.api.fluidics.ErrorEvent;
import com.cmiethling.mplex.device_events.api.fluidics.StatesEvent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FluidicsModule implements StompModule {
    @Override
    public void register(final SubscriptionRegistry registry) {
        registry.subscribe(ErrorEvent.class, evt -> log.warn("ERROR event: {}", evt));
        registry.subscribe(StatesEvent.class, evt -> log.info(evt.toString()));
    }
}
