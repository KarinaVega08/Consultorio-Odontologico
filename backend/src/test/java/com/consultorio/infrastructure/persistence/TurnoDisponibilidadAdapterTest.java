package com.consultorio.infrastructure.persistence;

import com.consultorio.domain.availability.Appointment;
import com.consultorio.domain.model.EstadoTurno;
import com.consultorio.domain.model.Turno;
import com.consultorio.domain.repository.TurnoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Adaptador de C-05 para el puerto de disponibilidad de C-04 (datos ficticios).
 */
@ExtendWith(MockitoExtension.class)
class TurnoDisponibilidadAdapterTest {

    private static final LocalDate FECHA = LocalDate.of(2099, 11, 25);
    private static final UUID PROFESIONAL_ID = UUID.randomUUID();
    private static final UUID SILLON_ID = UUID.randomUUID();

    @Mock private TurnoRepository turnoRepository;
    @InjectMocks private TurnoDisponibilidadAdapter adapter;

    @Test
    void devuelveIntervalosDeLosTurnosDelDiaIncluyendoCancelados() {
        when(turnoRepository.buscarOcupados(any(), any(), any(), any()))
            .thenReturn(List.of(
                turno(FECHA.atTime(9, 0), FECHA.atTime(9, 30), EstadoTurno.RESERVADO),
                turno(FECHA.atTime(12, 0), FECHA.atTime(12, 30), EstadoTurno.CANCELADO)));

        List<Appointment> ocupados = adapter.findTurnosOcupados(PROFESIONAL_ID, FECHA, null);

        assertThat(ocupados).containsExactly(
            new Appointment(FECHA.atTime(9, 0), FECHA.atTime(9, 30), EstadoTurno.RESERVADO),
            new Appointment(FECHA.atTime(12, 0), FECHA.atTime(12, 30), EstadoTurno.CANCELADO));

        ArgumentCaptor<LocalDateTime> desde = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> hasta = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(turnoRepository).buscarOcupados(eq(PROFESIONAL_ID), desde.capture(), hasta.capture(), isNull());
        assertThat(desde.getValue()).isEqualTo(FECHA.atStartOfDay());
        assertThat(hasta.getValue()).isEqualTo(FECHA.atTime(LocalTime.MAX));
    }

    @Test
    void propagaElFiltroDeSillonCuandoSeIndica() {
        when(turnoRepository.buscarOcupados(any(), any(), any(), eq(SILLON_ID))).thenReturn(List.of());

        assertThat(adapter.findTurnosOcupados(PROFESIONAL_ID, FECHA, SILLON_ID)).isEmpty();

        verify(turnoRepository).buscarOcupados(eq(PROFESIONAL_ID), any(), any(), eq(SILLON_ID));
    }

    private static Turno turno(LocalDateTime inicio, LocalDateTime fin, EstadoTurno estado) {
        Turno turno = new Turno();
        turno.setFechaHoraInicio(inicio);
        turno.setFechaHoraFin(fin);
        turno.setEstado(estado);
        return turno;
    }
}
