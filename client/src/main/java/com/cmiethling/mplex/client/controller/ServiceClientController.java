package com.cmiethling.mplex.client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cmiethling.mplex.client.config.Utils;
import com.cmiethling.mplex.client.service.BrowserUpdateService;
import com.cmiethling.mplex.client.service.DeviceCommandException;
import com.cmiethling.mplex.client.service.FluidicsService;
import com.cmiethling.mplex.client.service.HighVoltageService;

@SuppressWarnings("SameReturnValue")
@Controller
@RequestMapping(Utils.SERVICE_CLIENT)
public class ServiceClientController {

    @Autowired
    private FluidicsService fluidicsService;
    @Autowired
    private HighVoltageService highVoltageService;
    @Autowired
    private BrowserUpdateService browserUpdateService;

    @GetMapping
    public String displayServiceClient(final Model model) {
        addEvents(model);
        return Utils.SERVICE_CLIENT_HTML;
    }

    @GetMapping("/events")
    public String getEvents(final Model model) {
        addEvents(model);
        return "%s :: eventsTable".formatted(Utils.SERVICE_CLIENT); // only return th:fragment="eventsTable"
    }

    // browser listens here and reloads the events table on each "update" event
    @GetMapping("/stream")
    public SseEmitter stream() {
        return this.browserUpdateService.register();
    }

    private void addEvents(final Model model) {
        model.addAttribute("fluidicsStatus", this.fluidicsService.getFluidicsStatus());
        model.addAttribute("highVoltageStatus", this.highVoltageService.getHighVoltageStatus());
    }

    @PostMapping("/sendGelPumpModeCommand2")
    public String sendGelPumpModeCommand(@RequestParam final boolean isOn, final RedirectAttributes redirect) {
        try {
            this.fluidicsService.setGelPumpCommand(isOn);
        } catch (final DeviceCommandException e) {
            redirect.addFlashAttribute("message", e.getMessage()); // shown once after the redirect
        }
        return "redirect:" + Utils.SERVICE_CLIENT;
    }

    @PostMapping("/sendGelValveCommand")
    public String sendGelValveCommand(@RequestParam final boolean isOpen, final RedirectAttributes redirect) {
        try {
            this.fluidicsService.setGelValveCommand(isOpen);
        } catch (final DeviceCommandException e) {
            redirect.addFlashAttribute("message", e.getMessage()); // shown once after the redirect
        }
        return "redirect:" + Utils.SERVICE_CLIENT;
    }
}
