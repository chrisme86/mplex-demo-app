package com.cmiethling.mplex.client.controller;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import com.cmiethling.mplex.client.service.FluidicsService;
import com.cmiethling.mplex.client.service.HighVoltageService;
import com.cmiethling.mplex.client.stomp.StompConnection;

import lombok.RequiredArgsConstructor;

/**
 * Model attributes for the shared fragment status :: statusTable (home and service client).
 */
@Component
@RequiredArgsConstructor
public class DeviceStatusModel {
    private final FluidicsService fluidicsService;
    private final HighVoltageService highVoltageService;
    private final StompConnection stompConnection;

    public void addTo(final Model model, final boolean showReconnect) {
        model.addAttribute("fluidicsStatus", this.fluidicsService.getFluidicsStatus());
        model.addAttribute("highVoltageStatus", this.highVoltageService.getHighVoltageStatus());
        model.addAttribute("connectionState", this.stompConnection.getState());
        model.addAttribute("connectionRetry", this.stompConnection.getRetry());
        model.addAttribute("connectionMaxRetries", StompConnection.MAX_RETRIES);
        model.addAttribute("showReconnect", showReconnect);
    }
}
