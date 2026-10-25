package com.cmiethling.mplex.client.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
        return emitter;
    }

    public void notifyBrowsers() {
        for (final var emitter : this.emitters) {
            try {
                emitter.send(SseEmitter.event().name("update").data("update"));
            } catch (final IOException | IllegalStateException e) {
                // browser page was closed
                this.emitters.remove(emitter);
            }
        }
    }
}
