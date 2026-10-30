package com.cmiethling.mplex.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.cmiethling.mplex.client.model.BrewerStatus;
import com.cmiethling.mplex.client.model.DeviceState;
import com.cmiethling.mplex.client.model.HeaterStatus;
import com.cmiethling.mplex.client.service.BrewerService;
import com.cmiethling.mplex.client.service.DeviceCommandException;
import com.cmiethling.mplex.client.service.HeaterService;
import com.cmiethling.mplex.client.stomp.ConnectionState;
import com.cmiethling.mplex.client.stomp.StompConnection;
import com.cmiethling.mplex.device_events.api.brewer.BrewerError;
import com.cmiethling.mplex.device_events.api.heater.HeaterError;
import com.cmiethling.mplex.emulator.EmulatorApp;

/**
 * Client and emulator talk to each other for real: commands via REST, results via STOMP.
 */
@SpringBootTest
class ClientEmulatorIT {

    // the device executes a command after 1 s
    private static final Duration RESULT_WAIT = Duration.ofSeconds(3);

    private static ConfigurableApplicationContext emulator;

    @Autowired
    private HeaterService heaterService;
    @Autowired
    private BrewerService brewerService;
    @Autowired
    private HeaterStatus heaterStatus;
    @Autowired
    private BrewerStatus brewerStatus;
    @Autowired
    private StompConnection stompConnection;

    // emulator must run before the client connects to it
    @BeforeAll
    static void startEmulator() {
        emulator = new SpringApplicationBuilder(EmulatorApp.class).run("--server.port=0",
                // shared classpath: the client's application.yaml hides the emulator's one
                "--spring.mvc.problemdetails.enabled=true",
                // client's security is on the shared classpath, switch it off for the emulator only.
                "--spring.autoconfigure.exclude="
                        + "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration");
    }

    @AfterAll
    static void stopEmulator() {
        emulator.close();
    }

    @DynamicPropertySource
    static void emulatorUrls(final DynamicPropertyRegistry registry) {
        final var port = emulator.getEnvironment().getProperty("local.server.port");
        registry.add("restapiUrl", () -> "http://localhost:" + port);
        registry.add("stomp.url", () -> "ws://localhost:" + port + "/stomp");
    }

    private static void awaitState(final DeviceState deviceState, final String expected) {
        await().atMost(RESULT_WAIT).until(() -> expected.equals(deviceState.getCurrentState()));
    }

    private static void clear(final DeviceState deviceState) {
        deviceState.setCurrentState(null);
        deviceState.setPendingCommandId(null);
        deviceState.setCommandError(null);
    }

    @BeforeEach
    void resetDevice() {
        final var heater = emulator.getBean(com.cmiethling.mplex.emulator.model.HeaterStatus.class);
        heater.setHeaterError(HeaterError.NONE);
        heater.setWaterHeaterOn(false);
        final var brewer = emulator.getBean(com.cmiethling.mplex.emulator.model.BrewerStatus.class);
        brewer.setBrewerError(BrewerError.NONE);
        brewer.setWaterPumpOn(false);
        // client forgets what it saw, so every test waits for new events
        Stream.of(this.heaterStatus.getWaterHeater(), this.heaterStatus.getErrors(), this.brewerStatus.getWaterPump(),
                this.brewerStatus.getErrors()).forEach(ClientEmulatorIT::clear);

        await().atMost(RESULT_WAIT).until(() -> this.stompConnection.getState() == ConnectionState.CONNECTED);
    }

    @Test
    void commandResultArrivesViaStomp() {
        final var waterHeater = this.heaterStatus.getWaterHeater();

        this.heaterService.setWaterHeaterCommand(true);

        // confirmed via REST, result still open
        assertThat(waterHeater.getPendingCommandId()).isNotNull();
        // result event with the same commandId arrived via STOMP
        awaitState(waterHeater, "ON");
        assertThat(waterHeater.getPendingCommandId()).isNull();
    }

    @Test
    void rejectedCommandShowsReasonFromDevice() {
        // heater is off, so the emulator answers 409
        assertThatThrownBy(() -> this.brewerService.setWaterPumpCommand(true)) //
                .isInstanceOf(DeviceCommandException.class) //
                .hasMessage("SetWaterPump rejected: Water heater is off");
    }

    @Test
    void overheatedSwitchesOffHeaterAndPump() {
        this.heaterService.setWaterHeaterCommand(true);
        awaitState(this.heaterStatus.getWaterHeater(), "ON");
        this.brewerService.setWaterPumpCommand(true);
        awaitState(this.brewerStatus.getWaterPump(), "ON");

        // error as triggered on the emulator page
        emulator.getBean(com.cmiethling.mplex.emulator.service.HeaterService.class)
                .processError(HeaterError.OVERHEATED.name());

        awaitState(this.heaterStatus.getErrors(), "OVERHEATED");
        awaitState(this.heaterStatus.getWaterHeater(), "OFF");
        awaitState(this.brewerStatus.getWaterPump(), "OFF");
    }
}
