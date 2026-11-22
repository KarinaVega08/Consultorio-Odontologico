package com.consultorio.infrastructure.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Configuración de prueba: expone TestExceptionController como bean solo para tests,
 * y un SecurityFilterChain permisivo para que los tests de excepciones no requieran auth.
 * No forma parte del código productivo.
 */
@TestConfiguration
public class TestConfig {

    @Bean
    public SecurityFilterChain permitAllSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @RestController
    public static class TestExceptionController {

        @GetMapping("/api/test-exception")
        public void testException() {
            throw new RuntimeException("error de prueba intencional");
        }
    }
}
