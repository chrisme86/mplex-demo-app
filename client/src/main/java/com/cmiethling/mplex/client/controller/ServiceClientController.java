package com.cmiethling.mplex.client.controller;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cmiethling.mplex.client.config.Utils;
import com.cmiethling.mplex.client.core.DeviceCorePart;
import com.cmiethling.mplex.client.service.FluidicsService;
import com.cmiethling.mplex.device.DeviceException;

@SuppressWarnings("SameReturnValue")
@Controller
public class ServiceClientController {
    @Autowired
    private DeviceCorePart deviceCorePart;

    @Autowired
    private FluidicsService fluidicsService;

    @GetMapping(Utils.SERVICE_CLIENT)
    public String displayServiceClient(final Model model) {
        addEvents(model);
        return Utils.SERVICE_CLIENT_HTML;
    }

    @GetMapping(Utils.SERVICE_CLIENT + "/events")
    public String getEvents(final Model model) {
        addEvents(model);
        return "%s :: eventsTable".formatted(Utils.SERVICE_CLIENT); // only return th:fragment="eventsTable"
    }

    private void addEvents(final Model model) {
        model.addAttribute("isConnected", this.deviceCorePart.isConnected());
        model.addAttribute("fluidicsStatus", this.fluidicsService.getFluidicsStatus());
    }

    @PostMapping(Utils.SERVICE_CLIENT + "/sendGelPumpModeCommand2")
    public String sendGelPumpModeCommand(@RequestParam final boolean isOn)
            throws DeviceException, ExecutionException, InterruptedException {
        this.fluidicsService.sendGelPumpMode(isOn);
        return "redirect:" + Utils.SERVICE_CLIENT;
    }

    @PostMapping(Utils.SERVICE_CLIENT + "/test")
    public String openConnection(@RequestParam final String bla)
            throws DeviceException, InterruptedException, ExecutionException, TimeoutException {
        switch (bla) {
        case "true" -> this.deviceCorePart.openConnection();
        case "false" -> this.deviceCorePart.closeConnection();
        }
        return "redirect:" + Utils.SERVICE_CLIENT;
    }
}
