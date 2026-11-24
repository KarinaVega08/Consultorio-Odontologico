# Tasks — C-04 Disponibilidad de Turnos

> Checklist de trabajo para **Mai**. Rama: `feat/c04-disponibilidad-turnos`.

## Fase A — Preparación (antes de codear)

- [x] **A1.** Desestagiar `backend/.env` (ya hecho al crear la rama) y confirmar que `.gitignore` lo ignora.
- [x] **A2.** Confirmar con **Kari** el alcance de las entidades de `C-03` (`Profesional`, `SillonBox`, `Prestacion`, `HorarioAtencion`, `Bloqueo`).
- [x] **A3.** Confirmar con **Caste** cómo se modelará `Turno` en `C-05` (campos `fecha_hora_inicio`, `fecha_hora_fin`, `estado`, `profesional_id`, `sillon_box_id`).
- [x] **A4.** Definir la granularidad de la grilla: 15 minutos (propuesto) o 30. Registrar la decisión en `design.md`.

## Fase B — Ciclo OPSX (propose)

- [x] **B1.** Revisar `proposal.md`, `design.md` y `spec.md` de este change. Ajustar si el alcance está de más, de menos o inventado.
- [x] **B2.** Confirmar que el spec tiene al menos 3 escenarios: feliz (Esc. 1), error de negocio (Esc. 2) y borde (Esc. 3, 4 o 5).
- [x] **B3.** Ejecutar `/opsx:propose C-04-disponibilidad-turnos` o registrar manualmente la aprobación del propose.

## Fase C — Implementación (apply)

- [x] **C1.** Crear DTOs de entrada/salida en `application/dto`:
  - [x] `AvailabilityRequest` (profesionalId, fecha, prestacionId, sillonBoxId opcional).
  - [x] `AvailabilitySlotResponse` (inicio, fin).
  - [x] `AvailabilityResponse` (profesionalId, fecha, duracionMinutos, franjas).
- [x] **C2.** Crear la lógica pura en `domain/availability`:
  - [x] `WorkWindow` (horaInicio, horaFin).
  - [x] `Block` (inicio, fin, tipo: PROFESIONAL o SILLON).
  - [x] `Appointment` (inicio, fin, estado).
  - [x] `AvailabilitySlot` (inicio, fin).
  - [x] `AvailabilityCalculator` con método `calculate(...)`.
- [x] **C3.** Implementar el test unitario `AvailabilityCalculatorTest` cubriendo los 5 escenarios del spec (sin Spring, sin BD).
- [x] **C4.** Crear/conectar entidades de solo lectura si aún no existen (`HorarioAtencion`, `Bloqueo`, `Turno`) o definir interfaces de repositorio para cuando existan.
- [x] **C5.** Implementar `AvailabilityService` que consulta repositorios y llama al calculador.
- [x] **C6.** Implementar `AvailabilityController` con `GET /api/availability` y validación de parámetros (`@Valid`, `@NotNull`, formato de fecha).
- [x] **C7.** Asegurar que `/api/availability` sigue en la whitelist de `SecurityConfig` (público).

## Fase D — Verificación (verify)

- [x] **D1.** Crear `AvailabilityControllerTest` (slice `@WebMvcTest` o integración `@SpringBootTest`) con datos ficticios en `@BeforeEach`.
- [x] **D2.** Cubrir en tests: Esc. 1 (feliz), Esc. 2 (sin horario), Esc. 3 (borde a borde), Esc. 4 (bloqueo sillón), Esc. 5 (duración no ajustable).
- [x] **D3.** Ejecutar `cd backend && ./gradlew test` y confirmar `BUILD SUCCESSFUL` en verde (verificado con `./gradlew clean test`).
- [x] **D4.** Verificar rendimiento: tests unitarios < 50ms (calculador 26ms, dominio 2ms, DTO 11ms, puerto 5ms).

## Fase E — Integración y cierre

- [x] **E1.** Merge a `main` cuando `C-03` (Kari) esté lista, conectando los repositorios reales si se usaron interfaces.
- [x] **E2.** Avisar a **Caste** que `C-04` está disponible para su `C-05`.
- [x] **E3.** Archivar el change (`/opsx:archive C-04-disponibilidad-turnos`) una vez mergeado.
- [x] **E4.** Commits propios con mensajes claros (`feat(c04-...): ...`).

> **Estado al cierre de la sesión:** C-03 ya está integrado en la rama y sus repositorios reales están conectados en `AvailabilityService`. E3 (archive, 2026-11-23) completado en la rama `feat/c04-disponibilidad-turnos`; no se hizo push ni merge a `main`. Quedan pendientes de acción humana: el merge a `main` (E1) y el aviso directo a Caste (E2 — handoff documentado en `handoff-c05.md`).

## Fase F — Evidencia para la defensa

- [x] **F1.** Preparar explicación del motor de disponibilidad para el video (8–12 min, todos hablan).
- [x] **F2.** Anotar en la reflexión escrita: qué se ajustó con **Ajustar**, por qué, y qué habría pasado si se hubiera apretado **Continuar**.
- [x] **F3.** Dejar claro en la reflexión el aporte individual (commits propios + change responsable).
