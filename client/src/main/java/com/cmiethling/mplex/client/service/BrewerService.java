package com.cmiethling.mplex.client.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ProblemDetail;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.cmiethling.mplex.client.model.BrewerStatus;
import com.cmiethling.mplex.client.model.EventEntry;
import com.cmiethling.mplex.client_api.api.BrewerApi;
import com.cmiethling.mplex.client_api.model.CommandAccepted;
import com.cmiethling.mplex.client_api.model.SetBrewValveRequest;
import com.cmiethling.mplex.client_api.model.SetBrewValveRequestAllOfParameters;
import com.cmiethling.mplex.client_api.model.SetWaterPumpRequest;
import com.cmiethling.mplex.client_api.model.SetWaterPumpRequestAllOfParameters;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@Service
public class BrewerService {
    // max. time between the confirmation of a command and its result event
    private static final Duration RESULT_TIMEOUT = Duration.ofSeconds(5);

    @Autowired
    private BrewerApi brewerApi;
    @Autowired
    private BrowserUpdateService browserUpdateService;
    @Autowired
    private ThreadPoolTaskScheduler scheduler;

    // ################# current states ##########################
    @Autowired
    private BrewerStatus brewerStatus;

    // ################# commands ##########################
    public void setWaterPumpCommand(final boolean isOn) {
        final var request = new SetWaterPumpRequest() //
                .parameters(new SetWaterPumpRequestAllOfParameters().isOn(isOn));
        log.info("Sending Request: {}", request);
        sendCommand("SetWaterPump", this.brewerStatus.getWaterPump(),
                () -> this.brewerApi.setWaterPumpCommand(request));
    }

    public void setBrewValveCommand(final boolean isOpen) {
        final var request = new SetBrewValveRequest() //
                .parameters(new SetBrewValveRequestAllOfParameters().isOpen(isOpen));
        log.info("Sending Request: {}", request);
        sendCommand("SetBrewValve", this.brewerStatus.getBrewValve(),
                () -> this.brewerApi.setBrewValveCommand(request));
    }

    // the device only confirms the command (202), the row stays pending until the StatesEvent with this commandId
    private void sendCommand(final String command, final EventEntry entry, final Supplier<CommandAccepted> call) {
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
        entry.setPendingCommandId(commandId);
        entry.setCommandError(null);
        this.browserUpdateService.notifyBrowsers();
        this.scheduler.schedule(() -> checkResult(command, entry, commandId), Instant.now().plus(RESULT_TIMEOUT));
    }

    private void checkResult(final String command, final EventEntry entry, final UUID commandId) {
        if (!commandId.equals(entry.getPendingCommandId()))
            return; // result arrived (or a newer command was sent)
        log.warn("{} {}: no result from device within {} s", command, commandId, RESULT_TIMEOUT.toSeconds());
        entry.setPendingCommandId(null);
        entry.setCommandError("%s: no result from device".formatted(command));
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
