package com.cmiethling.mplex.client.stomp;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompConnection {
    // Components
    private final WebSocketStompClient client;
    private final ThreadPoolTaskScheduler scheduler;
    private final SubscriptionRegistry registry;
    private final List<StompModule> modules; // @Service + implements StompModule

    @Value("${stomp.url}")
    String url;
    private final AtomicInteger retries = new AtomicInteger();

    @PostConstruct
    public void start() {
        connect();
    }

    void connect() {
        log.info("Connecting to STOMP {}", this.url);
        this.client.connectAsync(this.url, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(final StompSession s, final StompHeaders h) {
                StompConnection.this.retries.set(0);
                StompConnection.this.registry.bindSession(s);
                // first-time register
                StompConnection.this.modules.forEach(m -> m.register(StompConnection.this.registry));

                log.info("STOMP connected: {}", s.getSessionId());
                log.info("Destinations Subscribed:\n{}", StompConnection.this.registry.destinations());
            }

            @Override
            public void handleTransportError(final StompSession s, final Throwable ex) {
                log.error("STOMP transport error:", ex);
                // FIXME: needs to delete and resubscribe all topics
                // scheduleReconnect();
            }

            @Override
            public void handleException(final StompSession session, @Nullable final StompCommand command,
                    final StompHeaders headers, final byte[] payload, final Throwable exception) {
                log.error("STOMP exception: command={}, headers={}, payload={}, error={}", command, headers,
                        new String(payload), exception.getMessage(), exception);
            }
        }).whenComplete((session, ex) -> {
            if (ex != null) {
                log.error("STOMP connection error:", ex);
                scheduleReconnect();
            }
        });
    }

    void scheduleReconnect() {
        final long backoff = Math.min(30_000L, (long) (1000L * Math.pow(2, this.retries.getAndIncrement())));
        this.scheduler.schedule(this::connect, Instant.now().plusMillis(backoff));
    }
}
