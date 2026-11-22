package com.consultorio.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import com.consultorio.infrastructure.config.SecurityConfig;

/**
 * Test en rojo (Strict TDD): /api/health debe devolver HTTP 200.
 * HealthController aún no existe; este test no compila hasta implementarlo.
 */
@WebMvcTest(HealthController.class)
@Import(SecurityConfig.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk());
    }
}
