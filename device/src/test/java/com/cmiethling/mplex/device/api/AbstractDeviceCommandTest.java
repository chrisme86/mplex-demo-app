package com.cmiethling.mplex.device.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.UUID;

import org.springframework.lang.NonNull;

import com.cmiethling.mplex.device.message.ResultMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractDeviceCommandTest extends AbstractDeviceTest {
    /**
     * This id is used in the JSON files for testing.
     */
    private static final UUID TESTING_UUID = UUID.fromString("2e4107c4-8773-4e62-a400-7e7c8195e918");

    protected void toRequestMessage(@NonNull final DeviceCommand command, @NonNull final String resourceName)
            throws Exception {
        final var command1 = (AbstractDeviceCommand<?>) command;
        command1.setIdGenerator(() -> TESTING_UUID);
        final var requestMessage = command1.toRequestMessage();
        final var json = super.deviceMessageService.serializeMessage(requestMessage);

        final var expectedJson = super.loadJson(resourceName);

        // maps to JsonNode preventing problems with string literals (ie line separator LF vs CRLF)
        final var mapper = new ObjectMapper();
        assertEquals(mapper.readTree(expectedJson), mapper.readTree(json), resourceName);
    }

    protected <T extends DeviceCommand> T fromResultMessage(@NonNull final T command,
            @NonNull final String resourceName) throws Exception {
        final var message = loadMessage(resourceName);
        final var result = assertInstanceOf(ResultMessage.class, message);

        command.fromResultMessage(result);
        return command;
    }
}

