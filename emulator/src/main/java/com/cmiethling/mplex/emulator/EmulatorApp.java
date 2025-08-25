package com.cmiethling.mplex.emulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.cmiethling.mplex.emulator" })
public class EmulatorApp {
    public static void main(final String[] args) {
        SpringApplication.run(EmulatorApp.class, args);
    }
}
