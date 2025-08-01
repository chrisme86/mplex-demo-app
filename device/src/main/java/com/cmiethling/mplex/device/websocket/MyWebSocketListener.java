package com.cmiethling.mplex.device.websocket;

import java.net.http.WebSocket;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;

import com.cmiethling.mplex.device.DeviceMessageException;
import com.cmiethling.mplex.device.DeviceModule;
import com.cmiethling.mplex.device.api.DeviceCommand;
import com.cmiethling.mplex.device.api.DeviceEvent;
import com.cmiethling.mplex.device.message.DeviceMessage;
import com.cmiethling.mplex.device.message.EventMessage;
import com.cmiethling.mplex.device.message.ResultMessage;
import com.cmiethling.mplex.device.service.DeviceMessageService;

public class MyWebSocketListener implements WebSocket.Listener {

    private final Logger log = DeviceModule.logger();

    private final DeviceMessageService deviceMessageService;
    private final ConcurrentMap<UUID, CommandTaskInfo<? extends DeviceCommand>> commandTasks;
    private final ApplicationEventPublisher eventPublisher;

    private final StringBuilder textData = new StringBuilder();

    public MyWebSocketListener(final ConcurrentMap<UUID, CommandTaskInfo<? extends DeviceCommand>> commandTasks,
            final DeviceMessageService deviceMessageService, final ApplicationEventPublisher eventPublisher) {
        this.commandTasks = commandTasks;
        this.deviceMessageService = deviceMessageService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * The application receives a {@link ResultMessage} or an {@link EventMessage} from the hardware.
     * <p>
     * Note that {@code onText()} itself is synchronous so it can only be called one at a time, but the returned
     * CompletionStage (=CompletableFuture) is asynchronous so one has to differentiate
     * </p>
     * <p>
     * For instance: given are 2 messages msg with delay d: msg1(d1=50ms), msg2(d2=0)
     * </p>
     * <ul>
     * <li>if delay was in synchronous onText() >> order will stay the same as without delay: {msg1, msg2}</li>
     * <li>if delay was in asynchronous CompletableFuture >> order will (most likely) change: {msg2, msg1}</li>
     * </ul>
     */
    @Override
    public CompletionStage<?> onText(final WebSocket webSocket, final CharSequence data, final boolean last) {
        webSocket.request(1);
        return onText(data, last);
    }

    CompletableFuture<?> onText(final CharSequence data, final boolean last) {
        // StringBuilder must be processed in (synchronous) onText() otherwise there might be raceConditions
        this.textData.append(data);

        if (last) {
            final var json = this.textData.toString();
            this.textData.setLength(0); // reset for next message
            return CompletableFuture.runAsync(() -> {
                WebSocketUtils.logJsonMessage(WebSocketUtils.receiveLogger, "Received from device", json);
                try {
                    computeReceivedMessage(this.deviceMessageService.deserializeMessage(json));
                } catch (final DeviceMessageException ex) {
                    this.log.error("", ex);
                    throw new CompletionException(ex);
                }
            });
        }
        // no idea what this return really does... "an indication [the WebSocket] may reclaim the CharSequence" ???
        return new CompletableFuture<>();// this is safest because null lets the "CharSequence reclaim immediately"???
    }

    void computeReceivedMessage(final DeviceMessage message) throws DeviceMessageException {
        switch (message) {
        case final ResultMessage result -> {
            // log the result with a result-specific logger
            WebSocketUtils.logMessage(result);

            // find a waiting task by the id
            final var messageId = result.getId();
            final var taskInfo = this.commandTasks.get(messageId);
            if (taskInfo != null) {
                // provide the message to the task
                taskInfo.setResultMessage(result);
            } else
                WebSocketUtils.receiveLogger.warn("Unexpected result message, id: {}", messageId);
        }
        case final EventMessage eventMessage -> {
            // log the event with an event-specific logger
            WebSocketUtils.logMessage(eventMessage);

            final var event = DeviceEvent.of(eventMessage.getSubsystem(), eventMessage.getTopic());
            event.fromEventMessage(eventMessage);

            this.eventPublisher.publishEvent(new DeviceEventWrapper<>(this, event));
        }
        default -> throw new DeviceMessageException("invalidMessageType: " + message);
        }
    }

    @Override
    public CompletionStage<?> onClose(final WebSocket webSocket, final int statusCode, final String reason) {
        this.log.info("WebSocketClient onClose={}   >> status={}, reason: {}", webSocket, statusCode, reason);
        this.commandTasks.clear();
        return null;
    }

    @Override
    public void onError(final WebSocket webSocket, final Throwable error) {
        this.log.error("Device connection error: ", error);
    }
}
