package com.cmiethling.mplex.client.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.http.ProblemDetail;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.cmiethling.mplex.client.model.DeviceState;
import com.cmiethling.mplex.client_api.model.CommandAccepted;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sends a command to the device. The device only confirms it (202), the device state stays pending until the
 * StatesEvent with this commandId arrives.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CommandSender {
    // max. time between the confirmation of a command and its result event
    private static final Duration RESULT_TIMEOUT = Duration.ofSeconds(5);

    private final BrowserUpdateService browserUpdateService;
    private final ThreadPoolTaskScheduler scheduler;

    public void send(final String command, final DeviceState deviceState, final Supplier<CommandAccepted> call) {
        final UUID commandId;
        try {
            commandId = call.get().getCommandId();
        } catch (final RestClientException e) {
            throw toDeviceCommandException(command, e);
        }
        if (commandId == null) { // eg an emulator with an older API version
            log.error("{}: response without commandId, result can't be assigned", command);
            throw new DeviceCommandException("%s failed: response without commandId".formatted(command), null);
        }
        log.info("{} accepted: {}", command, commandId);
        deviceState.setPendingCommandId(commandId);
        deviceState.setCommandError(null);
        this.browserUpdateService.notifyBrowsers();
        this.scheduler.schedule(() -> checkResult(command, deviceState, commandId), Instant.now().plus(RESULT_TIMEOUT));
    }

    private void checkResult(final String command, final DeviceState deviceState, final UUID commandId) {
        if (!commandId.equals(deviceState.getPendingCommandId()))
            return; // result arrived (or a newer command was sent)
        log.warn("{} {}: no result from device within {} s", command, commandId, RESULT_TIMEOUT.toSeconds());
        deviceState.setPendingCommandId(null);
        deviceState.setCommandError("%s: no result from device".formatted(command));
        this.browserUpdateService.notifyBrowsers();
    }

    // device answered with an error (eg 409 as ProblemDetail) or was not reachable
    private DeviceCommandException toDeviceCommandException(final String command, final RestClientException e) {
        if (e instanceof final RestClientResponseException responseException) {
            final var problem = responseException.getResponseBodyAs(ProblemDetail.class);
            final var detail = problem != null && problem.getDetail() != null ? problem.getDetail()
                    : responseException.getStatusText();
            log.warn("{} rejected with {}: {}", command, responseException.getStatusCode(), detail);
            return new DeviceCommandException("%s rejected: %s".formatted(command, detail), e);
        }
        log.error("{} failed: ", command, e);
        return new DeviceCommandException("%s failed: device not reachable".formatted(command), e);
    }
}
