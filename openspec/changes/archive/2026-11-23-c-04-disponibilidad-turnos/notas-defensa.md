# Notas de defensa — C-04 Disponibilidad de Turnos

## Guion para el video (8–12 min)

1. **Problema (1–2 min):** sin motor de disponibilidad, el paciente llama al consultorio y recepción revisa la agenda a mano; además C-05 no tendría una fuente única de verdad.
2. **Arquitectura (2 min):** separación entre cálculo puro (`domain.availability`) y adaptadores. El calculador no conoce Spring ni JPA: recibe DTOs inmutables y devuelve franjas.
3. **Algoritmo (3 min):** ventanas laborales por `dia_semana` → candidatas cada `max(grid, duración)` → descartar si solapan con bloqueos o turnos → ordenar. Regla de solapamiento `inicioA < finB && inicioB < finA` (borde permitido).
4. **Integración (2 min):** `AvailabilityService` lee entidades de C-03 (solo lectura) y el puerto `TurnoDisponibilidadPort` que implementará C-05. Endpoint público `GET /api/availability`.
5. **Tests (2 min):** 9 tests unitarios del calculador (5 escenarios del spec + 4 de triangulación), 8 tests de integración sobre H2 con datos ficticios, suite completa en verde.

## Reflexión: qué se ajustó y por qué

- **Delta spec creado:** el CLI de OpenSpec no reconocía el `spec.md` suelto; se generó `specs/c-04-disponibilidad-turnos/spec.md` para que `openspec validate` pase y el archive no se bloquee.
- **Diagrama de paquetes corregido:** `design.md` ubicaba las entidades en `com.consultorio.domain.*`, pero C-03 las dejó en `com.consultorio.admin.*`. Se ajustó para no duplicarlas.
- **`Turno` desacoplado:** C-05 aún no existe. En vez de crear una entidad stub que chocaría al mergear, se definió un puerto de solo lectura (mismo patrón que C-06).
- **Paso efectivo de la grilla:** con grilla de 15 y prestación de 30, generar candidatas cada 15 daría franjas solapadas (09:00 y 09:15 no pueden coexistir). Se ajustó a `max(grid, duración)`, que además reproduce exactamente el Escenario 1 del spec (09:00, 09:30, ...).
- **Si se hubiera apretado "Continuar"** sin revisar: se habría seguido el diseño original y el Escenario 1 habría fallado (franjas cada 15 min), el validate habría quedado roto por el delta spec ausente y la entidad `Turno` stub habría colisionado con C-05.

## Aporte individual

- Change responsable: **C-04 disponibilidad-turnos** (Mai), rama `feat/c04-disponibilidad-turnos`.
- Commits propios con prefijo `feat(c04-...)`; sin `git push`.
- Entregable reutilizable por C-05: `AvailabilityCalculator` + `TurnoDisponibilidadPort` (ver `handoff-c05.md`).
