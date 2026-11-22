package com.consultorio.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Habilita autorización por rol en métodos. Se define aparte de {@link SecurityConfig}
 * para no modificar la cadena de seguridad compartida con C-02.
 */
@Configuration
@EnableMethodSecurity
public class AdminSecurityConfig {
}
