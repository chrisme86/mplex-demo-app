package com.cmiethling.mplex.client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;

import com.cmiethling.mplex.client.config.Utils;
import com.cmiethling.mplex.client.model.FluidicsStatus;
import com.cmiethling.mplex.client_api.api.FluidicsApi;
import com.cmiethling.mplex.client_api.model.SetGelPumpRequest;
import com.cmiethling.mplex.client_api.model.SetGelPumpRequestAllOfParameters;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("SameReturnValue")
@Controller
@Slf4j
public class HomeController {

    @Autowired
    private FluidicsApi fluidicsApi;
    @Autowired
    private FluidicsStatus fluidicsStatus;

    @GetMapping({ Utils.HOME, Utils.PUBLIC + Utils.HOME, "/", "" })
    public String displayHome(@RequestParam(required = false) final boolean logout, final Model model) {
        final var msg = logout ? "Successfully logged out." : null;
        model.addAttribute("message", msg);

        model.addAttribute("fluidicsStatus", this.fluidicsStatus);
        return Utils.HOME_HTML;
    }

    @PostMapping(Utils.PUBLIC + Utils.SEND_GEL_PUMP_MODE_COMMAND)
    public String sendGelPumpModeCommand(@RequestParam final boolean isOn) {
        final var request = new SetGelPumpRequest() //
                .subsystem(SetGelPumpRequest.SubsystemEnum.FLUIDICS) //
                .topic(SetGelPumpRequest.TopicEnum.SET_GEL_PUMP) //
                .parameters(new SetGelPumpRequestAllOfParameters().isOn(isOn));
        log.info("Sending Request: {}", request);
        try {
            final var response = this.fluidicsApi.setGelPumpCommand(request);
            log.info("Received Response: {}", response);
            this.fluidicsStatus.setGelPump(isOn);
        } catch (final RestClientException e) {
            log.error("error while receiving response: ", e);
        }
        return "redirect:" + Utils.HOME;
    }
}
