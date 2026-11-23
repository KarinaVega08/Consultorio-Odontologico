# Tasks — C-04 Disponibilidad de Turnos

> Checklist de trabajo para **Mai**. Rama: `feat/c04-disponibilidad-turnos`.

## Fase A — Preparación (antes de codear)

- [ ] **A1.** Desestagiar `backend/.env` (ya hecho al crear la rama) y confirmar que `.gitignore` lo ignora.
- [ ] **A2.** Confirmar con **Kari** el alcance de las entidades de `C-03` (`Profesional`, `SillonBox`, `Prestacion`, `HorarioAtencion`, `Bloqueo`).
- [ ] **A3.** Confirmar con **Caste** cómo se modelará `Turno` en `C-05` (campos `fecha_hora_inicio`, `fecha_hora_fin`, `estado`, `profesional_id`, `sillon_box_id`).
- [ ] **A4.** Definir la granularidad de la grilla: 15 minutos (propuesto) o 30. Registrar la decisión en `design.md`.

## Fase B — Ciclo OPSX (propose)

- [ ] **B1.** Revisar `proposal.md`, `design.md` y `spec.md` de este change. Ajustar si el alcance está de más, de menos o inventado.
- [ ] **B2.** Confirmar que el spec tiene al menos 3 escenarios: feliz (Esc. 1), error de negocio (Esc. 2) y borde (Esc. 3, 4 o 5).
- [ ] **B3.** Ejecutar `/opsx:propose C-04-disponibilidad-turnos` o registrar manualmente la aprobación del propose.

## Fase C — Implementación (apply)

- [ ] **C1.** Crear DTOs de entrada/salida en `application/dto`:
  - [ ] `AvailabilityRequest` (profesionalId, fecha, prestacionId, sillonBoxId opcional).
  - [ ] `AvailabilitySlotResponse` (inicio, fin).
  - [ ] `AvailabilityResponse` (profesionalId, fecha, duracionMinutos, franjas).
- [ ] **C2.** Crear la lógica pura en `domain/availability`:
  - [ ] `WorkWindow` (horaInicio, horaFin).
  - [ ] `Block` (inicio, fin, tipo: PROFESIONAL o SILLON).
  - [ ] `Appointment` (inicio, fin, estado).
  - [ ] `AvailabilitySlot` (inicio, fin).
  - [ ] `AvailabilityCalculator` con método `calculate(...)`.
- [ ] **C3.** Implementar el test unitario `AvailabilityCalculatorTest` cubriendo los 5 escenarios del spec (sin Spring, sin BD).
- [ ] **C4.** Crear/conectar entidades de solo lectura si aún no existen (`HorarioAtencion`, `Bloqueo`, `Turno`) o definir interfaces de repositorio para cuando existan.
- [ ] **C5.** Implementar `AvailabilityService` que consulta repositorios y llama al calculador.
- [ ] **C6.** Implementar `AvailabilityController` con `GET /api/availability` y validación de parámetros (`@Valid`, `@NotNull`, formato de fecha).
- [ ] **C7.** Asegurar que `/api/availability` sigue en la whitelist de `SecurityConfig` (público).

## Fase D — Verificación (verify)

- [ ] **D1.** Crear `AvailabilityControllerTest` (slice `@WebMvcTest` o integración `@SpringBootTest`) con datos ficticios en `@BeforeEach`.
- [ ] **D2.** Cubrir en tests: Esc. 1 (feliz), Esc. 2 (sin horario), Esc. 3 (borde a borde), Esc. 4 (bloqueo sillón), Esc. 5 (duración no ajustable).
- [ ] **D3.** Ejecutar `cd backend && ./gradlew test` y confirmar `BUILD SUCCESSFUL` en verde.
- [ ] **D4.** Verificar rendimiento: tests unitarios < 50ms.

## Fase E — Integración y cierre

- [ ] **E1.** Merge a `main` cuando `C-03` (Kari) esté lista, conectando los repositorios reales si se usaron interfaces.
- [ ] **E2.** Avisar a **Caste** que `C-04` está disponible para su `C-05`.
- [ ] **E3.** Archivar el change (`/opsx:archive C-04-disponibilidad-turnos`) una vez mergeado.
- [ ] **E4.** Commits propios con mensajes claros (`feat(c04-...): ...`).

## Fase F — Evidencia para la defensa

- [ ] **F1.** Preparar explicación del motor de disponibilidad para el video (8–12 min, todos hablan).
- [ ] **F2.** Anotar en la reflexión escrita: qué se ajustó con **Ajustar**, por qué, y qué habría pasado si se hubiera apretado **Continuar**.
- [ ] **F3.** Dejar claro en la reflexión el aporte individual (commits propios + change responsable).
