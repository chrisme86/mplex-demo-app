package com.cmiethling.mplex.device.api.fluidics;

import org.junit.jupiter.api.Test;

import com.cmiethling.mplex.device.api.AbstractDeviceCommandTest;

public final class SetGelValveCommandTest extends AbstractDeviceCommandTest {

    @Test
    public void command() throws Exception {
        final var command = this.eventCommandFactory.command(SetGelValveCommand.class);
        command.setOn(true);

        toRequestMessage(command, "SetGelValve_request");
    }

    @Test
    public void result() throws Exception {
        fromResultMessage(this.eventCommandFactory.command(SetGelValveCommand.class), "SetGelValve_result");
    }
}

