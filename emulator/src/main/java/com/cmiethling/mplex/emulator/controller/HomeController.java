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
import com.cmiethling.mplex.device_events.api.brewer.BrewerError;
import com.cmiethling.mplex.device_events.api.brewer.ErrorEvent;
import com.cmiethling.mplex.device_events.api.heater.HeaterError;
import com.cmiethling.mplex.emulator.service.BrewerService;
import com.cmiethling.mplex.emulator.service.BrowserUpdateService;
import com.cmiethling.mplex.emulator.service.HeaterService;

import lombok.val;

@SuppressWarnings("SameReturnValue")
@Controller
public class HomeController {

    @Autowired
    private BrewerService brewerService;
    @Autowired
    private HeaterService heaterService;
    @Autowired
    private BrowserUpdateService browserUpdateService;

    @GetMapping({ "/home", "/" })
    public String getErrorEvents(final Model model) {
        val brewerError = new ErrorEvent();
        brewerError.setError(this.brewerService.getBrewerError());
        val heaterError = new com.cmiethling.mplex.device_events.api.heater.ErrorEvent();
        heaterError.setError(this.heaterService.getHeaterError());
        final List<DeviceEvent> errorEvents = Arrays.asList(heaterError, brewerError);
        model.addAttribute("errorEvents", errorEvents);

        // Mapping of Subsystem to its corresponding error types
        final Map<Subsystem, SubsystemError[]> errorTypesMap = Map.of(Subsystem.BREWER, BrewerError.values(),
                Subsystem.HEATER, HeaterError.values());

        // for dropdown menus
        model.addAttribute("errorTypesMap", errorTypesMap);

        // current device states, set by commands from the client
        addStates(model);

        return "home";
    }

    @GetMapping("/states")
    public String getStates(final Model model) {
        addStates(model);
        return "home :: statesTable"; // only return th:fragment="statesTable"
    }

    private void addStates(final Model model) {
        model.addAttribute("brewerStatus", this.brewerService.getBrewerStatus());
        model.addAttribute("heaterStatus", this.heaterService.getHeaterStatus());
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
            case BREWER -> this.brewerService.processError(newValue);
            case HEATER -> this.heaterService.processError(newValue);
            default -> throw new IllegalArgumentException("invalid subsystem: " + subsystem);
            }
        } catch (final IllegalArgumentException e) { // eg unknown error name
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return "redirect:/home";
    }
}
