package com.cmiethling.mplex.emulator.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.cmiethling.mplex.device.message.EventMessage;
import com.cmiethling.mplex.device.message.RequestMessage;
import com.cmiethling.mplex.device.message.ResultMessage;
import com.cmiethling.mplex.emulator.model.MessageEntry;

import lombok.Getter;

@Getter
@Service
public class LogService {
    private final List<MessageEntry> logEntries = new ArrayList<>();

    public void logEvent(final EventMessage eventMessage, @Nullable final String error) {
        this.logEntries.add(MessageEntry.ofEvent(eventMessage, error));
    }

    public void logRequestOnly(final RequestMessage request, @NonNull final String error) {
        this.logEntries.add(MessageEntry.ofRequest(request, error));
    }

    public void logFullCommand(final RequestMessage request, final ResultMessage result) {
        this.logEntries.add(MessageEntry.ofCommand(request, result));
    }
}


