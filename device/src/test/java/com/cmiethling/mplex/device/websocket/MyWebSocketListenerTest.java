package com.cmiethling.mplex.device.websocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.cmiethling.mplex.device.DeviceMessageException;
import com.cmiethling.mplex.device.TestConfig;
import com.cmiethling.mplex.device.api.DeviceCommand;
import com.cmiethling.mplex.device.api.DeviceEvent;
import com.cmiethling.mplex.device.api.test.ExampleEvent;
import com.cmiethling.mplex.device.message.EventMessage;
import com.cmiethling.mplex.device.message.RequestMessage;
import com.cmiethling.mplex.device.message.ResultMessage;
import com.cmiethling.mplex.device.message.Subsystem;
import com.cmiethling.mplex.device.service.DeviceMessageService;
import com.cmiethling.mplex.device.service.WebSocketServiceImpl;

@TestPropertySource(locations = "classpath:application.properties")
// include MyWebSocketListenerTest as it shall listen to the device events with @EventListener
@SpringJUnitConfig(classes = { TestConfig.class, MyWebSocketListenerTest.class })
public class MyWebSocketListenerTest {

    private static final String TEST_EVENT_TOPIC = "ExampleEvent";
    private static final String NAME_PARAMETER = "name";

    private static final List<DeviceEvent> events = new CopyOnWriteArrayList<>();
    @Autowired
    private DeviceMessageService deviceMessageService;
    @Autowired
    private MyWebSocketListener myWebSocketListener;
    @Autowired
    private WebSocketServiceImpl webSocketService;
    @Autowired
    private ConcurrentMap<UUID, CommandTaskInfo<? extends DeviceCommand>> commandTasks;

    @AfterEach
    public void tearDown() {
        events.clear();
    }

    // synchronous >> no concurrency handling needed
    @EventListener
    void eventReceived(final DeviceEventWrapper<DeviceEvent> eventWrapper) {
        events.add(eventWrapper.getEvent());
    }

    @Test
    public void computeReceivedMessageAsEventTest() throws Exception {

        final var event = new EventMessage(Subsystem.TEST, ExampleEvent.TOPIC);
        event.parameters().putString(NAME_PARAMETER, "some name");

        this.myWebSocketListener.computeReceivedMessage(event); // will block until event is finished

        assertEquals(1, events.size());
        assertEquals(event, events.getFirst().toEventMessage());
    }

    @Test
    public void computeReceivedMessageAsResultTest() throws Exception {
        final var uuid = UUID.randomUUID();

        // create a dummy task info
        final var taskInfo = new CommandTaskInfo<>(() -> null, Executors.newSingleThreadExecutor());
        this.commandTasks.put(uuid, taskInfo);

        // create a result message
        final var result = new ResultMessage(uuid, Subsystem.TEST, "topic");
        result.parameters().putString("name", "parameter name");

        // process a copy of this message
        this.myWebSocketListener.computeReceivedMessage(result);

        // check if the message was set correctly
        assertEquals(result, taskInfo.getResultMessage());

        this.commandTasks.remove(uuid);
    }

    @Test
    public void computeReceivedMessageAsCommandTest() {

        final var request = new RequestMessage(UUID.fromString("2e4107c4-8773-4e62-a400-7e7c8195e918"), Subsystem.TEST,
                "topic");
        request.parameters().putString("name", "parameter name");

        final var message = assertThrows(DeviceMessageException.class,
                () -> this.myWebSocketListener.computeReceivedMessage(request)).getMessage();
        assertTrue(message.contains("invalidMessageType: RequestMessage"));
    }

    @Test
    public void partialMessageTest() throws Exception {

        final var event = new EventMessage(Subsystem.TEST, TEST_EVENT_TOPIC);
        event.parameters().putString(NAME_PARAMETER, "parameter name");

        final var jsonEvent = this.deviceMessageService.serializeMessage(event);
        final var jsonEvent1 = jsonEvent.substring(0, 10);
        final var jsonEvent2 = jsonEvent.substring(10);

        this.myWebSocketListener.onText(jsonEvent1, false); // no join() as new Incomplete future is returned
        this.myWebSocketListener.onText(jsonEvent2, true).join(); // wait for computation >> join()

        assertEquals(1, events.size());
        assertEquals(event, events.getFirst().toEventMessage());
    }

    @Test
    public void wrongPartialMessageTest() throws Exception {
        final var event = new EventMessage(Subsystem.TEST, TEST_EVENT_TOPIC);
        event.parameters().putString(NAME_PARAMETER, "parameter name");
        final var jsonEvent = this.deviceMessageService.serializeMessage(event);

        this.myWebSocketListener.onText("make event wrong", false); // no join() as new Incomplete future is returned
        final var deviceMessageExc = assertThrows(CompletionException.class,
                () -> this.myWebSocketListener.onText(jsonEvent, true).join()).getCause();

        assertSame(DeviceMessageException.class, deviceMessageExc.getClass());
        assertEquals("Error reading JSON to DeviceMessage", deviceMessageExc.getMessage());
    }
}

