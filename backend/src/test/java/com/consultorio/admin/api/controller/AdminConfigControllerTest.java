package com.consultorio.admin.api.controller;

import com.consultorio.admin.application.dto.PrestacionResponse;
import com.consultorio.admin.application.dto.ProfesionalResponse;
import com.consultorio.admin.application.service.ConfiguracionConsultorioService;
import com.consultorio.infrastructure.config.AdminSecurityConfig;
import com.consultorio.infrastructure.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminConfigController.class)
@Import({SecurityConfig.class, AdminSecurityConfig.class})
class AdminConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConfiguracionConsultorioService service;

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void crearProfesionalComoAdminDevuelve201() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.crearProfesional(any())).thenReturn(new ProfesionalResponse(
            id, "Dra. Ana Ficticia", "MP-99999", true));

        mockMvc.perform(post("/api/admin/professionals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(java.util.Map.of(
                    "nombreCompleto", "Dra. Ana Ficticia",
                    "matricula", "MP-99999"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void crearProfesionalConNombreVacioDevuelve400() throws Exception {
        mockMvc.perform(post("/api/admin/professionals")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombreCompleto\":\"\",\"matricula\":\"MP-99999\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.nombreCompleto").exists());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void crearPrestacionConDuracionCeroDevuelve400() throws Exception {
        mockMvc.perform(post("/api/admin/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Inválida\",\"duracionMinutos\":0}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.duracionMinutos").exists());
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    void crearProfesionalComoPacienteDevuelve403() throws Exception {
        mockMvc.perform(post("/api/admin/professionals")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombreCompleto\":\"Dra. Ana Ficticia\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void crearProfesionalSinAutenticacionDevuelve403() throws Exception {
        mockMvc.perform(post("/api/admin/professionals")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombreCompleto\":\"Dra. Ana Ficticia\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void crearPrestacionValidaDevuelve201() throws Exception {
        when(service.crearPrestacion(any())).thenReturn(new PrestacionResponse(
            UUID.randomUUID(), "Endodoncia", 60, "Dato ficticio", true));

        mockMvc.perform(post("/api/admin/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Endodoncia\",\"duracionMinutos\":60}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.duracionMinutos").value(60));
    }
}
