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

import com.cmiethling.mplex.device.api.SubsystemError;
import com.cmiethling.mplex.device.api.fluidics.FluidicsError;
import com.cmiethling.mplex.device.api.hv.HighVoltageError;
import com.cmiethling.mplex.device.message.Subsystem;
import com.cmiethling.mplex.emulator.model.ErrorEvent;
import com.cmiethling.mplex.emulator.service.FluidicsService;
import com.cmiethling.mplex.emulator.service.HighVoltageService;

@SuppressWarnings("SameReturnValue")
@Controller
public class HomeController {

    @Autowired
    private FluidicsService fluidicsService;
    @Autowired
    private HighVoltageService highVoltageService;

    @GetMapping({ "/home", "/" })
    public String getErrorEvents(final Model model) {
        final List<ErrorEvent> errorEvents = Arrays.asList(
                new ErrorEvent(Subsystem.FLUIDICS, this.fluidicsService.getFluidicsError()),
                new ErrorEvent(Subsystem.HIGH_VOLTAGE, this.highVoltageService.getHighVoltageError()));
        // System.out.println(errorEvents.get(0).getError().getClass());

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
