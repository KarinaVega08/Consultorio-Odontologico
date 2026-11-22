package com.consultorio.infrastructure.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import com.consultorio.api.controller.HealthController;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test: rutas públicas accesibles sin auth, protegidas requieren auth,
 * y bean de PasswordEncoder disponible.
 */
@WebMvcTest(HealthController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void publicRoutesShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk());
    }

    @Test
    void protectedRoutesShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/appointments"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void passwordEncoderShouldBeAvailableAndWorking() {
        assertNotNull(passwordEncoder);
        String rawPassword = "password123";
        String encoded = passwordEncoder.encode(rawPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
    }
}
