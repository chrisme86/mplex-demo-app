package com.cmiethling.mplex.client.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.cmiethling.mplex.client.model.DeviceState;
import com.cmiethling.mplex.client_api.api.BrewerApi;
import com.cmiethling.mplex.client_api.invoker.ApiClient;
import com.cmiethling.mplex.client_api.model.SetWaterPumpRequest;
import com.cmiethling.mplex.client_api.model.SetWaterPumpRequestAllOfParameters;
import com.cmiethling.mplex.device_events.api.Subsystem;

/**
 * Sends commands through the generated OpenAPI client against a mocked device.
 */
@ExtendWith(MockitoExtension.class)
class CommandSenderTest {

    private static final String URL = "http://device/restapi/brewer/SetWaterPump";
    private final DeviceState waterPump = new DeviceState(Subsystem.BREWER, "Water Pump");
    @Mock
    private BrowserUpdateService browserUpdateService;
    @Mock
    private ThreadPoolTaskScheduler scheduler;
    private MockRestServiceServer device;
    private BrewerApi brewerApi;
    private CommandSender commandSender;

    @BeforeEach
    void setUp() {
        final var restTemplate = new RestTemplate();
        this.device = MockRestServiceServer.bindTo(restTemplate).build();
        final var apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath("http://device");
        this.brewerApi = new BrewerApi(apiClient);
        this.commandSender = new CommandSender(this.browserUpdateService, this.scheduler);
    }

    @Test
    void acceptedCommandIsPendingUntilResult() {
        final var commandId = UUID.randomUUID();
        // device expects pumpOn command and answers 202 with this ID
        this.device.expect(requestTo(URL)) //
                .andExpect(jsonPath("$.parameters.isOn").value(true)) //
                .andRespond(withStatus(HttpStatus.ACCEPTED).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"commandId\": \"%s\"}".formatted(commandId)));

        // run command
        sendPumpOn();

        // waterPump waits for the result of exactly this command
        assertThat(this.waterPump.getPendingCommandId()).isEqualTo(commandId);
        // result event arrived via STOMP (BrewerStatus.completeCommand does this in the app)
        this.waterPump.setPendingCommandId(null);
        // 5 s are over
        runTimeoutCheck();
        // result was in time, so no error
        assertThat(this.waterPump.getCommandError()).isNull();
    }

    @Test
    void missingResultEndsAsTimeoutError() {
        this.device.expect(requestTo(URL)) //
                .andRespond(withStatus(HttpStatus.ACCEPTED).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"commandId\": \"%s\"}".formatted(UUID.randomUUID())));

        sendPumpOn();
        // 5 s are over without result event
        runTimeoutCheck();

        // waterPump no longer waits, shows the error instead
        assertThat(this.waterPump.getCommandError()).isEqualTo("SetWaterPump: no result from device");
        assertThat(this.waterPump.getPendingCommandId()).isNull();
    }

    @Test
    void rejectedCommandShowsProblemDetail() {
        this.device.expect(requestTo(URL)) //
                .andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_PROBLEM_JSON)
                        .body("{\"status\": 409, \"title\": \"Conflict\", \"detail\": \"Water heater is off\"}"));

        assertThatThrownBy(this::sendPumpOn) //
                .isInstanceOf(DeviceCommandException.class) //
                .hasMessage("SetWaterPump rejected: Water heater is off");
        assertThat(this.waterPump.getPendingCommandId()).isNull();
    }

    @Test
    void unreachableDeviceShowsError() {
        this.device.expect(requestTo(URL)).andRespond(withException(new IOException("Connection refused")));

        assertThatThrownBy(this::sendPumpOn) //
                .isInstanceOf(DeviceCommandException.class) //
                .hasMessage("SetWaterPump failed: device not reachable");
    }

    private void sendPumpOn() {
        final var request = new SetWaterPumpRequest().parameters(new SetWaterPumpRequestAllOfParameters().isOn(true));
        this.commandSender.send("SetWaterPump", this.waterPump, () -> this.brewerApi.setWaterPumpCommand(request));
    }

    // runs the check that the scheduler would start after 5 s
    private void runTimeoutCheck() {
        final var check = ArgumentCaptor.forClass(Runnable.class);
        // fetch the check that send() handed to the mocked scheduler
        verify(this.scheduler).schedule(check.capture(), any(Instant.class));
        // run it now instead of waiting
        check.getValue().run();
    }
}
