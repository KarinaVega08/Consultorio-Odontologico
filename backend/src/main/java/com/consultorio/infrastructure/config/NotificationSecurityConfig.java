package com.consultorio.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cadena de seguridad propia para los enlaces públicos de notificación.
 * Se define aparte de {@link SecurityConfig} para no modificar la whitelist compartida.
 */
@Configuration
public class NotificationSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain notificationLinksSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/notifications/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
