package com.cmiethling.mplex.emulator.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.cmiethling.mplex.device_events.api.DeviceEvent;
import com.cmiethling.mplex.device_events.api.Subsystem;
import com.cmiethling.mplex.device_events.api.SubsystemError;
import com.cmiethling.mplex.device_events.api.fluidics.ErrorEvent;
import com.cmiethling.mplex.device_events.api.fluidics.FluidicsError;
import com.cmiethling.mplex.device_events.api.hv.HighVoltageError;
import com.cmiethling.mplex.emulator.service.BrowserUpdateService;
import com.cmiethling.mplex.emulator.service.FluidicsService;
import com.cmiethling.mplex.emulator.service.HighVoltageService;

import lombok.val;

@SuppressWarnings("SameReturnValue")
@Controller
public class HomeController {

    @Autowired
    private FluidicsService fluidicsService;
    @Autowired
    private HighVoltageService highVoltageService;
    @Autowired
    private BrowserUpdateService browserUpdateService;

    @GetMapping({ "/home", "/" })
    public String getErrorEvents(final Model model) {
        val fluidicsError = new ErrorEvent();
        fluidicsError.setError(this.fluidicsService.getFluidicsError());
        val hvError = new com.cmiethling.mplex.device_events.api.hv.ErrorEvent();
        hvError.setError(this.highVoltageService.getHighVoltageError());
        final List<DeviceEvent> errorEvents = Arrays.asList(fluidicsError, hvError);
        model.addAttribute("errorEvents", errorEvents);

        // Mapping of Subsystem to its corresponding error types
        final Map<Subsystem, SubsystemError[]> errorTypesMap = Map.of(Subsystem.FLUIDICS, FluidicsError.values(),
                Subsystem.HIGH_VOLTAGE, HighVoltageError.values());

        // for dropdown menus
        model.addAttribute("errorTypesMap", errorTypesMap);

        // current device states, set by commands from the client
        model.addAttribute("fluidicsStatus", this.fluidicsService.getFluidicsStatus());

        return "home";
    }

    @GetMapping("/states")
    public String getStates(final Model model) {
        model.addAttribute("fluidicsStatus", this.fluidicsService.getFluidicsStatus());
        return "home :: statesTable"; // only return th:fragment="statesTable"
    }

    // browser listens here and reloads the states table on each "update" event
    @GetMapping("/stream")
    public SseEmitter stream() {
        return this.browserUpdateService.register();
    }

    @PostMapping("/send-event")
    public String sendEvent(@RequestParam final Subsystem subsystem, @RequestParam final String newValue) {
        try {
            switch (subsystem) {
            case FLUIDICS -> this.fluidicsService.processError(newValue);
            case HIGH_VOLTAGE -> this.highVoltageService.processError(newValue);
            default -> throw new IllegalArgumentException("invalid subsystem: " + subsystem);
            }
        } catch (final IllegalArgumentException e) { // eg unknown error name
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return "redirect:/home";
    }
}
