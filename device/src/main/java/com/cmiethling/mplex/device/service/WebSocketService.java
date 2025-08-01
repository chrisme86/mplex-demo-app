package com.cmiethling.mplex.device.service;

import java.util.concurrent.Future;

import com.cmiethling.mplex.device.DeviceCommunicationException;
import com.cmiethling.mplex.device.DeviceException;
import com.cmiethling.mplex.device.DeviceMessageException;
import com.cmiethling.mplex.device.api.DeviceCommand;
import com.cmiethling.mplex.device.api.DeviceEvent;
import com.cmiethling.mplex.device.message.EventMessage;
import com.cmiethling.mplex.device.message.RequestMessage;
import com.cmiethling.mplex.device.message.ResultMessage;
import com.cmiethling.mplex.device.websocket.DeviceEventWrapper;

/**
 * Represents an interface between application and hardware.
 * <ul>
 * <li>It sends a {@link RequestMessage} (wrapped as {@link DeviceCommand}) to the (simulated) hardware and it waits for
 * a {@link ResultMessage} (again wrapped as {@link DeviceCommand}).</li>
 * <li>Independent events from the hardware will be sent as {@link EventMessage
 * EventMessages} (wrapped as {@link DeviceEvent DeviceEvents} and {@link DeviceEventWrapper DeviceEventWrapper}).</li>
 * </ul>
 */
public interface WebSocketService {

    /**
     * The application sends a {@link RequestMessage} wrapped as a {@link DeviceCommand} to the hardware (either
     * simulated or the real) and gets back a {@link ResultMessage} as Future. The Future wraps the following
     * Exceptions: If there is no ResultMessage by the timeout the Future will have a TimeoutException wrapped in a
     * ExecutionException.
     *
     * @param <T>     the type of the device command
     * @param command CommandMessage wrapped as a DeviceCommand to be sent to hardware
     *
     * @return The ResultMessage wrapped as a DeviceCommand or a wrapped Exception
     *
     * @throws DeviceException if the command cannot be sent: If there is a problem with the WebSocket connection >>
     *                         {@link DeviceCommunicationException}. If there is a problem evaluating the result message
     *                         >> {@link DeviceMessageException}.
     */
    <T extends DeviceCommand> Future<T> sendCommand(T command) throws DeviceException;

    /**
     * Cancel all pending commands. This will produce {@link InterruptedException}s in all {@link Future}s returned by
     * {@link #sendCommand(DeviceCommand)}.
     */
    void cancelAllCommands();

    /**
     * Opens a connection to the hardware via WebSocket.
     *
     * @throws DeviceException if the connection could not be established.
     */
    void openConnection() throws DeviceException;

    /**
     * This method ensures the connection to the WebSocket. There are 2 possibilities:
     * <ul>
     * <li>there is no WebSocketConnection yet. The method will try to establish a connection</li>
     * <li>the WebSocketSession is closed on hardware-side. The method will try to reconnect if so.</li>
     * </ul>
     * The method blocks.
     *
     * @throws DeviceException exception if connection could not be established (in case of reconnect attempt)
     */
    void ensureConnected() throws DeviceException;

    /**
     * Sends a close command at the WebSocket.
     *
     * @return async true if WebSocket connection has been closed
     */
    Future<Boolean> sendClose();

    boolean isConnected();
}
