package com.cmiethling.mplex.emulator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.cmiethling.mplex.device_events.api.DeviceEvent;
import com.cmiethling.mplex.device_events.api.brewer.BrewerError;
import com.cmiethling.mplex.device_events.api.heater.ErrorEvent;
import com.cmiethling.mplex.device_events.api.heater.HeaterError;
import com.cmiethling.mplex.device_events.api.heater.StatesEvent;
import com.cmiethling.mplex.emulator.model.BrewerStatus;
import com.cmiethling.mplex.emulator.model.HeaterStatus;

/**
 * Device rules across the subsystems HEATER and BREWER, without web server and WebSocket.
 */
@SpringJUnitConfig({ BrewerService.class, HeaterService.class, BrewerStatus.class, HeaterStatus.class })
class DeviceRulesTest {

    // the device executes a command after 1 s
    private static final int RESULT_WAIT_MS = 3000;

    @Autowired
    private BrewerService brewerService;
    @Autowired
    private HeaterService heaterService;
    @Autowired
    private BrewerStatus brewerStatus;
    @Autowired
    private HeaterStatus heaterStatus;

    @MockitoBean
    private WebSocketService webSocketService;
    @MockitoBean
    private BrowserUpdateService browserUpdateService;

    @BeforeEach
    void resetDevice() {
        this.heaterStatus.setHeaterError(HeaterError.NONE);
        this.heaterStatus.setWaterHeaterOn(false);
        this.brewerStatus.setBrewerError(BrewerError.NONE);
        this.brewerStatus.setWaterPumpOn(false);
    }

    @Test
    void pumpCantBeSwitchedOnWhileHeaterIsOff() {
        assertThatThrownBy(() -> this.brewerService.setWaterPump(true)) //
                .isInstanceOf(DeviceStateConflictException.class) //
                .hasMessageContaining("Water heater is off");
    }

    @Test
    void pumpCantBeSwitchedOnWhileJammed() {
        this.heaterStatus.setWaterHeaterOn(true);
        this.brewerStatus.setBrewerError(BrewerError.PUMP_JAMMED);

        assertThatThrownBy(() -> this.brewerService.setWaterPump(true)) //
                .isInstanceOf(DeviceStateConflictException.class) //
                .hasMessageContaining("Water pump is jammed");
    }

    @Test
    void heaterCantBeSwitchedOffWhilePumpIsRunning() {
        this.heaterStatus.setWaterHeaterOn(true);
        this.brewerStatus.setWaterPumpOn(true);

        assertThatThrownBy(() -> this.heaterService.setWaterHeater(false)) //
                .isInstanceOf(DeviceStateConflictException.class) //
                .hasMessageContaining("Water pump is running");
    }

    @Test
    void heaterCantBeSwitchedOnWhileOverheated() {
        this.heaterStatus.setHeaterError(HeaterError.OVERHEATED);

        assertThatThrownBy(() -> this.heaterService.setWaterHeater(true)) //
                .isInstanceOf(DeviceStateConflictException.class) //
                .hasMessageContaining("Water heater is overheated");
    }

    @Test
    void acceptedCommandSendsResultWithItsCommandId() throws InterruptedException {
        this.heaterStatus.setWaterHeaterOn(true);

        final var commandId = this.brewerService.setWaterPump(true);

        final var event = (com.cmiethling.mplex.device_events.api.brewer.StatesEvent) awaitEvents(1).getFirst();
        assertThat(event.getCommandId()).isEqualTo(commandId);
        assertThat(event.getWaterPumpOn()).isTrue();

        assertThat(this.brewerStatus.isWaterPumpOn()).isTrue();
    }

    @Test
    void overheatedSwitchesOffHeaterAndThenPump() {
        this.heaterStatus.setWaterHeaterOn(true);
        this.brewerStatus.setWaterPumpOn(true);

        this.heaterService.processError(HeaterError.OVERHEATED.name());

        final var events = awaitEvents(3);
        assertThat(events.get(0)).isInstanceOf(ErrorEvent.class);
        final var heaterStates = (StatesEvent) events.get(1);
        assertThat(heaterStates.getWaterHeaterOn()).isFalse();
        assertThat(heaterStates.getCommandId()).isNull(); // no command caused it
        final var brewerStates = (com.cmiethling.mplex.device_events.api.brewer.StatesEvent) events.get(2);
        assertThat(brewerStates.getWaterPumpOn()).isFalse();

        assertThat(this.heaterStatus.isWaterHeaterOn()).isFalse();
        assertThat(this.brewerStatus.isWaterPumpOn()).isFalse();
    }

    @Test
    void resultIsTheRealStateWhenErrorOccursDuringCommand() {
        final var commandId = this.heaterService.setWaterHeater(true);
        this.heaterService.processError(HeaterError.OVERHEATED.name()); // before the device executed the command

        final var result = (StatesEvent) awaitEvents(2).get(1);
        assertThat(result.getCommandId()).isEqualTo(commandId);
        assertThat(result.getWaterHeaterOn()).isFalse();
    }

    // all events sent so far, in order
    private List<DeviceEvent> awaitEvents(final int count) {
        final var captor = ArgumentCaptor.forClass(DeviceEvent.class);
        verify(this.webSocketService, timeout(RESULT_WAIT_MS).times(count)).sendEvent(captor.capture());
        return captor.getAllValues();
    }
}
