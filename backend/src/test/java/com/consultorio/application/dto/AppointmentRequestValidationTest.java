package com.consultorio.application.dto;

import com.consultorio.infrastructure.config.GlobalExceptionHandler;
import jakarta.validation.Valid;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Validación de los DTOs de turnos a través de MockMvc y el GlobalExceptionHandler.
 */
@WebMvcTest(AppointmentRequestValidationTest.ValidationController.class)
@Import({GlobalExceptionHandler.class, AppointmentRequestValidationTest.ValidationController.class})
@AutoConfigureMockMvc(addFilters = false)
class AppointmentRequestValidationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void payloadInvalidoDevuelve400ConFieldErrors() throws Exception {
        String json = """
            {
              "pacienteId": "11111111-1111-1111-1111-111111111111",
              "profesionalId": null,
              "sillonBoxId": "33333333-3333-3333-3333-333333333333",
              "prestacionId": null,
              "fechaHoraInicio": "2020-01-01T09:00:00"
            }
            """;

        mockMvc.perform(post("/api/test/appointments-validation")
                .contentType(APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.profesionalId").exists())
            .andExpect(jsonPath("$.fieldErrors.prestacionId").exists())
            .andExpect(jsonPath("$.fieldErrors.fechaHoraInicio").exists());
    }

    @Test
    void payloadValidoPasaLaValidacion() throws Exception {
        String json = """
            {
              "pacienteId": "11111111-1111-1111-1111-111111111111",
              "profesionalId": "22222222-2222-2222-2222-222222222222",
              "sillonBoxId": "33333333-3333-3333-3333-333333333333",
              "prestacionId": "44444444-4444-4444-4444-444444444444",
              "fechaHoraInicio": "2099-11-25T09:00:00"
            }
            """;

        mockMvc.perform(post("/api/test/appointments-validation")
                .contentType(APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk());
    }

    @RestController
    static class ValidationController {

        @PostMapping("/api/test/appointments-validation")
        ResponseEntity<Void> validar(@Valid @RequestBody CreateAppointmentRequest request) {
            return ResponseEntity.ok().build();
        }
    }
}
