package com.consultorio.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regla estricta de solapamiento: borde a borde permitido, sin Spring (< 50 ms).
 */
class TurnoSolapamientoTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 11, 25, 9, 0);

    @Test
    void finIgualAInicioNoEsSolapamiento() {
        assertThat(TurnoSolapamiento.seSolapan(BASE, BASE.plusMinutes(30),
            BASE.plusMinutes(30), BASE.plusMinutes(60))).isFalse();
        assertThat(TurnoSolapamiento.seSolapan(BASE.plusMinutes(30), BASE.plusMinutes(60),
            BASE, BASE.plusMinutes(30))).isFalse();
    }

    @Test
    void intervalosQueSeCruzanSiSonSolapamiento() {
        assertThat(TurnoSolapamiento.seSolapan(BASE, BASE.plusMinutes(60),
            BASE.plusMinutes(30), BASE.plusMinutes(90))).isTrue();
        assertThat(TurnoSolapamiento.seSolapan(BASE.plusMinutes(30), BASE.plusMinutes(90),
            BASE, BASE.plusMinutes(60))).isTrue();
    }

    @Test
    void intervalosDisjuntosNoSonSolapamiento() {
        assertThat(TurnoSolapamiento.seSolapan(BASE, BASE.plusMinutes(30),
            BASE.plusMinutes(45), BASE.plusMinutes(60))).isFalse();
    }
}
