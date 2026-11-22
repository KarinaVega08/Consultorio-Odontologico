package com.consultorio.admin.api.controller;

import com.consultorio.admin.application.dto.BloqueoResponse;
import com.consultorio.admin.application.dto.HorarioAtencionResponse;
import com.consultorio.admin.application.dto.PrestacionResponse;
import com.consultorio.admin.application.dto.ProfesionalResponse;
import com.consultorio.admin.application.dto.SillonBoxResponse;
import com.consultorio.admin.application.service.ConfiguracionConsultorioService;
import com.consultorio.infrastructure.config.AdminSecurityConfig;
import com.consultorio.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ENDPOINTS CRUD (GET/PUT/PATCH) de todos los recursos administrativos.
 */
@WebMvcTest(AdminConfigController.class)
@Import({SecurityConfig.class, AdminSecurityConfig.class})
class AdminConfigCrudEndpointsTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private ConfiguracionConsultorioService service;

    private final UUID id = UUID.randomUUID();

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void listarProfesionalesDevuelve200() throws Exception {
        when(service.listarProfesionales()).thenReturn(List.of(
            new ProfesionalResponse(id, "Dra. Ana Ficticia", "MP-99999", true)));

        mockMvc.perform(get("/api/admin/professionals"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].matricula").value("MP-99999"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void actualizarProfesionalDevuelve200() throws Exception {
        when(service.actualizarProfesional(eq(id), any())).thenReturn(
            new ProfesionalResponse(id, "Dra. Ana Actualizada", "MP-99998", true));

        mockMvc.perform(put("/api/admin/professionals/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombreCompleto\":\"Dra. Ana Actualizada\",\"matricula\":\"MP-99998\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.matricula").value("MP-99998"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void desactivarProfesionalDevuelve200() throws Exception {
        when(service.activarProfesional(id, false)).thenReturn(
            new ProfesionalResponse(id, "Dra. Ana Ficticia", "MP-99999", false));

        mockMvc.perform(patch("/api/admin/professionals/{id}/active", id).param("activo", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void listarSillonesBoxesDevuelve200() throws Exception {
        when(service.listarSillonesBoxes()).thenReturn(List.of(new SillonBoxResponse(id, "Sillón 1", true)));

        mockMvc.perform(get("/api/admin/consulting-rooms"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nombre").value("Sillón 1"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void actualizarSillonBoxDevuelve200() throws Exception {
        when(service.actualizarSillonBox(eq(id), any())).thenReturn(new SillonBoxResponse(id, "Sillón 2", true));

        mockMvc.perform(put("/api/admin/consulting-rooms/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Sillón 2\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Sillón 2"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void desactivarSillonBoxDevuelve200() throws Exception {
        when(service.activarSillonBox(id, false)).thenReturn(new SillonBoxResponse(id, "Sillón 1", false));

        mockMvc.perform(patch("/api/admin/consulting-rooms/{id}/active", id).param("activo", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void listarPrestacionesDevuelve200() throws Exception {
        when(service.listarPrestaciones()).thenReturn(List.of(
            new PrestacionResponse(id, "Endodoncia", 60, "Ficticio", true)));

        mockMvc.perform(get("/api/admin/services"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].duracionMinutos").value(60));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void actualizarPrestacionDevuelve200() throws Exception {
        when(service.actualizarPrestacion(eq(id), any())).thenReturn(
            new PrestacionResponse(id, "Endodoncia", 60, "Ficticio", true));

        mockMvc.perform(put("/api/admin/services/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Endodoncia\",\"duracionMinutos\":60}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.duracionMinutos").value(60));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void desactivarPrestacionDevuelve200() throws Exception {
        when(service.activarPrestacion(id, false)).thenReturn(
            new PrestacionResponse(id, "Endodoncia", 60, "Ficticio", false));

        mockMvc.perform(patch("/api/admin/services/{id}/active", id).param("activo", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void listarHorariosPorProfesionalDevuelve200() throws Exception {
        when(service.listarHorariosPorProfesional(id)).thenReturn(List.of(horarioResponse()));

        mockMvc.perform(get("/api/admin/schedules").param("profesionalId", id.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].diaSemana").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void actualizarHorarioDevuelve200() throws Exception {
        when(service.actualizarHorario(eq(id), any())).thenReturn(horarioResponse());

        mockMvc.perform(put("/api/admin/schedules/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"profesionalId\":\"" + id + "\",\"diaSemana\":1,"
                    + "\"horaInicio\":\"09:00:00\",\"horaFin\":\"17:00:00\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.horaInicio").value("09:00:00"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void desactivarHorarioDevuelve200() throws Exception {
        when(service.activarHorario(id, false)).thenReturn(
            new HorarioAtencionResponse(id, id, 1, LocalTime.of(9, 0), LocalTime.of(17, 0), false));

        mockMvc.perform(patch("/api/admin/schedules/{id}/active", id).param("activo", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void listarBloqueosPorSillonDevuelve200() throws Exception {
        when(service.listarBloqueosPorSillonBox(id)).thenReturn(List.of(bloqueoResponse()));

        mockMvc.perform(get("/api/admin/blockers").param("sillonBoxId", id.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].motivo").value("Capacitación"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void actualizarBloqueoDevuelve200() throws Exception {
        when(service.actualizarBloqueo(eq(id), any())).thenReturn(bloqueoResponse());

        mockMvc.perform(put("/api/admin/blockers/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"profesionalId\":\"" + id + "\",\"sillonBoxId\":\"" + id + "\","
                    + "\"fechaHoraInicio\":\"2026-11-25T10:00:00\",\"fechaHoraFin\":\"2026-11-25T11:00:00\","
                    + "\"motivo\":\"Capacitación\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.motivo").value("Capacitación"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void desactivarBloqueoDevuelve200() throws Exception {
        when(service.activarBloqueo(id, false)).thenReturn(
            new BloqueoResponse(id, id, id, LocalDateTime.of(2026, 11, 25, 10, 0),
                LocalDateTime.of(2026, 11, 25, 11, 0), "Capacitación", false));

        mockMvc.perform(patch("/api/admin/blockers/{id}/active", id).param("activo", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void actualizarHorarioConDiaInvalidoDevuelve400() throws Exception {
        mockMvc.perform(put("/api/admin/schedules/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"profesionalId\":\"" + id + "\",\"diaSemana\":8,"
                    + "\"horaInicio\":\"09:00:00\",\"horaFin\":\"17:00:00\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.diaSemana").exists());
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    void pacienteNoPuedeListarProfesionalesDevuelve403() throws Exception {
        mockMvc.perform(get("/api/admin/professionals"))
            .andExpect(status().isForbidden());
    }

    private HorarioAtencionResponse horarioResponse() {
        return new HorarioAtencionResponse(id, id, 1, LocalTime.of(9, 0), LocalTime.of(17, 0), true);
    }

    private BloqueoResponse bloqueoResponse() {
        return new BloqueoResponse(id, id, id, LocalDateTime.of(2026, 11, 25, 10, 0),
            LocalDateTime.of(2026, 11, 25, 11, 0), "Capacitación", true);
    }
}
