# Tasks — C-05 gestion-turnos

> Checklist de trabajo para **Caste**. Rama: `feat/c05-gestion-turnos` (solo este change).
> Referencias: `specs/gestion-turnos/spec.md` (qué) y `design.md` (cómo).
> La integración con `C-06` se planifica en la rama `feat/c06-integracion-turnos` y **no** pertenece a esta rama.

## 1. Coordinación y preparación (antes de codear)

- [ ] 1.1 Responderle a **Mai** (su tarea A3) con el modelo de `Turno` del design D1/D3 (campos, ubicación `domain.model.Turno`, regla estricta) — verificación: respuesta enviada en el chat del equipo y confirmación de Mai.
- [x] 1.2 Avisar a **Thiago** que el apply requiere el merge de `C-02` (`Paciente`, `AuthenticatedUser`, filtro Bearer) — verificación: `git log` confirma que `d5d8a73 feat(c02-auth-pacientes)` es ancestro de esta rama.
- [x] 1.3 Confirmar en verde la base antes de empezar — verificación: `./gradlew test` en `backend/` → `BUILD SUCCESSFUL`.

## 2. Ciclo OPSX — Explore y Propose

- [x] 2.1 Explore: relevar estado del código, KB y specs relacionadas en `explore.md` — verificación: archivo presente con brechas detectadas.
- [x] 2.2 Propose: `proposal.md` con Why, What Changes y capacidad `gestion-turnos` — verificación: `openspec status --change c-05-gestion-turnos` marca `proposal` done.
- [x] 2.3 Spec: `specs/gestion-turnos/spec.md` con 9 requirements y 12 escenarios WHEN/THEN (feliz, error de negocio, borde y seguridad) — verificación: `openspec validate c-05-gestion-turnos --strict` sin errores.
- [x] 2.4 Design: `design.md` con decisiones D1-D5 (IN-04 resuelto; sin integración C-06) — verificación: archivo presente con todas las secciones.
- [x] 2.5 Tasks: este archivo — verificación: `openspec status --change c-05-gestion-turnos` → todos los artefactos `done`.
- [x] 2.6 Subir la rama a `origin/feat/c05-gestion-turnos` (sin PR: la revisión y el merge los hace Thiago) — verificación: `git ls-remote origin refs/heads/feat/c05-gestion-turnos` muestra el commit del propose.

## 3. Modelo de datos (apply)

- [x] 3.1 Crear entidad `Turno` (hereda `BaseEntity`: UUID, auditoría) con FKs a `Paciente`, `Profesional`, `SillonBox`, `Prestacion`, `fechaHoraInicio`, `fechaHoraFin`, `estado` (`EstadoTurno`) y `creadoPor` — verificación: `TurnoPersistenceTest` en verde con `@DataJpaTest`.
- [x] 3.2 Crear `TurnoRepository` con consulta de turnos solapados por profesional y por sillón (excluyendo `CANCELADO`, excluyendo el propio id) y lock pesimista `PESSIMISTIC_WRITE` sobre profesional/sillón — verificación: `TurnoRepositoryTest` en verde (solape, borde, cancelados y locks).
- [x] 3.3 Crear DTOs con Bean Validation (`CreateAppointmentRequest`, `RescheduleAppointmentRequest`, `AppointmentResponse`, `AppointmentQuery`) — verificación: `AppointmentRequestValidationTest` en verde (400 con `fieldErrors`).

## 4. Servicio anti-solapamiento (apply)

- [x] 4.1 `TurnoService.crear`: calcular `fechaHoraFin` = inicio + duración de la prestación (nunca recibida del cliente) y validar RN-TU-03 (horario laboral del día y bloqueos) — verificación: `TurnoServiceTest` cubre los 3 escenarios en verde.
- [x] 4.2 Validar anti-solapamiento por profesional y sillón con la regla estricta `inicioA < finB && inicioB < finA` dentro de `@Transactional` con los locks del 3.2 — verificación: `TurnoServiceTest` (2 escenarios + `@Transactional` y locks) en verde.
- [x] 4.3 Test unitario de la regla de borde: `finA == inicioB` no se solapa — verificación: `TurnoSolapamientoTest` (3 tests, sin Spring) en verde.
- [x] 4.4 `TurnoService.reprogramar` (PUT): revalidar todo y liberar el hueco anterior — verificación: `TurnoServiceTest` (exclusión del propio id en el guard) en verde.
- [x] 4.5 `TurnoService.cancelar` (DELETE): estado `CANCELADO` sin borrado físico + rechazo si ya está cancelado — verificación: `TurnoServiceTest` (sin delete + rechazo) en verde.
- [x] 4.6 `TurnoService.listar` con filtros opcionales de profesional, fecha y estado — verificación: `TurnoServiceTest` + consulta `buscarConFiltros` en verde.

## 5. API y seguridad (apply)

- [x] 5.1 `AppointmentController` con `POST`, `PUT`, `DELETE` y `GET /api/appointments` invocando al servicio y devolviendo DTOs — verificación: `AppointmentControllerTest` en verde (201/200/400/404 + DTOs).
- [x] 5.2 RBAC: autenticación obligatoria; paciente solo sobre turnos propios; recepción/odontólogo/administrador sobre cualquiera (`@PreAuthorize`) — verificación: `AppointmentControllerTest` + `TurnoServiceTest` en verde (sin auth 403, rol inválido 403, paciente ajeno 403, dueño/personal OK).

## 6. Verificación integral (verify)

- [x] 6.1 Correr la suite completa en verde: `./gradlew clean test` en `backend/` → `BUILD SUCCESSFUL`, 162 tests, 0 fallos, H2 en memoria y sin servicios externos — verificación: 32 suites XML con `failures_errors=0`.
- [x] 6.2 Revisar reglas duras: cero datos reales de pacientes (Ley 25.326) y cero secrets commiteados — verificación: solo DNIs ficticios `999999xx` y nombres "Ficticio/a"; las únicas coincidencias de `password|secret` son la variable local generada con `SecureRandom` en `SecurityConfig` y un password de prueba preexistente en `SecurityConfigTest`.

## 7. Cierre de esta rama

- [x] 7.1 Dejar la rama lista para revisión: commit de planificación pusheado, `openspec validate c-05-gestion-turnos --strict` en verde y **cero archivos de C-06** en la rama — verificación: `git diff --name-only origin/main..HEAD` solo muestra rutas bajo `openspec/changes/c-05-gestion-turnos/`. Nota: por instrucción del usuario **no se hicieron commit ni push del código de implementación**; ese trabajo quedó en el working tree sin commitear.

## Workflow follow-up

- La revisión, el merge y el archivado del change los hace **Thiago** (no se ejecutan desde esta rama).
- Dejar lista la evidencia para el video: tests en verde, el momento de "Ajustar" del propose y el archivo archivado.
