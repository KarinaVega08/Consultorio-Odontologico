package com.consultorio.domain.availability;

import com.consultorio.domain.model.EstadoTurno;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class AvailabilityDomainTest {

    @Test
    void workWindowExponeLimites() {
        WorkWindow ventana = new WorkWindow(LocalTime.of(9, 0), LocalTime.of(12, 0));

        assertThat(ventana.horaInicio()).isEqualTo(LocalTime.of(9, 0));
        assertThat(ventana.horaFin()).isEqualTo(LocalTime.of(12, 0));
    }

    @Test
    void blockConservaIntervaloYTipo() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 25, 10, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 11, 25, 11, 0);

        Block bloqueo = new Block(inicio, fin, TipoBloqueo.SILLON);

        assertThat(bloqueo.inicio()).isEqualTo(inicio);
        assertThat(bloqueo.fin()).isEqualTo(fin);
        assertThat(bloqueo.tipo()).isEqualTo(TipoBloqueo.SILLON);
    }

    @Test
    void appointmentConservaIntervaloYEstado() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 25, 9, 30);
        LocalDateTime fin = LocalDateTime.of(2026, 11, 25, 10, 0);

        Appointment turno = new Appointment(inicio, fin, EstadoTurno.CONFIRMADO);

        assertThat(turno.inicio()).isEqualTo(inicio);
        assertThat(turno.fin()).isEqualTo(fin);
        assertThat(turno.estado()).isEqualTo(EstadoTurno.CONFIRMADO);
    }

    @Test
    void availabilitySlotEsUnValorInmutable() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 25, 9, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 11, 25, 9, 30);

        AvailabilitySlot slot = new AvailabilitySlot(inicio, fin);

        assertThat(slot).isEqualTo(new AvailabilitySlot(inicio, fin));
        assertThat(slot.inicio()).isEqualTo(inicio);
        assertThat(slot.fin()).isEqualTo(fin);
    }
}
