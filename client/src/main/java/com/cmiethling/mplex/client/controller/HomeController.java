package com.cmiethling.mplex.client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.cmiethling.mplex.client.config.Utils;
import com.cmiethling.mplex.client.service.BrowserUpdateService;

import lombok.extern.slf4j.Slf4j;

/**
 * Public, read-only status page. Commands are only possible in the service client (login).
 */
@SuppressWarnings("SameReturnValue")
@Controller
@Slf4j
public class HomeController {

    @Autowired
    private DeviceStatusModel deviceStatusModel;
    @Autowired
    private BrowserUpdateService browserUpdateService;

    @GetMapping({ Utils.HOME, Utils.PUBLIC + Utils.HOME, "/", "" })
    public String displayHome(@RequestParam(required = false) final boolean logout, final Model model) {
        if (logout)
            model.addAttribute("message", "Successfully logged out.");

        this.deviceStatusModel.addTo(model, false);
        return Utils.HOME_HTML;
    }

    @GetMapping(Utils.PUBLIC + "/status")
    public String getStatus(final Model model) {
        this.deviceStatusModel.addTo(model, false);
        return "status :: statusTable"; // only the fragment, no Reconnect button
    }

    // home and service client listen here and reload their status on each "update" event (carries no data)
    @GetMapping(Utils.PUBLIC + "/stream")
    public SseEmitter stream() {
        return this.browserUpdateService.register();
    }
}
