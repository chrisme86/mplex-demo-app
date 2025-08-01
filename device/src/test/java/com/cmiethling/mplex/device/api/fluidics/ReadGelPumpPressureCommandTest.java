package com.cmiethling.mplex.device.api.fluidics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.cmiethling.mplex.device.api.AbstractDeviceCommandTest;

public final class ReadGelPumpPressureCommandTest extends AbstractDeviceCommandTest {

    @Test
    public void command() throws Exception {
        final var command = this.eventCommandFactory.command(ReadGelPumpPressureCommand.class);
        toRequestMessage(command, "ReadGelPumpPressure_request");
    }

    @Test
    public void result() throws Exception {
        final var command = fromResultMessage(this.eventCommandFactory.command(ReadGelPumpPressureCommand.class),
                "ReadGelPumpPressure_result");

        assertEquals(42.2, command.getPressure());
    }
}

