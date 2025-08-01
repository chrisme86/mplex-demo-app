package com.cmiethling.mplex.client.controller;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cmiethling.mplex.client.config.Utils;
import com.cmiethling.mplex.client.core.DeviceCorePart;
import com.cmiethling.mplex.client.service.FluidicsService;
import com.cmiethling.mplex.client.service.HighVoltageService;
import com.cmiethling.mplex.device.DeviceException;

@SuppressWarnings("SameReturnValue")
@Controller
public class HomeController {
    @Autowired
    private DeviceCorePart deviceCorePart;
    @Autowired
    private FluidicsService fluidicsService;
    @Autowired
    private HighVoltageService highVoltageService;

    @GetMapping({ Utils.HOME, Utils.PUBLIC + Utils.HOME, "/", "" })
    public String displayHome(@RequestParam(required = false) final boolean logout, final Model model) {
        final var msg = logout ? "Successfully logged out." : null;
        model.addAttribute("message", msg);

        model.addAttribute("fluidicsStatus", this.fluidicsService.getFluidicsStatus());
        return Utils.HOME_HTML;
    }

    @PostMapping(Utils.PUBLIC + Utils.SEND_GEL_PUMP_MODE_COMMAND)
    public String sendGelPumpModeCommand(@RequestParam final boolean isOn)
            throws DeviceException, ExecutionException, InterruptedException {
        this.fluidicsService.sendGelPumpMode(isOn);
        return "redirect:" + Utils.HOME;
    }
}
