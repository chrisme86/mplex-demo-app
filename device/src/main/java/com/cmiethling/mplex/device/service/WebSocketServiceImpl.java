package com.cmiethling.mplex.device.service;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;

import com.cmiethling.mplex.device.DeviceCommunicationException;
import com.cmiethling.mplex.device.DeviceException;
import com.cmiethling.mplex.device.DeviceModule;
import com.cmiethling.mplex.device.api.DeviceCommand;
import com.cmiethling.mplex.device.websocket.CommandTaskInfo;
import com.cmiethling.mplex.device.websocket.MyWebSocketListener;

/**
 * Implementation of the websocket client interface.
 */
public final class WebSocketServiceImpl implements WebSocketService {

    private final Logger log = DeviceModule.logger();

    // beans
    private final URI uri;
    private final ConcurrentMap<UUID, CommandTaskInfo<? extends DeviceCommand>> commandTasks;
    private final DeviceMessageService deviceMessageService;
    private final MyWebSocketListener myWebSocketListener;

    private WebSocket webSocketClient;
    private ExecutorService executor = Executors.newCachedThreadPool();

    public WebSocketServiceImpl(final URI uri,
            final ConcurrentMap<UUID, CommandTaskInfo<? extends DeviceCommand>> commandTasks,
            final DeviceMessageService deviceMessageService, final MyWebSocketListener myWebSocketListener) {
        this.uri = uri;
        this.commandTasks = commandTasks;
        this.deviceMessageService = deviceMessageService;
        this.myWebSocketListener = myWebSocketListener;
    }

    // ########################## Device methods ##########################
    @Override
    public void openConnection() throws DeviceException {
        // FIXME remove later
        if (this.executor.isShutdown()) // can't happen after closing app :)
            this.executor = Executors.newCachedThreadPool();
        try {
            final var builder = HttpClient.newHttpClient().newWebSocketBuilder();
            this.webSocketClient = builder.buildAsync(this.uri, this.myWebSocketListener).get();
            this.log.info("Device connection established: " + this.webSocketClient);
        } catch (final ExecutionException ex) {// unwrap ExeExc
            throw new DeviceCommunicationException("connectionFailed", (Exception) ex.getCause());
        } catch (final InterruptedException ex) {
            throw new DeviceCommunicationException("connectionFailed", ex);
        }
    }

    @Override
    public Future<Boolean> sendClose() {
        this.executor.shutdownNow();
        if (this.webSocketClient != null) {
            return this.webSocketClient.sendClose(WebSocket.NORMAL_CLOSURE, "closed WebSocket connection")
                    // ### use thenApplyAsync() as main thread could be used with thenApply()!### see:
                    // https://stackoverflow.com/questions/27723546/completablefuture-supplyasync-and-thenapply
                    .thenApplyAsync(ws -> Boolean.TRUE)// if no exception >> ws is closed properly
                    .exceptionally(ex -> {
                        this.log.error("", ex);
                        return Boolean.FALSE;
                    });
        }
        return CompletableFuture.completedFuture(Boolean.FALSE);
    }

    @Override
    public boolean isConnected() {
        return this.webSocketClient != null && !this.webSocketClient.isOutputClosed()
                && !this.webSocketClient.isInputClosed();
    }

    @Override
    public <T extends DeviceCommand> Future<T> sendCommand(final T command) throws DeviceException {

        // before we create a task for sending the message we need to perform same preparations
        // get the message to send and the id for reference
        final var requestMessage = command.toRequestMessage();
        final var messageId = requestMessage.getId();

        // make sure we have a connection
        ensureConnected();

        final Callable<T> processCommandMessage = () -> {
            try {
                // convert the message to JSON
                final var json = this.deviceMessageService.serializeMessage(requestMessage);

                synchronized (this) {
                    this.webSocketClient.sendText(json, true).get();
                }
                this.log.info("sent {}", requestMessage);

                final var taskInfo = this.commandTasks.get(messageId);
                if (taskInfo == null)
                    throw new IllegalStateException("CommandTaskInfo must be put in map BEFORE it is started");
                // wait 3s for the result
                if (!taskInfo.waitForResult(3, TimeUnit.SECONDS))
                    throw new TimeoutException();

                // get the result
                final var resultMessage = taskInfo.getResultMessage();

                // set the result to the command and return it
                command.fromResultMessage(resultMessage);
                this.log.info("received {}", resultMessage);
                return command;
            } finally {
                // make sure we unregister the task
                this.commandTasks.remove(messageId);
            }
        };
        final var commandTaskInfo = new CommandTaskInfo<>(processCommandMessage, this.executor);
        this.commandTasks.put(messageId, commandTaskInfo);
        // commandTaskInfo is in map >> now send the message and wait for the answer (no race condition can occur)
        commandTaskInfo.start();
        // null can never happen here
        return requireNonNull(commandTaskInfo.getTask());
    }

    @Override
    public void cancelAllCommands() {
        this.commandTasks.values().forEach(CommandTaskInfo::cancel);
    }

    @Override
    public synchronized void ensureConnected() throws DeviceException {
        if (this.webSocketClient == null || this.webSocketClient.isOutputClosed()) {
            this.log.info(this.webSocketClient == null ? "Device not connected yet, trying to connect to device..." :
                    "Device connection was " + "closed, trying to reconnect to device...");
            openConnection();
        }
        if (this.executor.isShutdown()) // for testing {@code sendClose()} only, can't happen after closing app :)
            this.executor = Executors.newCachedThreadPool();
    }
}

