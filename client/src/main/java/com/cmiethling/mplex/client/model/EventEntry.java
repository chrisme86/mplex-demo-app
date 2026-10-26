package com.cmiethling.mplex.client.model;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.cmiethling.mplex.device_events.api.Subsystem;

import lombok.Data;

@Data
public class EventEntry {
    @NonNull
    private Subsystem subsystem;
    @NonNull
    private String topic;
    @Nullable
    private String currentState;
    // command sent and confirmed, waiting for its result event
    @Nullable
    private UUID pendingCommandId;
    // eg no result from the device in time
    @Nullable
    private String commandError;

    public EventEntry(@NonNull final Subsystem subsystem, @NonNull final String topic) {
        this.subsystem = subsystem;
        this.topic = topic;
        this.currentState = null;
    }
}
