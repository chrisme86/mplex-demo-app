package com.cmiethling.mplex.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
public class ProjectSecurityConfig {

    public static final String SERVICE_ROLE = "SERVICE";

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(final HttpSecurity http,
            final HandlerMappingIntrospector introspector) throws Exception {
        final var builder = new MvcRequestMatcher.Builder(introspector);
        http.authorizeHttpRequests(request -> {
            request.requestMatchers(builder.pattern("/"), builder.pattern(Utils.HOME)).permitAll();
            request.requestMatchers(builder.pattern(Utils.PUBLIC + "/**")).permitAll();
            // so that css in /assets can work
            request.requestMatchers(builder.pattern("/assets/**")).permitAll();
            request.requestMatchers(builder.pattern("/error")).permitAll();

            request.requestMatchers(builder.pattern(Utils.SERVICE_CLIENT + "/**")).authenticated();
            request.requestMatchers(builder.pattern(Utils.LOGIN), builder.pattern(Utils.LOGOUT)).permitAll();
            // for OpenAPI
            request.requestMatchers(builder.pattern("/api-docs/**"), builder.pattern("/swagger-ui/**"),
                    builder.pattern("/api/**")).permitAll();
        });
        http.httpBasic(Customizer.withDefaults());

        http.formLogin(loginConfigure -> loginConfigure.loginPage(Utils.LOGIN).defaultSuccessUrl(Utils.SERVICE_CLIENT)
                .failureUrl(Utils.LOGIN + "?error=true").permitAll());

        // all POSTS in home controller are allowed
        http.csrf(csrfConfigurer -> {
            csrfConfigurer.ignoringRequestMatchers(builder.pattern(Utils.PUBLIC + "/**"));
            csrfConfigurer.ignoringRequestMatchers(builder.pattern("/api/**"));
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
