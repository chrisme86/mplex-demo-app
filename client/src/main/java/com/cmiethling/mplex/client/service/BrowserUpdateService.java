package com.cmiethling.mplex.client.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

/**
 * Pushes Server-Sent Events to all open browser pages when a device state changes.
 */
@Service
@Slf4j
public class BrowserUpdateService {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter register() {
        final var emitter = new SseEmitter(0L); // 0 = no timeout, connection stays open
        this.emitters.add(emitter);
        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        emitter.onError(ex -> this.emitters.remove(emitter));
        log.debug("Browser registered for updates, {} open", this.emitters.size());
        // first event opens the stream and lets the browser load the current state (eg after a reconnect)
        sendUpdate(emitter);
        return emitter;
    }

    public void notifyBrowsers() {
        this.emitters.forEach(this::sendUpdate);
    }

    // close open streams before the graceful shutdown, otherwise it waits for them until its timeout (30 s)
    @EventListener(ContextClosedEvent.class)
    public void closeAll() {
        this.emitters.forEach(SseEmitter::complete);
        this.emitters.clear();
    }

    private void sendUpdate(final SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("update").data("update"));
        } catch (final IOException | IllegalStateException e) {
            // browser page was closed
            this.emitters.remove(emitter);
        }
    }
}
