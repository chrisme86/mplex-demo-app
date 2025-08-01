package com.cmiethling.mplex.emulator.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.cmiethling.mplex.device.DeviceException;
import com.cmiethling.mplex.device.DeviceMessageException;
import com.cmiethling.mplex.device.message.DeviceMessage;
import com.cmiethling.mplex.device.message.EventMessage;
import com.cmiethling.mplex.device.message.RequestMessage;
import com.cmiethling.mplex.device.message.ResultError;
import com.cmiethling.mplex.device.message.ResultMessage;
import com.cmiethling.mplex.device.service.DeviceMessageService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebSocketServerService extends TextWebSocketHandler {

    private WebSocketSession session1;

    @Autowired
    private DeviceMessageService deviceMessageService;

    @Autowired
    private LogService logService;

    /**
     * Sends an event to the client.
     *
     * @param eventMessage the message to send
     *
     * @return {@code null} if the event was sent successfully, otherwise the error message
     */
    public String broadcastEvent(@NonNull final EventMessage eventMessage) {
        try {
            sendMessage(eventMessage);
            return null;
        } catch (final IOException | DeviceMessageException | NullPointerException ex) {
            log.error("Could not send event: {}", ex.toString());
            return ex.toString();
        }
    }

    private void sendMessage(@NonNull final DeviceMessage eventMessage)
            throws DeviceMessageException, IOException, NullPointerException {
        final var json = this.deviceMessageService.serializeMessage(eventMessage);
        this.session1.sendMessage(new TextMessage(json));
        log.info("Message sent: {}", json);
    }

    /**
     * Handles the incoming text message. This can only be a serialized {@link RequestMessage}.
     *
     * @param session the current session
     * @param message the incoming serialized {@link RequestMessage}
     *
     * @throws DeviceException if there was a problem deserializing the textmessage.
     */
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public void handleTextMessage(@NonNull final WebSocketSession session, @NonNull final TextMessage message)
            throws DeviceException {

        final String json = message.getPayload();
        log.info("received: {}", json);

        final var deviceMessage = this.deviceMessageService.deserializeMessage(json);
        switch (deviceMessage) {
        case final RequestMessage request -> {
            final var result = new ResultMessage(request.getId(), request.getSubsystem(), request.getTopic());
            // TODO: differentiate between different commands
            result.setError(ResultError.NONE);
            try {
                sendMessage(result);
                this.logService.logFullCommand(request, result);
            } catch (final IOException | DeviceMessageException ex) {
                this.logService.logRequestOnly(request, ex.toString());
            }
        }
        default -> throw new IllegalStateException("Unexpected value: " + deviceMessage);
        }
    }

    @Override
    public void afterConnectionEstablished(@NonNull final WebSocketSession session) throws Exception {
        if (this.session1 != null)
            throw new IllegalStateException("session was already established");
        this.session1 = session;
        log.info("Emulator Session opened: {}", session.getId());
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(@NonNull final WebSocketSession session, @NonNull final CloseStatus status)
            throws Exception {
        this.session1 = null;
        log.info(String.format("Emulator Session closed: %s with %s", session.getId(), status));
        super.afterConnectionClosed(session, status);
    }
}
