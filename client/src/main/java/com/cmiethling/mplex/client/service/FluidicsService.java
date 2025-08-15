package com.cmiethling.mplex.client.service;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.cmiethling.mplex.client.AbstractSubsystem;
import com.cmiethling.mplex.client.model.FluidicsStatus;
import com.cmiethling.mplex.client_api.api.FluidicsApi;
import com.cmiethling.mplex.client_api.model.SetGelPumpRequest;
import com.cmiethling.mplex.client_api.model.SetGelPumpRequestAllOfParameters;
import com.cmiethling.mplex.device.DeviceException;
import com.cmiethling.mplex.device.api.fluidics.ErrorEvent;
import com.cmiethling.mplex.device.api.fluidics.SetGelPumpCommand;
import com.cmiethling.mplex.device.api.fluidics.StatesEvent;
import com.cmiethling.mplex.device.message.Subsystem;
import com.cmiethling.mplex.device.websocket.DeviceEventWrapper;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@Service
public class FluidicsService extends AbstractSubsystem {

    @Autowired
    private FluidicsApi fluidicsApi;

    // ################# current states ##########################
    @Autowired
    private FluidicsStatus fluidicsStatus;

    protected FluidicsService() {
        super(Subsystem.FLUIDICS);
    }

    // ################# events ##########################
    @EventListener
    private void errorEventReceived(final DeviceEventWrapper<ErrorEvent> eventWrapper) {
        final var event = eventWrapper.getEvent();
        log.info("received {} with {}", event, event.getErrorCode());
        this.fluidicsStatus.setErrorsEvent(event);
    }

    @EventListener
    private void statesEventReceived(final DeviceEventWrapper<StatesEvent> eventWrapper) {
        final var event = eventWrapper.getEvent();
        log.info("received: {} with isGelPumpOn={}, isGelValveOpen={}", event, event.isGelPumpOn(),
                event.isGelValveOpen());
        event.isGelPumpOn().ifPresent(isOn -> this.fluidicsStatus.setGelPump(isOn));
        event.isGelValveOpen().ifPresent(isOpen -> this.fluidicsStatus.setGelValve(isOpen));
    }

    // ################# commands ##########################
    public void setGelPumpCommand(final boolean isOn) {
        final var request = new SetGelPumpRequest() //
                .subsystem(SetGelPumpRequest.SubsystemEnum.FLUIDICS) //
                .topic(SetGelPumpRequest.TopicEnum.SET_GEL_PUMP) //
                .parameters(new SetGelPumpRequestAllOfParameters().isOn(isOn));
        log.info("Sending Request: {}", request);
        try {
            final var response = this.fluidicsApi.setGelPumpCommand(request);
            log.info("Received Response: {}", response);
            this.fluidicsStatus.setGelPump(isOn);
        } catch (final RestClientException e) {
            log.error("error while receiving response: ", e);
        }
    }

    public void sendGelPumpMode(final boolean isOn) throws ExecutionException, InterruptedException, DeviceException {
        final var command = command(SetGelPumpCommand.class);
        command.setOn(isOn);
        sendCommand(command);
        // TODO remove once StatesEvent is established
        // if there is no exception then command is successful
        this.fluidicsStatus.setGelPump(isOn);
    }
}
