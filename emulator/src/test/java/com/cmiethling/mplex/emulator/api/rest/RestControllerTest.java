package com.cmiethling.mplex.emulator.api.rest;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.cmiethling.mplex.emulator.service.BrewerService;
import com.cmiethling.mplex.emulator.service.DeviceStateConflictException;
import com.cmiethling.mplex.emulator.service.HeaterService;

/**
 * HTTP answers of the device REST API as described in the OpenAPI spec.
 */
@WebMvcTest({ BrewerRestController.class, HeaterRestController.class })
class RestControllerTest {

    private static final String PUMP_ON = """
            {"subsystem": "brewer", "topic": "SetWaterPump", "parameters": {"isOn": true}}""";
    private static final String HEATER_OFF = """
            {"subsystem": "heater", "topic": "SetWaterHeater", "parameters": {"isOn": false}}""";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BrewerService brewerService;
    @MockitoBean
    private HeaterService heaterService;

    @Test
    void acceptedCommandReturns202WithCommandId() throws Exception {
        final var commandId = UUID.randomUUID();
        when(this.brewerService.setWaterPump(true)).thenReturn(commandId);

        this.mockMvc.perform(post("/restapi/brewer/SetWaterPump").contentType(MediaType.APPLICATION_JSON).content(PUMP_ON))
                .andExpect(status().isAccepted()) //
                .andExpect(jsonPath("$.commandId").value(commandId.toString()));
    }

    @Test
    void rejectedCommandReturns409AsProblemDetail() throws Exception {
        when(this.heaterService.setWaterHeater(false)).thenThrow(new DeviceStateConflictException("Water pump is running"));

        this.mockMvc
                .perform(post("/restapi/heater/SetWaterHeater").contentType(MediaType.APPLICATION_JSON).content(HEATER_OFF))
                .andExpect(status().isConflict()) //
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON)) //
                .andExpect(jsonPath("$.detail").value("Water pump is running"));
    }

    @Test
    void malformedRequestReturns400() throws Exception {
        this.mockMvc
                .perform(post("/restapi/brewer/SetWaterPump").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parameters\": {\"isOn\": \"maybe\"}}"))
                .andExpect(status().isBadRequest());
    }
}
