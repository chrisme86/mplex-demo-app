package com.cmiethling.mplex.client.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class Utils {

    public static final String PUBLIC = "/public";

    // web pages
    public static final String LOGIN = "/login";
    public static final String LOGOUT = "/logout";
    public static final String SERVICE_CLIENT = "/service_client";
    public static final String HOME = "/home";

    public static final String LOGIN_HTML = getHtmlString(LOGIN);
    public static final String LOGOUT_HTML = getHtmlString(LOGOUT);
    public static final String SERVICE_CLIENT_HTML = getHtmlString(SERVICE_CLIENT);
    public static final String HOME_HTML = getHtmlString(HOME);

    private static String getHtmlString(final String url) {
        return url.substring(1) + ".html";
    }
}
