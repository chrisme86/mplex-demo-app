package com.cmiethling.mplex.client.controller;

import java.util.UUID;
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
import com.cmiethling.mplex.client_api.api.FluidicsApi;
import com.cmiethling.mplex.client_api.model.fluidics.SetGelPumpRequest;
import com.cmiethling.mplex.client_api.model.fluidics.SetGelPumpRequestAllOfParameters;
import com.cmiethling.mplex.device.DeviceException;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("SameReturnValue")
@Controller
@Slf4j
public class HomeController {
    @Autowired
    private DeviceCorePart deviceCorePart;
    @Autowired
    private FluidicsService fluidicsService;
    @Autowired
    private HighVoltageService highVoltageService;

    @Autowired
    private FluidicsApi fluidicsApi;

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
        final var request = new SetGelPumpRequest();
        request.id(UUID.randomUUID()).type(SetGelPumpRequest.TypeEnum.REQUEST)
                .subsystem(SetGelPumpRequest.SubsystemEnum.FLUIDICS).topic(SetGelPumpRequest.TopicEnum.SET_GEL_PUMP)
                .parameters(new SetGelPumpRequestAllOfParameters().isOn(isOn));
        System.out.println("Request: " + request);
        try {
            final var response = this.fluidicsApi.setGelPumpCommand(request);
            System.out.println("Response: " + response);
        } catch (final Exception e) {
            log.error("no response: ", e);
        }
        this.fluidicsService.sendGelPumpMode(isOn);
        return "redirect:" + Utils.HOME;
    }
}
