package com.cmiethling.mplex.emulator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.cmiethling.mplex.emulator.service.LogService;

@Controller
public class LogController {

    @Autowired
    private LogService logService;

    @GetMapping("/logs")
    public String showLogs(final Model model) {
        model.addAttribute("logEntries", this.logService.getLogEntries());
        return "logs";
    }
}
