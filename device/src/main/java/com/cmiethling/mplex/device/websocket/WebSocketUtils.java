package com.cmiethling.mplex.device.websocket;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import com.cmiethling.mplex.device.DeviceModule;
import com.cmiethling.mplex.device.message.DeviceMessage;
import com.cmiethling.mplex.device.message.EventMessage;
import com.cmiethling.mplex.device.message.RequestMessage;
import com.cmiethling.mplex.device.message.ResultMessage;

@Configuration
public class WebSocketUtils {
    /**
     * Name of the logger for received raw messages.
     */
    private static final String RECEIVE_LOGGER_NAME = "device.receive";
    static final Logger receiveLogger = DeviceModule.logger(RECEIVE_LOGGER_NAME);
    /**
     * Pattern for the name of the logger for specific commands.
     */
    private static final String REQUEST_LOGGER_NAME_PATTERN = "device.request.%s.%s";
    /**
     * Pattern for the name of the logger for specific results.
     */
    private static final String RESULT_LOGGER_NAME_PATTERN = "device.result.%s.%s";
    /**
     * Pattern for the name of the logger for specific events.
     */
    private static final String EVENT_LOGGER_NAME_PATTERN = "device.event.%s.%s";

    public static void logMessage(@NonNull final DeviceMessage message) {
        // get the logger name pattern and the log text pattern for the message type
        final String loggerNamePattern;
        final String logMessagePrefix;
        switch (message) {
        case final RequestMessage requestMessage -> {
            loggerNamePattern = REQUEST_LOGGER_NAME_PATTERN;
            logMessagePrefix = "Sending request";
        }
        case final ResultMessage resultMessage -> {
            loggerNamePattern = RESULT_LOGGER_NAME_PATTERN;
            logMessagePrefix = "Received result";
        }
        case final EventMessage eventMessage -> {
            loggerNamePattern = EVENT_LOGGER_NAME_PATTERN;
            logMessagePrefix = "Received event";
        }
        default -> throw new IllegalArgumentException(message.getClass().getName());
        }

        // get a message-specific logger
        final var logger = DeviceModule.logger(
                String.format(loggerNamePattern, message.getSubsystem().id(), message.getTopic()));

        final Supplier<String> header = () -> "%s: id=%s subsystem=%s topic=%s    ".formatted(logMessagePrefix,
                message.getId(), message.getSubsystem(), message.getTopic());

        if (logger.isTraceEnabled())
            logger.trace("{}{}", header.get(), message.parameters());
        else if (logger.isDebugEnabled())
            logger.debug(header.get());
    }

    public static void logJsonMessage(final Logger logger, final String prefix, final String json) {
        final var jsonPattern = "%s:%s";
        if (logger.isTraceEnabled()) {
            // log full JSON
            logger.trace(jsonPattern.formatted(prefix, json));
        } else if (logger.isDebugEnabled()) {
            // log shortened JSON
            logger.debug(shortenLogString(jsonPattern.formatted(prefix, json)));
        }
    }

    private static String shortenLogString(final String s) {
        final int maxLength = 500;
        if (s != null && s.length() > maxLength)
            return s.substring(0, maxLength) + "...";
        return s;
    }
}
