package com.cmiethling.mplex.device.api.fluidics;

import org.junit.jupiter.api.Test;

import com.cmiethling.mplex.device.api.AbstractDeviceCommandTest;

public final class SetGelPumpCommandTest extends AbstractDeviceCommandTest {

    @Test
    public void command() throws Exception {
        final var command = this.eventCommandFactory.command(SetGelPumpCommand.class);
        command.setOn(true);

        toRequestMessage(command, "SetGelPump_request");
    }

    @Test
    public void result() throws Exception {
        fromResultMessage(this.eventCommandFactory.command(SetGelPumpCommand.class), "SetGelPump_result");
    }
}

