package com.consultorio.domain.availability;

import com.consultorio.domain.model.EstadoTurno;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvailabilityCalculatorTest {

    private final AvailabilityCalculator calculator = new AvailabilityCalculator();
    private final LocalDate fecha = LocalDate.of(2026, 11, 25);

    @Test
    void escenario1_franjasLibresEnHorarioLaboral() {
        List<AvailabilitySlot> franjas = calculator.calculate(
            fecha, ventanas(LocalTime.of(9, 0), LocalTime.of(12, 0)), List.of(), List.of(), 30, 15);

        assertThat(horas(franjas)).containsExactly(
            LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0),
            LocalTime.of(10, 30), LocalTime.of(11, 0), LocalTime.of(11, 30));
        assertThat(franjas).allSatisfy(franja ->
            assertThat(franja.fin()).isEqualTo(franja.inicio().plusMinutes(30)));
    }

    @Test
    void escenario2_sinHorarioDeAtencionDevuelveListaVacia() {
        List<AvailabilitySlot> franjas = calculator.calculate(fecha, List.of(), List.of(), List.of(), 30, 15);

        assertThat(franjas).isEmpty();
    }

    @Test
    void escenario3_turnoExistentePermiteFranjaBordeABorde() {
        Appointment turno = new Appointment(
            LocalDateTime.of(2026, 11, 25, 9, 30),
            LocalDateTime.of(2026, 11, 25, 10, 0),
            EstadoTurno.CONFIRMADO);

        List<AvailabilitySlot> franjas = calculator.calculate(
            fecha, ventanas(LocalTime.of(9, 0), LocalTime.of(12, 0)), List.of(), List.of(turno), 30, 15);

        assertThat(horas(franjas)).containsExactly(
            LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(10, 30),
            LocalTime.of(11, 0), LocalTime.of(11, 30));
    }

    @Test
    void escenario4_bloqueoDeSillonExcluyeSoloLasFranjasSolapadas() {
        Block bloqueo = new Block(
            LocalDateTime.of(2026, 11, 25, 10, 0),
            LocalDateTime.of(2026, 11, 25, 11, 0),
            TipoBloqueo.SILLON);

        List<AvailabilitySlot> franjas = calculator.calculate(
            fecha, ventanas(LocalTime.of(9, 0), LocalTime.of(12, 0)), List.of(bloqueo), List.of(), 30, 15);

        assertThat(horas(franjas)).containsExactly(
            LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(11, 0), LocalTime.of(11, 30));
    }

    @Test
    void escenario5_duracionNoCabeEnLaVentana() {
        List<AvailabilitySlot> franjas = calculator.calculate(
            fecha, ventanas(LocalTime.of(9, 0), LocalTime.of(9, 20)), List.of(), List.of(), 30, 15);

        assertThat(franjas).isEmpty();
    }

    @Test
    void turnoCanceladoLiberaElHueco() {
        Appointment cancelado = new Appointment(
            LocalDateTime.of(2026, 11, 25, 9, 30),
            LocalDateTime.of(2026, 11, 25, 10, 0),
            EstadoTurno.CANCELADO);

        List<AvailabilitySlot> franjas = calculator.calculate(
            fecha, ventanas(LocalTime.of(9, 0), LocalTime.of(12, 0)), List.of(), List.of(cancelado), 30, 15);

        assertThat(horas(franjas)).containsExactly(
            LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0),
            LocalTime.of(10, 30), LocalTime.of(11, 0), LocalTime.of(11, 30));
    }

    @Test
    void bloqueoDeProfesionalPermiteLaFranjaQueEmpiezaAlTerminar() {
        Block bloqueo = new Block(
            LocalDateTime.of(2026, 11, 25, 10, 0),
            LocalDateTime.of(2026, 11, 25, 10, 30),
            TipoBloqueo.PROFESIONAL);

        List<AvailabilitySlot> franjas = calculator.calculate(
            fecha, ventanas(LocalTime.of(9, 0), LocalTime.of(12, 0)), List.of(bloqueo), List.of(), 30, 15);

        assertThat(horas(franjas)).doesNotContain(LocalTime.of(10, 0));
        assertThat(horas(franjas)).contains(LocalTime.of(10, 30));
    }

    @Test
    void grillaConfiguradaA30MinutosGeneraLasMismasFranjas() {
        List<AvailabilitySlot> franjas = calculator.calculate(
            fecha, ventanas(LocalTime.of(9, 0), LocalTime.of(12, 0)), List.of(), List.of(), 30, 30);

        assertThat(horas(franjas)).containsExactly(
            LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0),
            LocalTime.of(10, 30), LocalTime.of(11, 0), LocalTime.of(11, 30));
    }

    @Test
    void multiplesVentanasSeOrdenanPorHoraDeInicio() {
        List<WorkWindow> ventanas = List.of(
            new WorkWindow(LocalTime.of(11, 0), LocalTime.of(12, 0)),
            new WorkWindow(LocalTime.of(9, 0), LocalTime.of(10, 0)));

        List<AvailabilitySlot> franjas = calculator.calculate(fecha, ventanas, List.of(), List.of(), 30, 15);

        assertThat(horas(franjas)).containsExactly(
            LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(11, 0), LocalTime.of(11, 30));
    }

    private List<WorkWindow> ventanas(LocalTime inicio, LocalTime fin) {
        return List.of(new WorkWindow(inicio, fin));
    }

    private List<LocalTime> horas(List<AvailabilitySlot> franjas) {
        return franjas.stream().map(franja -> franja.inicio().toLocalTime()).toList();
    }
}
