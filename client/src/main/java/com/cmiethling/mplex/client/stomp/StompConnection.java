package com.cmiethling.mplex.client.stomp;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.cmiethling.mplex.client.service.BrowserUpdateService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompConnection {
    public static final int MAX_RETRIES = 3;
    private static final long FIRST_RETRY_DELAY_MS = 2000; // then 4 s, 8 s

    // Components
    private final WebSocketStompClient client;
    private final ThreadPoolTaskScheduler scheduler;
    private final SubscriptionRegistry registry;
    private final List<StompModule> modules; // @Service + implements StompModule
    private final BrowserUpdateService browserUpdateService;

    @Value("${stomp.url}")
    String url;
    private final AtomicInteger retries = new AtomicInteger();
    // connect failure and transport error can both report the same lost connection
    private final AtomicBoolean reconnectScheduled = new AtomicBoolean();
    private volatile boolean stopping;
    @Getter
    private volatile ConnectionState state = ConnectionState.CONNECTING;

    @PostConstruct
    public void start() {
        connect();
    }

    @PreDestroy
    public void stop() {
        this.stopping = true; // no reconnect when the client itself shuts down
    }

    /**
     * Manual reconnect (eg button in the service client) after all retries failed.
     */
    public void reconnect() {
        if (this.state != ConnectionState.DISCONNECTED)
            return;
        this.retries.set(0);
        this.reconnectScheduled.set(false);
        connect();
    }

    /**
     * Number of the current retry, 0 if it is the first attempt.
     */
    public int getRetry() {
        return this.retries.get();
    }

    void connect() {
        log.info("Connecting to STOMP {}", this.url);
        setState(ConnectionState.CONNECTING);
        this.client.connectAsync(this.url, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(final StompSession s, final StompHeaders h) {
                StompConnection.this.retries.set(0);
                // new session: old subscriptions are gone, so register all topics again
                StompConnection.this.registry.bindSession(s);
                StompConnection.this.modules.forEach(m -> m.register(StompConnection.this.registry));

                log.info("STOMP connected: {}", s.getSessionId());
                log.info("Destinations Subscribed:\n{}", StompConnection.this.registry.destinations());
                setState(ConnectionState.CONNECTED);
            }

            @Override
            public void handleTransportError(final StompSession s, final Throwable ex) {
                connectionLost(ex);
            }

            @Override
            public void handleException(final StompSession session, @Nullable final StompCommand command,
                    final StompHeaders headers, final byte[] payload, final Throwable exception) {
                log.error("STOMP exception: command={}, headers={}, payload={}, error={}", command, headers,
                        new String(payload), exception.getMessage(), exception);
            }
        }).whenComplete((session, ex) -> {
            if (ex != null)
                connectionLost(ex);
        });
    }

    private void connectionLost(final Throwable ex) {
        if (this.stopping || !this.reconnectScheduled.compareAndSet(false, true))
            return;

        final var reason = NestedExceptionUtils.getMostSpecificCause(ex).getMessage();
        final var retry = this.retries.incrementAndGet();
        if (retry > MAX_RETRIES) {
            log.error("STOMP connection to {} failed ({}), giving up after {} retries", this.url, reason,
                    MAX_RETRIES);
            this.retries.set(0);
            // reconnectScheduled stays set, so the second report of this failure is ignored; reconnect() resets it
            setState(ConnectionState.DISCONNECTED);
            return;
        }

        final long delay = FIRST_RETRY_DELAY_MS << (retry - 1); // 2 s, 4 s, 8 s
        log.warn("STOMP connection lost ({}), retry {}/{} in {} s", reason, retry, MAX_RETRIES, delay / 1000);
        setState(ConnectionState.CONNECTING);
        this.scheduler.schedule(() -> {
            this.reconnectScheduled.set(false);
            connect();
        }, Instant.now().plusMillis(delay));
    }

    private void setState(final ConnectionState newState) {
        this.state = newState;
        this.browserUpdateService.notifyBrowsers();
    }
}
