package com.cmiethling.mplex.client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cmiethling.mplex.client.config.Utils;
import com.cmiethling.mplex.client.service.DeviceCommandException;
import com.cmiethling.mplex.client.service.FluidicsService;
import com.cmiethling.mplex.client.stomp.StompConnection;

@SuppressWarnings("SameReturnValue")
@Controller
@RequestMapping(Utils.SERVICE_CLIENT)
public class ServiceClientController {

    @Autowired
    private FluidicsService fluidicsService;
    @Autowired
    private DeviceStatusModel deviceStatusModel;
    @Autowired
    private StompConnection stompConnection;

    @GetMapping
    public String displayServiceClient(final Model model) {
        this.deviceStatusModel.addTo(model, true);
        return Utils.SERVICE_CLIENT_HTML;
    }

    @GetMapping("/status")
    public String getStatus(final Model model) {
        this.deviceStatusModel.addTo(model, true);
        return "status :: statusTable"; // only the fragment, with Reconnect button
    }

    @PostMapping("/reconnect")
    public String reconnect() {
        this.stompConnection.reconnect();
        return "redirect:" + Utils.SERVICE_CLIENT;
    }

    @PostMapping("/sendGelPumpCommand")
    public String sendGelPumpCommand(@RequestParam final boolean isOn, final RedirectAttributes redirect) {
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
