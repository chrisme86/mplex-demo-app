package com.cmiethling.mplex.client.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.cmiethling.mplex.client.config.Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class LoginController {

    @RequestMapping(value = Utils.LOGIN, method = { RequestMethod.GET, RequestMethod.POST })
    public String login(final Model model, // /login?error=true
            @RequestParam(required = false) final boolean error) {

        String msg = null;
        if (error)
            msg = "Username or Password is incorrect!";
        model.addAttribute("message", msg);

        return Utils.LOGIN_HTML;
    }

    @GetMapping(value = Utils.LOGOUT)
    public String logoutPage(final HttpServletRequest request, final HttpServletResponse response) {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null)
            new SecurityContextLogoutHandler().logout(request, response, auth);

        return "redirect:" + Utils.HOME + "?logout=true";
    }
}
