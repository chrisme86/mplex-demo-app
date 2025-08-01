package com.cmiethling.mplex.client.model;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.cmiethling.mplex.device.message.Subsystem;

import lombok.Data;

@Data
public class EventEntry {
    @NonNull
    private Subsystem subsystem;
    @NonNull
    private String topic;
    @Nullable
    private String currentState;

    public EventEntry(@NonNull final Subsystem subsystem, @NonNull final String topic) {
        this.subsystem = subsystem;
        this.topic = topic;
        this.currentState = null;
    }
}
