package com.consultorio.domain.availability;

import com.consultorio.domain.model.EstadoTurno;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Motor puro de cálculo de franjas libres. Sin dependencias de Spring ni JPA;
 * reutilizable por C-05 para validar reservas.
 */
public class AvailabilityCalculator {

    /**
     * Calcula las franjas libres para una fecha, considerando ventanas laborales,
     * bloqueos y turnos existentes. Dos intervalos se solapan solo si
     * {@code inicioA < finB && inicioB < finA}: el borde a borde está permitido.
     *
     * <p>El paso entre candidatas es {@code max(gridMinutes, durationMinutes)} para
     * no ofrecer inicios que se solapen entre sí (Esc. 1 del spec: 30 min → 09:00, 09:30, ...).</p>
     */
    public List<AvailabilitySlot> calculate(
            LocalDate fecha,
            List<WorkWindow> workWindows,
            List<Block> blocks,
            List<Appointment> appointments,
            int durationMinutes,
            int gridMinutes) {

        if (durationMinutes <= 0 || gridMinutes <= 0) {
            return List.of();
        }

        int paso = Math.max(gridMinutes, durationMinutes);
        List<AvailabilitySlot> franjas = new ArrayList<>();
        List<Block> bloqueos = blocks == null ? List.of() : blocks;
        List<Appointment> turnos = appointments == null ? List.of() : appointments;

        for (WorkWindow ventana : workWindows) {
            LocalDateTime inicioVentana = fecha.atTime(ventana.horaInicio());
            LocalDateTime ultimoInicio = fecha.atTime(ventana.horaFin()).minusMinutes(durationMinutes);

            for (LocalDateTime candidata = inicioVentana;
                 !candidata.isAfter(ultimoInicio);
                 candidata = candidata.plusMinutes(paso)) {

                LocalDateTime fin = candidata.plusMinutes(durationMinutes);
                if (estaLibre(candidata, fin, bloqueos, turnos)) {
                    franjas.add(new AvailabilitySlot(candidata, fin));
                }
            }
        }

        franjas.sort(Comparator.comparing(AvailabilitySlot::inicio));
        return franjas;
    }

    private boolean estaLibre(LocalDateTime inicio,
                              LocalDateTime fin,
                              List<Block> bloqueos,
                              List<Appointment> turnos) {
        for (Block bloqueo : bloqueos) {
            if (solapan(inicio, fin, bloqueo.inicio(), bloqueo.fin())) {
                return false;
            }
        }
        for (Appointment turno : turnos) {
            if (turno.estado() == EstadoTurno.CANCELADO) {
                continue;
            }
            if (solapan(inicio, fin, turno.inicio(), turno.fin())) {
                return false;
            }
        }
        return true;
    }

    private boolean solapan(LocalDateTime inicioA, LocalDateTime finA,
                            LocalDateTime inicioB, LocalDateTime finB) {
        return inicioA.isBefore(finB) && inicioB.isBefore(finA);
    }
}
