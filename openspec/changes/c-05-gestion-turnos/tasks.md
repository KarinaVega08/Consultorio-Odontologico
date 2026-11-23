# Tasks — C-05 gestion-turnos

> Checklist de trabajo para **Caste**. Rama: `feat/c05-gestion-turnos`.
> Referencias: `specs/gestion-turnos/spec.md` (qué) y `design.md` (cómo).

## 1. Coordinación y preparación (antes de codear)

- [ ] 1.1 Responderle a **Mai** (su tarea A3) con el modelo de `Turno` del design D1/D3 (campos, ubicación `domain.model.Turno`, regla estricta) — verificación: respuesta enviada en el chat del equipo y confirmación de Mai.
- [ ] 1.2 Avisar a **Thiago** que el apply requiere el merge de `C-02` (`Paciente`, `AuthenticatedUser`, filtro Bearer) — verificación: `git fetch origin && git log origin/main` muestra el merge de `feat/c02-registro-pacientes`.
- [ ] 1.3 Confirmar en verde la base antes de empezar — verificación: `./gradlew test` en `backend/` → `BUILD SUCCESSFUL` (70 tests actuales).

## 2. Ciclo OPSX — Explore y Propose

- [x] 2.1 Explore: relevar estado del código, KB y specs relacionadas en `explore.md` — verificación: archivo presente con brechas detectadas.
- [x] 2.2 Propose: `proposal.md` con Why, What Changes y capacidad `gestion-turnos` — verificación: `openspec status --change c-05-gestion-turnos` marca `proposal` done.
- [x] 2.3 Spec: `specs/gestion-turnos/spec.md` con ≥3 escenarios (feliz, error de negocio, borde) — verificación: `openspec validate --change c-05-gestion-turnos` sin errores.
- [x] 2.4 Design: `design.md` con decisiones D1-D6 (IN-04 resuelto, integración C-06) — verificación: archivo presente con todas las secciones.
- [x] 2.5 Tasks: este archivo — verificación: `openspec status --change c-05-gestion-turnos` → todos los artefactos `done`.
- [ ] 2.6 Subir la rama y abrir PR a `main` para revisión de Thiago (solo artefactos de planificación, sin código) — verificación: PR abierto en GitHub.

## 3. Modelo de datos (apply)

- [ ] 3.1 Crear entidad `Turno` (hereda `BaseEntity`: UUID, auditoría) con FKs a `Paciente`, `Profesional`, `SillonBox`, `Prestacion`, `fechaHoraInicio`, `fechaHoraFin`, `estado` (`EstadoTurno`) y `creadoPor` — verificación: test `@DataJpaTest` que persiste un turno y lo recupera con `fechaCreacion`.
- [ ] 3.2 Crear `TurnoRepository` con consulta de turnos solapados por profesional y por sillón (excluyendo `CANCELADO`, excluyendo el propio id) y lock pesimista `PESSIMISTIC_WRITE` sobre profesional/sillón — verificación: test del repositorio que encuentra el solape y no encuentra turnos cancelados.
- [ ] 3.3 Crear DTOs con Bean Validation (`CreateAppointmentRequest`, `RescheduleAppointmentRequest`, `AppointmentResponse`, `AppointmentQuery`) — verificación: test MockMvc de payload inválido → `400` con `fieldErrors`.

## 4. Servicio anti-solapamiento (apply)

- [ ] 4.1 `TurnoService.crear`: calcular `fechaHoraFin` = inicio + duración de la prestación (nunca recibida del cliente) y validar RN-TU-03 (horario laboral del día y bloqueos) — verificación: tests de los escenarios "Creación exitosa", "Horario fuera de la atención" y "Choque con bloqueo" en verde.
- [ ] 4.2 Validar anti-solapamiento por profesional y sillón con la regla estricta `inicioA < finB && inicioB < finA` dentro de `@Transactional` con los locks del 3.2 — verificación: tests "Solapamiento por profesional" y "Solapamiento por sillón" (400, no persiste) en verde.
- [ ] 4.3 Test unitario de la regla de borde: `finA == inicioB` no se solapa — verificación: test sin Spring (< 50 ms) en verde.
- [ ] 4.4 `TurnoService.reprogramar` (PUT): revalidar todo y liberar el hueco anterior — verificación: test "Reprogramación exitosa libera el hueco anterior" en verde.
- [ ] 4.5 `TurnoService.cancelar` (DELETE): estado `CANCELADO` sin borrado físico + rechazo si ya está cancelado — verificación: tests "Cancelación libera el hueco" y "Cancelación de un turno ya cancelado" en verde.
- [ ] 4.6 `TurnoService.listar` con filtros opcionales de profesional, fecha y estado — verificación: test "Listado filtrado" en verde.

## 5. API y seguridad (apply)

- [ ] 5.1 `AppointmentController` con `POST`, `PUT`, `DELETE` y `GET /api/appointments` invocando al servicio y devolviendo DTOs — verificación: tests MockMvc de los escenarios del spec (201/200/400/404).
- [ ] 5.2 RBAC: autenticación obligatoria; paciente solo sobre turnos propios; recepción/odontólogo/administrador sobre cualquiera (`@PreAuthorize`) — verificación: tests "Acceso sin autenticación" (401/403) y "Paciente ajeno" (403) en verde.

## 6. Integración con C-06 (apply)

- [ ] 6.1 Mapper a `TurnoNotificable` y publicación de `TurnoCreadoEvent`, `TurnoReprogramadoEvent`, `TurnoCanceladoEvent` al final del método (sin evento si el paciente no tiene email) — verificación: test que verifica la publicación y que el correo de confirmación sale con los dos enlaces únicos.
- [ ] 6.2 Implementar `TurnoParaRecordarPort` (turnos `RESERVADO`/`CONFIRMADO` en la ventana con paciente con email) — verificación: test del puerto que devuelve los turnos esperados y el contexto arranca sin el no-op.
- [ ] 6.3 Listeners de `TurnoCanceladoPorPacienteEvent` (→ `CANCELADO`) y `AsistenciaConfirmadaEvent` (→ `CONFIRMADO`), ignorando turnos inexistentes o ya cancelados — verificación: tests de integración abriendo `/api/notifications/cancel` y `/api/notifications/confirm` con token real: el turno cambia de estado.

## 7. Verificación integral (verify)

- [ ] 7.1 Correr la suite completa en verde: `./gradlew test` en `backend/` → `BUILD SUCCESSFUL`, 0 fallos, H2 en memoria y sin servicios externos — verificación: salida de Gradle con todos los escenarios del spec cubiertos.
- [ ] 7.2 Revisar reglas duras: cero datos reales de pacientes (Ley 25.326) y cero secrets commiteados — verificación: sin coincidencias de DNIs/emails reales ni credenciales en `git grep -iE "(password|secret).*=.*[^$]"`.

## 8. Cierre

- [ ] 8.1 Marcar `[x]` el checkbox de `C-05` en `CHANGES.md` — verificación: `grep "C-05" CHANGES.md` muestra completado.
- [ ] 8.2 PR final a `main` con tests en verde y revisión/aprobación de Thiago — verificación: PR mergeado en `main` y `./gradlew test` en verde sobre `main`.

## Workflow follow-up

- Archivar el change (`/opsx:archive c-05-gestion-turnos`) una vez mergeado el PR.
- Dejar lista la evidencia para el video: tests en verde, el momento de "Ajustar" del propose y el archivo archivado.
