# Spec Delta — C-05 gestion-turnos

## Purpose

Gestionar la agenda de turnos del consultorio: crear, reprogramar y cancelar citas sin solapamientos por profesional ni por sillón/box, conservando el historial de turnos y publicando los eventos que consume el módulo de notificaciones.

## ADDED Requirements

### Requirement: Creación de turno con duración derivada de la prestación
El sistema SHALL crear un turno vía `POST /api/appointments` cuando un usuario autenticado indica profesional, sillón/box, prestación y fecha/hora de inicio. El servidor SHALL calcular `fecha_hora_fin` = inicio + duración de la prestación seleccionada y SHALL persistir el turno en estado `RESERVADO`.

#### Scenario: Creación exitosa de turno (feliz)
- **GIVEN** que un recepcionista está autenticado y existen un profesional, un sillón/box y una prestación de 30 minutos activos
- **WHEN** envía `POST /api/appointments` con inicio `2026-11-25T09:00:00` dentro del horario laboral y sin turnos ocupados
- **THEN** el sistema responde `201 Created` con el turno en estado `RESERVADO`
- **AND** `fecha_hora_fin` es `2026-11-25T09:30:00` (calculado en el servidor, no recibido del cliente)

### Requirement: Anti-solapamiento por profesional
El sistema SHALL rechazar la creación o reprogramación de un turno cuyo rango se solape con un turno existente no cancelado del mismo profesional. Dos turnos se solapan si `inicioA < finB && inicioB < finA`; los turnos `CANCELADO` no ocupan horario.

#### Scenario: Solapamiento por profesional rechazado (error de negocio)
- **GIVEN** que el profesional tiene un turno `RESERVADO` de `09:00` a `10:00`
- **WHEN** se intenta crear otro turno del mismo profesional de `09:30` a `10:30`
- **THEN** el sistema responde `400 Bad Request` con mensaje `"El profesional o el sillón ya tiene un turno en ese horario"`
- **AND** el turno nuevo no se persiste

### Requirement: Anti-solapamiento por sillón/box
El sistema SHALL rechazar la creación o reprogramación de un turno cuyo rango se solape con un turno existente no cancelado del mismo sillón/box, aunque el profesional sea distinto.

#### Scenario: Solapamiento por sillón/box rechazado (error de negocio)
- **GIVEN** que el sillón "Sillón 1" tiene un turno `CONFIRMADO` de `10:00` a `11:00` de otro profesional
- **WHEN** se intenta crear un turno en "Sillón 1" de `10:30` a `11:00` con un profesional distinto
- **THEN** el sistema responde `400 Bad Request` con mensaje `"El profesional o el sillón ya tiene un turno en ese horario"`
- **AND** el turno nuevo no se persiste

### Requirement: Turnos consecutivos borde a borde permitidos
El sistema SHALL permitir turnos consecutivos donde uno inicia exactamente cuando termina el anterior (`finA == inicioB` no es solapamiento), tanto por profesional como por sillón/box.

#### Scenario: Turno consecutivo borde a borde aceptado (borde)
- **GIVEN** que el profesional tiene un turno de `09:00` a `09:30`
- **WHEN** se crea un turno del mismo profesional (y del mismo sillón) que inicia a las `09:30`
- **THEN** el sistema responde `201 Created` con el turno en estado `RESERVADO`

### Requirement: El turno debe caber en el horario laboral y sin bloqueos
El sistema SHALL rechazar turnos que no estén contenidos completos dentro del horario laboral vigente del profesional para el día de la fecha, o que se solapen con un bloqueo del profesional o del sillón/box.

#### Scenario: Horario fuera de la atención (error de negocio)
- **GIVEN** que el profesional tiene horario de atención de `09:00` a `12:00` los miércoles
- **WHEN** se crea un turno el `2026-11-25` (miércoles) a las `12:00`
- **THEN** el sistema responde `400 Bad Request` con mensaje `"El horario seleccionado no está dentro del horario de atención"`

#### Scenario: Choque con bloqueo (error de negocio)
- **GIVEN** que el sillón "Sillón 1" tiene un bloqueo de `10:00` a `11:00`
- **WHEN** se crea un turno en "Sillón 1" de `10:30` a `11:00`
- **THEN** el sistema responde `400 Bad Request` indicando que el horario está bloqueado
- **AND** el turno nuevo no se persiste

### Requirement: Reprogramación que libera el hueco anterior
El sistema SHALL permitir reprogramar un turno existente a otro horario válido vía `PUT /api/appointments/{id}`, revalidando anti-solapamiento, horario y bloqueos, y SHALL liberar el hueco inmediatamente.

#### Scenario: Reprogramación exitosa libera el hueco anterior (feliz)
- **GIVEN** que existe un turno `RESERVADO` de `09:00` a `10:00`
- **WHEN** se envía `PUT /api/appointments/{id}` con inicio `2026-11-25T14:00:00`
- **THEN** el sistema responde `200 OK` con el turno en el nuevo horario
- **AND** al crear después un turno en el hueco `09:00` a `10:00` el sistema responde `201 Created`

### Requirement: Cancelación lógica sin borrado físico
El sistema SHALL cancelar turnos vía `DELETE /api/appointments/{id}` cambiando el estado a `CANCELADO` sin eliminar el registro, y SHALL liberar el hueco de inmediato para nuevas reservas (RN-TU-04).

#### Scenario: Cancelación libera el hueco y conserva el historial (feliz)
- **GIVEN** que existe un turno `RESERVADO` de `09:00` a `10:00`
- **WHEN** se envía `DELETE /api/appointments/{id}`
- **THEN** el sistema responde `200 OK` con el turno en estado `CANCELADO`
- **AND** el turno sigue siendo consultable vía `GET` (no hay borrado físico)
- **AND** al crear un turno en `09:00` a `10:00` el sistema responde `201 Created`

#### Scenario: Cancelación de un turno ya cancelado (error de negocio)
- **GIVEN** que existe un turno en estado `CANCELADO`
- **WHEN** se envía `DELETE /api/appointments/{id}` nuevamente
- **THEN** el sistema responde `400 Bad Request` con mensaje `"El turno ya se encuentra cancelado"`

### Requirement: Listado de turnos con filtros
El sistema SHALL exponer `GET /api/appointments` con filtros opcionales de profesional, fecha y estado, devolviendo `200 OK` con la lista resultante.

#### Scenario: Listado filtrado por profesional y estado (feliz)
- **GIVEN** que existen turnos de dos profesionales en distintos estados
- **WHEN** se envía `GET /api/appointments?profesionalId=...&estado=RESERVADO`
- **THEN** el sistema responde `200 OK` solo con los turnos de ese profesional en estado `RESERVADO`

### Requirement: Integración de eventos con el módulo de notificaciones
El sistema SHALL publicar `TurnoCreadoEvent`, `TurnoReprogramadoEvent` y `TurnoCanceladoEvent` (con los datos mínimos del turno para notificar) al crear, reprogramar y cancelar por API, SHALL implementar la consulta real de `TurnoParaRecordarPort`, y SHALL cambiar el estado a `CANCELADO` al recibir `TurnoCanceladoPorPacienteEvent` y a `CONFIRMADO` al recibir `AsistenciaConfirmadaEvent`.

#### Scenario: La creación dispara la confirmación por email (feliz, integración)
- **GIVEN** que el paciente del turno tiene correo de recuperación vinculado
- **WHEN** se crea el turno
- **THEN** se publica el evento de turno creado y el módulo de notificaciones envía el correo de confirmación con los enlaces únicos de confirmar asistencia y cancelar

#### Scenario: Cancelar desde el enlace único pone el turno en CANCELADO (integración)
- **GIVEN** que existe un turno `RESERVADO` con un token de cancelación válido y sin usar
- **WHEN** el paciente abre `GET /api/notifications/cancel?token=...`
- **THEN** el turno queda en estado `CANCELADO` (recibido vía evento desde el módulo de notificaciones)

#### Scenario: Confirmar asistencia desde el enlace único pone el turno en CONFIRMADO (integración)
- **GIVEN** que existe un turno `RESERVADO` con un token de confirmación válido y sin usar
- **WHEN** el paciente abre `GET /api/notifications/confirm?token=...`
- **THEN** el turno queda en estado `CONFIRMADO`

### Requirement: Autorización sobre los turnos según rol
El sistema SHALL exigir autenticación para todos los endpoints de turnos; el paciente SHALL poder operar solo sobre turnos propios y el personal (recepcionista, odontólogo, administrador) sobre cualquier turno.

#### Scenario: Acceso sin autenticación rechazado (seguridad)
- **GIVEN** que un cliente no envía token de autenticación
- **WHEN** solicita `POST /api/appointments` o `GET /api/appointments`
- **THEN** el sistema responde `401 Unauthorized` o `403 Forbidden`

#### Scenario: Paciente ajeno rechazado (seguridad)
- **GIVEN** que el paciente A está autenticado y existe un turno del paciente B
- **WHEN** el paciente A envía `DELETE /api/appointments/{id}` sobre el turno de B
- **THEN** el sistema responde `403 Forbidden` y el turno de B conserva su estado
