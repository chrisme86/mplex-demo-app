package com.cmiethling.mplex.client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cmiethling.mplex.client.config.Utils;
import com.cmiethling.mplex.client.service.DeviceCommandException;
import com.cmiethling.mplex.client.service.FluidicsService;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("SameReturnValue")
@Controller
@Slf4j
public class HomeController {

    @Autowired
    private FluidicsService fluidicsService;

    @GetMapping({ Utils.HOME, Utils.PUBLIC + Utils.HOME, "/", "" })
    public String displayHome(@RequestParam(required = false) final boolean logout, final Model model) {
        // don't overwrite a flash message from a rejected command
        if (logout)
            model.addAttribute("message", "Successfully logged out.");

        model.addAttribute("fluidicsStatus", this.fluidicsService.getFluidicsStatus());
        return Utils.HOME_HTML;
    }

    @PostMapping(Utils.PUBLIC + Utils.SEND_GEL_PUMP_MODE_COMMAND)
    public String sendGelPumpModeCommand(@RequestParam final boolean isOn, final RedirectAttributes redirect) {
        try {
            this.fluidicsService.setGelPumpCommand(isOn);
        } catch (final DeviceCommandException e) {
            redirect.addFlashAttribute("message", e.getMessage()); // shown once after the redirect
        }
        return "redirect:" + Utils.HOME;
    }
}
