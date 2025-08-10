package com.cmiethling.mplex.device.message;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.cmiethling.mplex.device.DeviceMessageException;
import com.cmiethling.mplex.device.config.DeviceMessageConfig;
import com.cmiethling.mplex.device.service.DeviceMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringJUnitConfig(classes = { DeviceMessageService.class, DeviceMessageConfig.class })
public final class RequestMessageTest {
    private static final UUID ANY_UUID = UUID.fromString("2e4107c4-8773-4e62-a400-7e7c8195e918");

    @Autowired
    private DeviceMessageService deviceMessageService;

    public static Stream<Arguments> invalidJson() {
        return Stream.of(Arguments.of("hello"), //
                Arguments.of("{}"), //
                Arguments.of("""
                        {
                            "type": "command"
                        }
                        """), //
                Arguments.of("""
                        {
                            "type": "command",
                            "uuid": "2e4107c4-8773-4e62-a400-7e7c8195e918"
                        }
                        """), //
                Arguments.of("""
                        {
                            "type": " "
                        }
                        """), //
                Arguments.of("""
                        {
                            "type": "hello"
                        }
                        """),//
                Arguments.of("""
                        {
                            "type": "command",
                            "uuid": "2e4107c4-8773-4e62-a400-7e7c8195e918",
                            "system": "motorcontrol"
                        }
                        """), Arguments.of("""
                        {
                            "type": "command",
                            "uuid": "2e4107c4-8773-4e62-a400-7e7c8195e918",
                            "system": "motorcontrol",
                            "method": "move",
                            "parameters": "mode"
                        }
                        """), //
                Arguments.of("""
                        {
                            "type": "command",
                            "uuid": "2e4107c4-8773-4e62-a400-7e7c8195e918",
                            "system": "motorcontrol",
                            "method": "move",
                            "parameters": {
                                "mode": ""
                            }
                        }
                        """));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void fromJson() throws DeviceMessageException {
        final var json = """
                {
                    "type": "request",
                    "id": "2e4107c4-8773-4e62-a400-7e7c8195e918",
                    "subsystem": "motorcontrol",
                    "topic": "move",
                    "parameters" : {
                        "mode": "smooth",
                        "x-pos": 42.42,
                        "speed": 100,
                        "async": true,
                        "innerParams" : {
                            "insideMode" : "not smooth"
                        }
                    }
                }""";

        final var message = this.deviceMessageService.deserializeMessage(json);
        final var request = assertInstanceOf(RequestMessage.class, message);
        // System.out.println(request);

        assertNotNull(request);
        assertEquals(ANY_UUID, request.getId());
        assertEquals(Subsystem.MOTOR_CONTROL, request.getSubsystem());
        assertEquals("move", request.getTopic());
        assertEquals("smooth", request.parameters().getString("mode").get());
        assertEquals(42.42, request.parameters().getRequiredDouble("x-pos"));
        assertEquals(100, request.parameters().getRequiredInt("speed"));
        assertTrue(request.parameters().getBoolean("async").get());
        final var innerParams = new MessageParametersImpl();
        innerParams.putString("insideMode", "not smooth");
        assertEquals(innerParams, request.parameters().getNested("innerParams").get());
    }

    @Test
    public void toJson() throws DeviceMessageException, JsonProcessingException {
        final var message = new RequestMessage(ANY_UUID, Subsystem.MOTOR_CONTROL, "move");
        message.parameters().putString("mode", "smooth");
        message.parameters().putDouble("x-pos", 42.42);
        message.parameters().putInt("speed", 100);
        message.parameters().putBoolean("async", true);
        final var params2 = message.parameters().addNested("innerParams");
        params2.putString("insideMode", "not smooth");
        final var json = this.deviceMessageService.serializeMessage(message);
        // System.out.println(json);
        final var expected = """
                {
                  "type": "request",
                  "id": "2e4107c4-8773-4e62-a400-7e7c8195e918",
                  "subsystem": "motorcontrol",
                  "topic": "move",
                  "parameters": {
                    "mode": "smooth",
                    "async": true,
                    "innerParams": {
                      "insideMode": "not smooth"
                    },
                    "x-pos": 42.42,
                    "speed": 100
                  }
                }
                """;
        final var mapper = new ObjectMapper(); // maps to JsonNode preventing problems with string literals
        assertEquals(mapper.readTree(expected), mapper.readTree(json));
    }

    @ParameterizedTest()
    @MethodSource("invalidJson")
    public void fromInvalidJson(final String json) {
        assertThrows(DeviceMessageException.class, () -> this.deviceMessageService.deserializeMessage(json));
    }

    @Test
    public void testEquals() {
        final var request1 = new RequestMessage(UUID.randomUUID(), Subsystem.HIGH_VOLTAGE, "topic1");
        final var request2 = new RequestMessage(UUID.randomUUID(), Subsystem.FLUIDICS, "topic2");

        assertEquals(request1, request1);
        assertEquals(request2, request2);

        assertNotEquals(request1, request2);
        assertNotEquals(request2, request1);
    }
}

