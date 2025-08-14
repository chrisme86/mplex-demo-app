package com.cmiethling.mplex.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
public class ProjectSecurityConfig {

    public static final String SERVICE_ROLE = "SERVICE";

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(final HttpSecurity http,
            final HandlerMappingIntrospector introspector) throws Exception {
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers("/", Utils.HOME).permitAll();
            auth.requestMatchers(Utils.PUBLIC + "/**").permitAll();
            // so that css in /assets can work
            auth.requestMatchers("/assets/**").permitAll();
            auth.requestMatchers("/error").permitAll();

            auth.requestMatchers(Utils.SERVICE_CLIENT + "/**").authenticated();
            auth.requestMatchers(Utils.LOGIN, Utils.LOGOUT).permitAll();
            // for OpenAPI
            auth.requestMatchers("/api-docs/**", "/swagger-ui/**", "/api/**").permitAll();
        });
        http.httpBasic(Customizer.withDefaults());

        http.formLogin(loginConfigure -> loginConfigure.loginPage(Utils.LOGIN).defaultSuccessUrl(Utils.SERVICE_CLIENT)
                .failureUrl(Utils.LOGIN + "?error=true").permitAll());

        // all POSTS in home controller are allowed
        http.csrf(csrfConfigurer -> {
            csrfConfigurer.ignoringRequestMatchers(Utils.PUBLIC + "/**");
            csrfConfigurer.ignoringRequestMatchers("/api/**");
        });
        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        final var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        final var serviceTechnician = User.withUsername("service").password(encoder.encode("service"))
                .roles(SERVICE_ROLE).build();
        return new InMemoryUserDetailsManager(serviceTechnician);
    }
}
