package com.cmiethling.mplex.device.api.fluidics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.cmiethling.mplex.device.api.AbstractDeviceEventTest;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public final class StatesEventTest extends AbstractDeviceEventTest {

    @Test
    public void event1() throws Exception {
        final var event = fromEventMessage(StatesEvent.class, "States_event1");
        assertTrue(event.isGelPumpOn().get());
        assertTrue(event.isGelValveOpen().isEmpty());
    }

    @Test
    public void event2() throws Exception {
        final var event = fromEventMessage(StatesEvent.class, "States_event2");
        assertTrue(event.isGelPumpOn().get());
        assertFalse(event.isGelValveOpen().get());
    }

    @Test
    public void event3() throws Exception {
        final var event = fromEventMessage(StatesEvent.class, "States_event3");
        assertTrue(event.isGelPumpOn().isEmpty());
        assertFalse(event.isGelValveOpen().get());
    }
}
