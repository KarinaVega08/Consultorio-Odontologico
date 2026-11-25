# Handoff C-04 → C-05 (para Franco Castellarín)

C-04 dejó un puerto de solo lectura para que C-05 conecte sus turnos reales sin acoplar este change a la entidad `Turno`.

## Contrato a implementar

```java
package com.consultorio.domain.availability;

public interface TurnoDisponibilidadPort {
    List<Appointment> findTurnosOcupados(UUID profesionalId, LocalDate fecha, UUID sillonBoxId);
}

public record Appointment(LocalDateTime inicio, LocalDateTime fin, EstadoTurno estado) {}
```

- `EstadoTurno` ya existe en `com.consultorio.domain.model` (`RESERVADO`, `CONFIRMADO`, `CANCELADO`, `COMPLETADO`).
- `CANCELADO` **no** ocupa la agenda: el calculador lo ignora.
- Devuelve los turnos del profesional para esa fecha; si `sillonBoxId` no es null, además se filtra por sillón/box.
- Solapamiento estricto: `inicioA < finB && inicioB < finA`; borde a borde permitido.

## Regla de negocio compartida

El motor `AvailabilityCalculator.calculate(...)` es la fuente única de verdad para el cálculo de franjas. C-05 puede reutilizarlo para validar que una reserva propuesta encaje. El paso entre candidatas es `max(gridMinutes, durationMinutes)`, con `availability.grid-minutes` (default 15).

## Fallback actual

`AvailabilityIntegrationConfig` registra `NoOpTurnoDisponibilidadPort` con `@ConditionalOnMissingBean(TurnoDisponibilidadPort.class)`. En cuanto C-05 aporte un bean real, el fallback se retrae automáticamente: no hay que tocar C-04.

## Datos de prueba

Todos los tests usan datos ficticios (Ley 25.326): "Dr. Demo Ficticio", "MP-99999", "Sillón 1", DNI reales **no** se usan en este change.
