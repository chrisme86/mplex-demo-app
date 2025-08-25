package com.cmiethling.mplex.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.cmiethling.mplex.client" })
public class ClientApp {
    public static void main(final String[] args) {
        SpringApplication.run(ClientApp.class, args);
    }
}
