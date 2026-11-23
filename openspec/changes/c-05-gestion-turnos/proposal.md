# Proposal — C-05 gestion-turnos

## Why

El MVP no tiene todavía ninguna forma de **reservar, reprogramar o cancelar turnos**: es el core del producto (Épica 3, US-009 a US-012) y el eslabón del camino crítico `C-01 → C-02 → C-05 → C-06`. Sin él, los recordatorios y enlaces únicos de `C-06` quedan colgados (su `TurnoParaRecordarPort` sigue en no-op) y `C-04` no tiene contra qué validar las franjas.

## What Changes

- Nueva entidad de dominio **`Turno`** (UUID, FKs a `Paciente`, `Profesional`, `SillonBox` y `Prestacion`, `fechaHoraInicio`, `fechaHoraFin` calculada en el servidor = inicio + `duracionMinutos` de la prestación, `estado` con el enum `EstadoTurno` ya existente, `creadoPor` y auditoría vía `BaseEntity`).
- **`POST /api/appointments`** — creación de turno (paciente autogestionado o personal del consultorio) con validación **anti-solapamiento por profesional y por sillón/box dentro de una transacción** (race-condition guard) según RN-TU-01 y RN-TU-02.
- **`PUT /api/appointments/{id}`** — reprogramación que **libera el hueco anterior inmediatamente** y revalida anti-solapamiento (RN-TU-04).
- **`DELETE /api/appointments/{id}`** — cancelación a estado `CANCELADO` (**nunca borrado físico**, RN-TU-04) liberando el hueco de inmediato.
- **`GET /api/appointments`** — listado filtrable por profesional, fecha y estado.
- Validación de **RN-TU-03**: el turno debe caber completo dentro del horario laboral vigente del profesional y sin chocar con bloqueos de profesional y/o sillón/box.
- **Turnos consecutivos borde a borde permitidos** (`finA == inicioB` no es solapamiento) — criterio estricto `inicioA < finB && inicioB < finA`, idéntico al de la spec de `C-04`.
- **Integración con `C-06`** (change que Caste comparte con Kari): publicación de `TurnoCreadoEvent`, `TurnoReprogramadoEvent` y `TurnoCanceladoEvent`, implementación real de `TurnoParaRecordarPort` (reemplaza el bean no-op), y escucha de `TurnoCanceladoPorPacienteEvent` (→ `CANCELADO`) y `AsistenciaConfirmadaEvent` (→ `CONFIRMADO`), que hoy nadie recibe.
- RBAC sobre los endpoints: cualquier rol autenticado; el paciente solo opera sobre **sus propios turnos**; recepción/odontólogo/administrador sobre cualquiera (Flujo 4, paso 2).

## Capabilities

### New Capabilities

- `gestion-turnos`: creación, reprogramación, cancelación lógica y consulta de turnos de la agenda del consultorio, con anti-solapamiento por profesional y sillón/box, turnos borde a borde permitidos e integración de eventos con el módulo de notificaciones.

### Modified Capabilities

<!-- Ninguna: los requirements de C-06, C-03 y C-04 no cambian; C-05 los consume. -->

## Impact

- **Código nuevo**: `backend/src/main/java/com/consultorio/domain/model/Turno.java`, `domain/repository/TurnoRepository.java`, `application/service/TurnoService.java` (+ mapper a `TurnoNotificable`), `application/dto/*`, `api/controller/AppointmentController.java`, implementación de `TurnoParaRecordarPort` y listeners de eventos de `C-06`.
- **Dependencias**: requiere el merge de **`C-02`** (entidad `Paciente`, `AuthenticatedUser` y filtro Bearer) para el apply; consume entidades y repositorios de **`C-03`** (`Profesional`, `SillonBox`, `Prestacion`, `HorarioAtencion`, `Bloqueo`) ya presentes en `main`; se alinea con la regla de solapamiento de **`C-04`** y con los puertos/eventos de **`C-06`**.
- **BD**: estrategia anti-solapamiento compatible con H2 (tests) y PostgreSQL (runtime) — resuelve IN-04.
- **Tests**: nueva batería sobre H2 con datos ficticios (RN-EX-01 / RN-EX-02), sin servicios externos.

## Leer antes

- `knowledge-base/04_modelo_de_datos.md` — §Turno
- `knowledge-base/05_reglas_de_negocio.md` — §RN-TU-01 a RN-TU-05, §RN-EX-01, §RN-EX-02
- `knowledge-base/06_funcionalidades.md` — §Épica 3 (US-009 a US-012)
- `knowledge-base/07_flujos_principales.md` — §Flujo 2, §Flujo 3, §Flujo 4
- `knowledge-base/10_preguntas_abiertas.md` — §IN-04 (estrategia de bloqueo en BD)
