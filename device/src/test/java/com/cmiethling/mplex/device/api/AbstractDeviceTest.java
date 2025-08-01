package com.cmiethling.mplex.device.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.cmiethling.mplex.device.DeviceMessageException;
import com.cmiethling.mplex.device.config.DeviceMessageConfig;
import com.cmiethling.mplex.device.message.DeviceMessage;
import com.cmiethling.mplex.device.service.DeviceMessageService;
import com.cmiethling.mplex.device.service.EventCommandFactory;

@SpringJUnitConfig(classes = { DeviceMessageService.class, EventCommandFactory.class, DeviceMessageConfig.class })
public abstract class AbstractDeviceTest {
    @Autowired
    protected DeviceMessageService deviceMessageService;
    @Autowired
    protected EventCommandFactory eventCommandFactory;

    protected DeviceMessage loadMessage(final String resourceName) throws IOException, DeviceMessageException {
        return this.deviceMessageService.deserializeMessage(loadJson(resourceName));
    }

    protected String loadJson(final String resourceName) throws IOException {
        final var fullResourceName = resourceName + ".json";
        try (final var in = getClass().getResourceAsStream(fullResourceName)) {
            assertNotNull(in, "Could not find resource: " + fullResourceName);
            try (final var isr = new InputStreamReader(in); final var br = new BufferedReader(isr)) {
                final var content = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    content.append(line);
                    content.append('\n');
                }

                return content.toString();
            }
        }
    }
}

