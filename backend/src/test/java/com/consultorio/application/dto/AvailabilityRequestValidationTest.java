package com.consultorio.application.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.format.annotation.DateTimeFormat;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AvailabilityRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeFactory() {
        factory.close();
    }

    private final UUID profesionalId = UUID.randomUUID();
    private final UUID prestacionId = UUID.randomUUID();
    private final LocalDate fecha = LocalDate.of(2026, 11, 25);

    @Test
    void profesionalIdEsObligatorio() {
        AvailabilityRequest request = new AvailabilityRequest(null, fecha, prestacionId, null);

        assertThat(camposConError(request)).contains("profesionalId");
    }

    @Test
    void fechaYPrestacionSonObligatorios() {
        AvailabilityRequest request = new AvailabilityRequest(profesionalId, null, null, null);

        assertThat(camposConError(request)).contains("fecha", "prestacionId");
    }

    @Test
    void sillonBoxEsOpcional() {
        AvailabilityRequest request = new AvailabilityRequest(profesionalId, fecha, prestacionId, null);

        assertThat(camposConError(request)).isEmpty();
    }

    @Test
    void fechaDeclaraFormatoIso() throws NoSuchFieldException {
        Field campo = AvailabilityRequest.class.getDeclaredField("fecha");
        DateTimeFormat formato = campo.getAnnotation(DateTimeFormat.class);

        assertThat(formato).isNotNull();
        assertThat(formato.iso()).isEqualTo(DateTimeFormat.ISO.DATE);
    }

    @Test
    void respuestaExponeMetadatosYFranjasEnOrden() {
        AvailabilitySlotResponse primera = new AvailabilitySlotResponse(
            LocalDateTime.of(2026, 11, 25, 9, 0), LocalDateTime.of(2026, 11, 25, 9, 30));
        AvailabilitySlotResponse segunda = new AvailabilitySlotResponse(
            LocalDateTime.of(2026, 11, 25, 9, 30), LocalDateTime.of(2026, 11, 25, 10, 0));

        AvailabilityResponse response = new AvailabilityResponse(
            profesionalId, fecha, 30, List.of(primera, segunda));

        assertThat(response.profesionalId()).isEqualTo(profesionalId);
        assertThat(response.fecha()).isEqualTo(fecha);
        assertThat(response.duracionMinutos()).isEqualTo(30);
        assertThat(response.franjas()).extracting(AvailabilitySlotResponse::inicio)
            .containsExactly(LocalDateTime.of(2026, 11, 25, 9, 0), LocalDateTime.of(2026, 11, 25, 9, 30));
    }

    @Test
    void requestConSillonBoxEsValido() {
        AvailabilityRequest request = new AvailabilityRequest(
            profesionalId, fecha, prestacionId, UUID.randomUUID());

        assertThat(camposConError(request)).isEmpty();
    }

    @Test
    void respuestaSinDisponibilidadUsaListaVacia() {
        AvailabilityResponse response = new AvailabilityResponse(profesionalId, fecha, 30, List.of());

        assertThat(response.franjas()).isEmpty();
        assertThat(response.franjas()).isNotNull();
    }

    private Set<String> camposConError(AvailabilityRequest request) {
        Set<ConstraintViolation<AvailabilityRequest>> violaciones = validator.validate(request);
        return violaciones.stream()
            .map(violacion -> violacion.getPropertyPath().toString())
            .collect(java.util.stream.Collectors.toSet());
    }
}
