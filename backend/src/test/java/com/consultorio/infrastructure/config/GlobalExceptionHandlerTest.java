package com.consultorio.infrastructure.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test en rojo (Strict TDD): GlobalExceptionHandler captura excepciones y devuelve
 * respuesta de error estructurada. TestExceptionController y seguridad permisiva se
 * proveen vía TestConfig (solo tests, no código productivo).
 */
@WebMvcTest(TestConfig.TestExceptionController.class)
@Import({GlobalExceptionHandler.class, TestConfig.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void runtimeExceptionShouldReturnErrorResponse() throws Exception {
        mockMvc.perform(get("/api/test-exception"))
            .andExpect(status().is5xxServerError())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void businessExceptionShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/test-business-exception"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("regla de negocio violada"));
    }

    @Test
    void resourceNotFoundExceptionShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/test-not-found"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("recurso no encontrado"));
    }
}
