package com.consultorio.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El catálogo de combos debe ser consultable sin token (el navegador no envía Bearer
 * en la navegación y los endpoints /api/admin/** requieren rol ADMINISTRADOR).
 */
@SpringBootTest
@AutoConfigureMockMvc
class PublicCatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void catalogoEsPublicoYDevuelveTresListas() throws Exception {
        mockMvc.perform(get("/api/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profesionales").isArray())
                .andExpect(jsonPath("$.prestaciones").isArray())
                .andExpect(jsonPath("$.sillonesBoxes").isArray());
    }
}
