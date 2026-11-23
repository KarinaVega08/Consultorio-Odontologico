package com.consultorio.api.controller;

import com.consultorio.application.dto.AppointmentQuery;
import com.consultorio.application.dto.AppointmentResponse;
import com.consultorio.application.dto.CreateAppointmentRequest;
import com.consultorio.application.dto.RescheduleAppointmentRequest;
import com.consultorio.application.service.TokenService;
import com.consultorio.application.service.TurnoService;
import com.consultorio.domain.exception.BusinessException;
import com.consultorio.domain.exception.ResourceNotFoundException;
import com.consultorio.domain.model.EstadoTurno;
import com.consultorio.domain.model.RolUsuario;
import com.consultorio.infrastructure.config.AdminSecurityConfig;
import com.consultorio.infrastructure.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * API REST de turnos: códigos HTTP, DTOs y mapeo de errores de negocio (datos ficticios).
 */
@WebMvcTest(AppointmentController.class)
@Import({SecurityConfig.class, AdminSecurityConfig.class})
class AppointmentControllerTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2099, 11, 25, 9, 0);
    private static final LocalDateTime FIN = INICIO.plusMinutes(30);
    private static final UUID PACIENTE_ID = UUID.randomUUID();
    private static final UUID PROFESIONAL_ID = UUID.randomUUID();
    private static final UUID SILLON_ID = UUID.randomUUID();
    private static final UUID PRESTACION_ID = UUID.randomUUID();
    private static final UUID TURNO_ID = UUID.randomUUID();

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TokenService tokenService;

    @MockitoBean private TurnoService turnoService;

    private String tokenRecepcionista;

    @BeforeEach
    void setUp() {
        tokenRecepcionista = bearer(RolUsuario.RECEPCIONISTA);
    }

    @Test
    void postCreaTurnoYDevuelve201ConFinCalculado() throws Exception {
        when(turnoService.crear(any(CreateAppointmentRequest.class), any(TokenService.AuthenticatedUser.class)))
            .thenReturn(respuesta(EstadoTurno.RESERVADO));

        mockMvc.perform(post("/api/appointments")
                .header("Authorization", tokenRecepcionista)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payloadValido())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(TURNO_ID.toString()))
            .andExpect(jsonPath("$.estado").value("RESERVADO"))
            .andExpect(jsonPath("$.fechaHoraInicio").value("2099-11-25T09:00:00"))
            .andExpect(jsonPath("$.fechaHoraFin").value("2099-11-25T09:30:00"))
            .andExpect(jsonPath("$.creadoPor").value("99999999"));
    }

    @Test
    void postConPayloadInvalidoDevuelve400ConFieldErrors() throws Exception {
        String json = """
            {
              "pacienteId": "%s",
              "profesionalId": null,
              "sillonBoxId": "%s",
              "prestacionId": null,
              "fechaHoraInicio": "2020-01-01T09:00:00"
            }
            """.formatted(PACIENTE_ID, SILLON_ID);

        mockMvc.perform(post("/api/appointments")
                .header("Authorization", tokenRecepcionista)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.profesionalId").exists())
            .andExpect(jsonPath("$.fieldErrors.prestacionId").exists())
            .andExpect(jsonPath("$.fieldErrors.fechaHoraInicio").exists());
    }

    @Test
    void postConProfesionalInexistenteDevuelve404() throws Exception {
        when(turnoService.crear(any(CreateAppointmentRequest.class), any(TokenService.AuthenticatedUser.class)))
            .thenThrow(new ResourceNotFoundException("Profesional no encontrado: " + PROFESIONAL_ID));

        mockMvc.perform(post("/api/appointments")
                .header("Authorization", tokenRecepcionista)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payloadValido())))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Profesional no encontrado: " + PROFESIONAL_ID));
    }

    @Test
    void postConSolapeDevuelve400ConMensajeDeNegocio() throws Exception {
        when(turnoService.crear(any(CreateAppointmentRequest.class), any(TokenService.AuthenticatedUser.class)))
            .thenThrow(new BusinessException("El profesional o el sillón ya tiene un turno en ese horario"));

        mockMvc.perform(post("/api/appointments")
                .header("Authorization", tokenRecepcionista)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payloadValido())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("El profesional o el sillón ya tiene un turno en ese horario"));
    }

    @Test
    void putReprogramaYDevuelve200() throws Exception {
        LocalDateTime nuevoInicio = INICIO.plusHours(5);
        AppointmentResponse reprogramado = new AppointmentResponse(TURNO_ID, PACIENTE_ID, PROFESIONAL_ID,
            SILLON_ID, PRESTACION_ID, nuevoInicio, nuevoInicio.plusMinutes(30),
            EstadoTurno.RESERVADO, "99999999", Instant.now());
        when(turnoService.reprogramar(eq(TURNO_ID), any(RescheduleAppointmentRequest.class),
            any(TokenService.AuthenticatedUser.class))).thenReturn(reprogramado);

        mockMvc.perform(put("/api/appointments/{id}", TURNO_ID)
                .header("Authorization", tokenRecepcionista)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fechaHoraInicio\":\"2099-11-25T14:00:00\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fechaHoraInicio").value("2099-11-25T14:00:00"))
            .andExpect(jsonPath("$.fechaHoraFin").value("2099-11-25T14:30:00"));
    }

    @Test
    void deleteCancelaYDevuelve200ConEstadoCancelado() throws Exception {
        when(turnoService.cancelar(eq(TURNO_ID), any(TokenService.AuthenticatedUser.class)))
            .thenReturn(respuesta(EstadoTurno.CANCELADO));

        mockMvc.perform(delete("/api/appointments/{id}", TURNO_ID)
                .header("Authorization", tokenRecepcionista))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("CANCELADO"));
    }

    @Test
    void getFiltraPorProfesionalFechaYEstado() throws Exception {
        when(turnoService.listar(any(AppointmentQuery.class), any(TokenService.AuthenticatedUser.class)))
            .thenReturn(List.of(respuesta(EstadoTurno.RESERVADO)));

        mockMvc.perform(get("/api/appointments")
                .header("Authorization", tokenRecepcionista)
                .param("profesionalId", PROFESIONAL_ID.toString())
                .param("fecha", "2099-11-25")
                .param("estado", "RESERVADO"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(TURNO_ID.toString()))
            .andExpect(jsonPath("$[0].estado").value("RESERVADO"));

        ArgumentCaptor<AppointmentQuery> captor = ArgumentCaptor.forClass(AppointmentQuery.class);
        verify(turnoService).listar(captor.capture(), any(TokenService.AuthenticatedUser.class));
        assertThat(captor.getValue().profesionalId()).isEqualTo(PROFESIONAL_ID);
        assertThat(captor.getValue().fecha()).isEqualTo(INICIO.toLocalDate());
        assertThat(captor.getValue().estado()).isEqualTo(EstadoTurno.RESERVADO);
    }

    @Test
    void postSinAutenticacionDevuelve403() throws Exception {
        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payloadValido())))
            .andExpect(status().isForbidden());
    }

    @Test
    void getSinAutenticacionDevuelve403() throws Exception {
        mockMvc.perform(get("/api/appointments"))
            .andExpect(status().isForbidden());
    }

    @Test
    void deleteSobreTurnoAjenoComoPacienteDevuelve403() throws Exception {
        when(turnoService.cancelar(eq(TURNO_ID), any(TokenService.AuthenticatedUser.class)))
            .thenThrow(new AccessDeniedException("El paciente solo puede operar sobre sus propios turnos"));

        mockMvc.perform(delete("/api/appointments/{id}", TURNO_ID)
                .header("Authorization", bearer(RolUsuario.PACIENTE)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("No tiene permisos para realizar esta operación"));
    }

    @Test
    @WithMockUser(roles = "INVITADO")
    void rolNoAutorizadoDevuelve403() throws Exception {
        mockMvc.perform(get("/api/appointments"))
            .andExpect(status().isForbidden());
    }

    private String bearer(RolUsuario rol) {
        return "Bearer " + tokenService.generateToken(UUID.randomUUID(), "99999999", rol);
    }

    private static Map<String, Object> payloadValido() {
        return Map.of(
            "pacienteId", PACIENTE_ID.toString(),
            "profesionalId", PROFESIONAL_ID.toString(),
            "sillonBoxId", SILLON_ID.toString(),
            "prestacionId", PRESTACION_ID.toString(),
            "fechaHoraInicio", "2099-11-25T09:00:00"
        );
    }

    private static AppointmentResponse respuesta(EstadoTurno estado) {
        return new AppointmentResponse(TURNO_ID, PACIENTE_ID, PROFESIONAL_ID, SILLON_ID, PRESTACION_ID,
            INICIO, FIN, estado, "99999999", Instant.now());
    }
}
