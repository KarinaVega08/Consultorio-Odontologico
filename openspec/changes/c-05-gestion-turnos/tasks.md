# Tasks — C-05 gestion-turnos

> Checklist de trabajo para **Caste**. Rama: `feat/c05-gestion-turnos` (solo este change).
> Referencias: `specs/gestion-turnos/spec.md` (qué) y `design.md` (cómo).
> La integración con `C-06` se planifica en la rama `feat/c06-integracion-turnos` y **no** pertenece a esta rama.

## 1. Coordinación y preparación (antes de codear)

- [ ] 1.1 Responderle a **Mai** (su tarea A3) con el modelo de `Turno` del design D1/D3 (campos, ubicación `domain.model.Turno`, regla estricta) — verificación: respuesta enviada en el chat del equipo y confirmación de Mai.
- [ ] 1.2 Avisar a **Thiago** que el apply requiere el merge de `C-02` (`Paciente`, `AuthenticatedUser`, filtro Bearer) — verificación: `git fetch origin && git log origin/main` muestra el merge de `feat/c02-registro-pacientes`.
- [ ] 1.3 Confirmar en verde la base antes de empezar — verificación: `./gradlew test` en `backend/` → `BUILD SUCCESSFUL` (70 tests actuales).

## 2. Ciclo OPSX — Explore y Propose

- [x] 2.1 Explore: relevar estado del código, KB y specs relacionadas en `explore.md` — verificación: archivo presente con brechas detectadas.
- [x] 2.2 Propose: `proposal.md` con Why, What Changes y capacidad `gestion-turnos` — verificación: `openspec status --change c-05-gestion-turnos` marca `proposal` done.
- [x] 2.3 Spec: `specs/gestion-turnos/spec.md` con 9 requirements y 12 escenarios WHEN/THEN (feliz, error de negocio, borde y seguridad) — verificación: `openspec validate c-05-gestion-turnos --strict` sin errores.
- [x] 2.4 Design: `design.md` con decisiones D1-D5 (IN-04 resuelto; sin integración C-06) — verificación: archivo presente con todas las secciones.
- [x] 2.5 Tasks: este archivo — verificación: `openspec status --change c-05-gestion-turnos` → todos los artefactos `done`.
- [x] 2.6 Subir la rama a `origin/feat/c05-gestion-turnos` (sin PR: la revisión y el merge los hace Thiago) — verificación: `git ls-remote origin refs/heads/feat/c05-gestion-turnos` muestra el commit del propose.

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

## 6. Verificación integral (verify)

- [ ] 6.1 Correr la suite completa en verde: `./gradlew test` en `backend/` → `BUILD SUCCESSFUL`, 0 fallos, H2 en memoria y sin servicios externos — verificación: salida de Gradle con todos los escenarios del spec cubiertos.
- [ ] 6.2 Revisar reglas duras: cero datos reales de pacientes (Ley 25.326) y cero secrets commiteados — verificación: sin coincidencias de DNIs/emails reales ni credenciales en `git grep -iE "(password|secret).*=.*[^$]"`.

## 7. Cierre de esta rama

- [ ] 7.1 Dejar la rama lista para revisión: commit de planificación pusheado, `openspec validate` en verde y **cero archivos de C-06** en la rama — verificación: `git diff --name-only origin/main..HEAD` solo muestra rutas bajo `openspec/changes/c-05-gestion-turnos/`.

## Workflow follow-up

- La revisión, el merge y el archivado del change los hace **Thiago** (no se ejecutan desde esta rama).
- Dejar lista la evidencia para el video: tests en verde, el momento de "Ajustar" del propose y el archivo archivado.
