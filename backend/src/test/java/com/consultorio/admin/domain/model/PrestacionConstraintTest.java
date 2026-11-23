package com.consultorio.admin.domain.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PrestacionConstraintTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void duracionMinutosCeroViolaConstraintMinUno() {
        Prestacion prestacion = new Prestacion();
        prestacion.setNombre("Inválida");
        prestacion.setDuracionMinutos(0);

        Set<ConstraintViolation<Prestacion>> violaciones = validator.validate(prestacion);
        assertThat(violaciones)
            .extracting(ConstraintViolation::getMessage)
            .contains("La duración de la prestación debe ser mayor a 0 minutos");
    }

    @Test
    void duracionMinutosNegativoViolaConstraintMinUno() {
        Prestacion prestacion = new Prestacion();
        prestacion.setNombre("Inválida");
        prestacion.setDuracionMinutos(-5);

        Set<ConstraintViolation<Prestacion>> violaciones = validator.validate(prestacion);
        assertThat(violaciones)
            .extracting(ConstraintViolation::getMessage)
            .contains("La duración de la prestación debe ser mayor a 0 minutos");
    }

    @Test
    void duracionMinutosUnoEsValida() {
        Prestacion prestacion = new Prestacion();
        prestacion.setNombre("Válida");
        prestacion.setDuracionMinutos(1);

        Set<ConstraintViolation<Prestacion>> violaciones = validator.validate(prestacion);
        assertThat(violaciones).isEmpty();
    }
}
