package com.cmiethling.mplex.emulator.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cmiethling.mplex.device_events.api.DeviceEvent;
import com.cmiethling.mplex.device_events.api.Subsystem;
import com.cmiethling.mplex.device_events.api.SubsystemError;
import com.cmiethling.mplex.device_events.api.fluidics.ErrorEvent;
import com.cmiethling.mplex.device_events.api.fluidics.FluidicsError;
import com.cmiethling.mplex.device_events.api.hv.HighVoltageError;
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

        return "home";
    }

    @PostMapping("/send-event")
    public String sendEvent(@RequestParam final Subsystem subsystem, @RequestParam final String newValue) {
        switch (subsystem) {
        case FLUIDICS -> this.fluidicsService.processError(newValue);
        case HIGH_VOLTAGE -> this.highVoltageService.processError(newValue);
        default -> throw new IllegalArgumentException("invalid subsystem: " + subsystem);
        }
        return "redirect:/home";
    }
}
