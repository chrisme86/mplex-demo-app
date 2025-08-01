package com.cmiethling.mplex.client.core;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cmiethling.mplex.device.DeviceException;
import com.cmiethling.mplex.device.api.DeviceCommand;
import com.cmiethling.mplex.device.message.RequestMessage;
import com.cmiethling.mplex.device.message.ResultMessage;
import com.cmiethling.mplex.device.service.EventCommandFactory;
import com.cmiethling.mplex.device.service.WebSocketService;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DeviceCorePart {

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private EventCommandFactory eventCommandFactory;

    // @PostConstruct
    // public void init() throws DeviceException {
    //     openConnection();
    // }

    @PreDestroy
    public void cleanup() throws ExecutionException, InterruptedException, TimeoutException {
        closeConnection();
    }

    /**
     * The application sends a {@link RequestMessage} wrapped as a {@link DeviceCommand} to the hardware (either
     * simulated or the real) and gets back a {@link ResultMessage}.
     *
     * @param command the command
     *
     * @return the command with the {@link ResultMessage}
     *
     * @throws DeviceException      if the command could not be sent
     * @throws ExecutionException   if the {@link ResultMessage} returns with an error or if the command took too long
     *                              (TimeoutException)
     * @throws InterruptedException if the execution was interrupted from outside
     */
    public <T extends DeviceCommand> T sendCommand(final T command)
            throws DeviceException, ExecutionException, InterruptedException {
        return this.webSocketService.sendCommand(command).get();
    }

    public <T extends DeviceCommand> T command(final Class<T> command) {
        return this.eventCommandFactory.command(command);
    }

    public void openConnection() throws DeviceException {
        this.webSocketService.openConnection();
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean closeConnection() throws ExecutionException, InterruptedException, TimeoutException {
        return this.webSocketService.sendClose().get(1, TimeUnit.SECONDS);
    }

    public boolean isConnected() {
        return this.webSocketService.isConnected();
    }
}
