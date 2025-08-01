package com.cmiethling.mplex.device.api.hv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.cmiethling.mplex.device.api.AbstractDeviceCommandTest;

public class ReadCapillaryDataCommandTest extends AbstractDeviceCommandTest {
    @Test
    public void command() throws Exception {
        final var command = this.eventCommandFactory.command(ReadCapillaryDataCommand.class);
        command.setIndex(5);

        toRequestMessage(command, "ReadCapillaryData_request");
    }

    @Test
    public void result() throws Exception {
        final var command = fromResultMessage(this.eventCommandFactory.command(ReadCapillaryDataCommand.class),
                "ReadCapillaryData_result");

        assertEquals(15, command.getBadRuns());
        assertEquals(CapillaryState.BAD, command.getCapState());
    }
}
