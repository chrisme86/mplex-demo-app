package com.cmiethling.mplex.client.stomp;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionRegistry {
    private final Map<String, StompSession.Subscription> subs = new ConcurrentHashMap<>();
    private volatile StompSession session;

    public void bindSession(final StompSession session) {
        this.session = session;
    }

    public <T> void subscribe(final String destination, final Class<T> type, final Consumer<T> handler) {
        // idempotent: avoid duplicate subscriptions for same destination
        this.subs.computeIfAbsent(destination, d -> this.session.subscribe(d, new StompFrameHandler() {
            @Override
            public Type getPayloadType(final StompHeaders headers) {
                return type;
            }

            @Override
            public void handleFrame(final StompHeaders headers, final Object payload) {
                @SuppressWarnings("unchecked")
                final T body = (T) payload;
                handler.accept(body);
            }
        }));
    }

    public Set<String> destinations() {
        return Set.copyOf(this.subs.keySet());
    }
}
