package com.consultorio.infrastructure.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import com.consultorio.api.controller.HealthController;

/**
 * Test en rojo (Strict TDD): rutas públicas accesibles sin auth y protegidas requieren auth.
 * SecurityConfig aún no existía; se implementó. Ahora el test debe pasar en verde.
 */
@WebMvcTest(HealthController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

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
}
