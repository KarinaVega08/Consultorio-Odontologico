package com.consultorio.notification.api.controller;

import com.consultorio.domain.exception.BusinessException;
import com.consultorio.infrastructure.config.AdminSecurityConfig;
import com.consultorio.infrastructure.config.NotificationSecurityConfig;
import com.consultorio.infrastructure.config.SecurityConfig;
import com.consultorio.notification.application.dto.NotificationSettingsRequest;
import com.consultorio.notification.application.dto.NotificationSettingsResponse;
import com.consultorio.notification.application.service.NotificacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({NotificationLinkController.class, AdminNotificationSettingsController.class})
@Import({SecurityConfig.class, AdminSecurityConfig.class, NotificationSecurityConfig.class})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificacionService service;

    @Test
    void confirmarAsistenciaSinAutenticacionDevuelve200() throws Exception {
        when(service.confirmarAsistencia(anyString())).thenReturn("Asistencia confirmada");

        mockMvc.perform(get("/api/notifications/confirm").param("token", "token-ficticio"))
            .andExpect(status().isOk())
            .andExpect(content().string("Asistencia confirmada"));

        verify(service).confirmarAsistencia("token-ficticio");
    }

    @Test
    void cancelarConTokenInvalidoDevuelve400() throws Exception {
        doThrow(new BusinessException("El enlace no es válido"))
            .when(service).cancelarTurnoConToken(anyString());

        mockMvc.perform(get("/api/notifications/cancel").param("token", "invalido"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void administradorPuedeCambiarAntelacion() throws Exception {
        when(service.actualizarHorasAntelacion(anyInt())).thenReturn(48);

        mockMvc.perform(put("/api/admin/notification-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horasAntelacion\":48}"))
            .andExpect(status().isOk());

        verify(service).actualizarHorasAntelacion(48);
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    void pacienteNoPuedeCambiarAntelacion() throws Exception {
        mockMvc.perform(put("/api/admin/notification-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horasAntelacion\":48}"))
            .andExpect(status().isForbidden());
    }
}
