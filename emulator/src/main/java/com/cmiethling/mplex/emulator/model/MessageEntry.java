package com.cmiethling.mplex.emulator.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.cmiethling.mplex.device.message.EventMessage;
import com.cmiethling.mplex.device.message.RequestMessage;
import com.cmiethling.mplex.device.message.ResultMessage;
import com.cmiethling.mplex.device.message.Subsystem;

import lombok.Getter;

public class MessageEntry {

    @Getter
    private final LocalDateTime timestamp;
    @Getter
    private final MessageEntryType type;
    @Getter
    private final Subsystem subsystem;
    @Getter
    private final String topic;
    private final String in;
    private final String out;
    @Getter
    private final String thymeleafTimestamp;
    @Getter
    private final boolean sent;

    public MessageEntry(final LocalDateTime timestamp, final MessageEntryType type, final Subsystem subsystem,
            final String topic, final String in, final String out, final boolean sent) {
        this.timestamp = timestamp;
        this.type = type;
        this.subsystem = subsystem;
        this.topic = topic;
        this.in = in;
        this.out = out;
        this.thymeleafTimestamp = timestamp.format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss"));
        this.sent = sent;
    }

    public static MessageEntry ofEvent(final EventMessage event, @Nullable final String error) {
        if (error == null)
            return new MessageEntry(LocalDateTime.now(), MessageEntryType.EVENT, event.getSubsystem(), event.getTopic(),
                    null, event.toString(), true);
        return new MessageEntry(LocalDateTime.now(), MessageEntryType.EVENT, event.getSubsystem(), event.getTopic(),
                null, error, false);
    }

    public static MessageEntry ofRequest(final RequestMessage request, @NonNull final String error) {
        return new MessageEntry(LocalDateTime.now(), MessageEntryType.REQUEST, request.getSubsystem(),
                request.getTopic(), error, null, false);
    }

    public static MessageEntry ofCommand(final RequestMessage request, final ResultMessage result) {
        return new MessageEntry(LocalDateTime.now(), MessageEntryType.COMMAND, result.getSubsystem(), result.getTopic(),
                request.toString(), result.toString(), true);
    }

    /**
     * Returns incoming request in JSON format.
     *
     * @return incoming request in JSON format
     */
    public String getRequest() {
        return switch (this.type) {
            case REQUEST, COMMAND -> this.in;
            default -> null;
        };
    }

    /**
     * Returns outgoing result in JSON format.
     *
     * @return outgoing result in JSON format
     */
    public String getResult() {
        return switch (this.type) {
            case RESULT, COMMAND -> this.out;
            default -> null;
        };
    }

    /**
     * Returns outgoing event in JSON format.
     *
     * @return outgoing event in JSON format
     */
    public String getEvent() {
        if (this.type == MessageEntryType.EVENT)
            return this.out;
        return null;
    }
}
