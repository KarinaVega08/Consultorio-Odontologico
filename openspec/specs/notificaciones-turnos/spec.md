# notificaciones-turnos Specification

## Purpose

Conectar la agenda de turnos (`C-05`) con el módulo de notificaciones ya existente (`C-06`) sin modificarlo: publicar los eventos que disparan correos, proveer los turnos a recordar y reflejar en el estado del turno las acciones realizadas desde los enlaces únicos. La gestión de turnos en sí se especifica en `c-05-gestion-turnos` (rama `feat/c05-gestion-turnos`).

## Requirements

### Requirement: Publicación del evento de creación
El sistema SHALL publicar `TurnoCreadoEvent` con un `TurnoNotificable` completo cuando se crea un turno y el paciente tiene correo de recuperación vinculado, para que `C-06` envíe el correo de confirmación con enlaces únicos de confirmar asistencia y cancelar (RN-NT-01, RN-NT-02).

#### Scenario: La creación dispara la confirmación por email (feliz)
- **GIVEN** que existe un turno con un paciente ficticio con correo de recuperación vinculado
- **WHEN** se crea el turno vía `POST /api/appointments`
- **THEN** se publica `TurnoCreadoEvent` con `turnoId`, `pacienteId`, `dniPaciente`, `emailPaciente`, `profesionalNombre`, `fechaHoraInicio` y `prestacionNombre`
- **AND** `C-06` envía un correo de confirmación con dos enlaces únicos válidos (confirmar asistencia y cancelar)

#### Scenario: Paciente sin correo no dispara notificación (borde)
- **GIVEN** que el paciente del turno no tiene correo de recuperación vinculado
- **WHEN** se crea el turno
- **THEN** no se publica `TurnoCreadoEvent`
- **AND** no se registra ningún error de envío por destinatario nulo

### Requirement: Publicación del evento de reprogramación
El sistema SHALL publicar `TurnoReprogramadoEvent` con el `TurnoNotificable` actualizado y la `fechaHoraAnterior` cuando se reprograma un turno con correo vinculado.

#### Scenario: Reprogramar notifica el cambio de horario (feliz)
- **GIVEN** que existe un turno `RESERVADO` con correo vinculado y se reprograma de `09:00` a `14:00`
- **WHEN** se envía `PUT /api/appointments/{id}`
- **THEN** se publica `TurnoReprogramadoEvent` con `fechaHoraAnterior` igual a `09:00` y `turno.fechaHoraInicio()` igual a `14:00`
- **AND** `C-06` envía el correo de reprogramación

### Requirement: Publicación del evento de cancelación por API
El sistema SHALL publicar `TurnoCanceladoEvent` cuando se cancela un turno por `DELETE /api/appointments/{id}` y el paciente tiene correo vinculado.

#### Scenario: Cancelar por API avisa por correo (feliz)
- **GIVEN** que existe un turno `RESERVADO` con correo vinculado
- **WHEN** se envía `DELETE /api/appointments/{id}`
- **THEN** el turno queda `CANCELADO` (sin borrado físico) y se publica `TurnoCanceladoEvent`
- **AND** `C-06` envía el correo de cancelación

### Requirement: Consulta real de turnos a recordar
El sistema SHALL proveer una implementación de `TurnoParaRecordarPort` que devuelva los turnos `RESERVADO` o `CONFIRMADO` cuya `fechaHoraInicio` esté entre `desde` y `hasta` (inclusive) y cuyo paciente tenga correo, mapeados a `TurnoNotificable`, reemplazando el bean no-op sin cambios en `C-06`.

#### Scenario: El scheduler encuentra turnos para recordar (feliz)
- **GIVEN** que existen dos turnos `RESERVADO`/`CONFIRMADO` con correo dentro de la ventana de antelación configurada
- **WHEN** `NotificacionService.enviarRecordatorios()` consulta `findTurnosEnVentana(desde, hasta)`
- **THEN** recibe exactamente esos dos `TurnoNotificable` con todos sus campos completos
- **AND** se envía un recordatorio por turno (RN-NT-01, CA-2 de C-06)

#### Scenario: Turnos cancelados, sin correo o fuera de ventana se excluyen (borde)
- **GIVEN** que existen un turno `CANCELADO`, uno sin correo y uno fuera de la ventana, además de uno válido
- **WHEN** se consulta `findTurnosEnVentana(desde, hasta)`
- **THEN** solo se devuelve el turno válido
- **AND** el bean no-op de `NotificationIntegrationConfig` ya no está activo porque existe esta implementación

### Requirement: Cancelación desde el enlace único actualiza el turno
El sistema SHALL cambiar el estado del turno a `CANCELADO` (nunca borrado físico) al recibir `TurnoCanceladoPorPacienteEvent`, manteniendo la idempotencia ante turnos inexistentes o ya cancelados y sin propagar excepciones hacia el enlace público.

#### Scenario: Cancelar desde el link pone el turno en CANCELADO (feliz)
- **GIVEN** que existe un turno `RESERVADO` con un token de cancelación válido y sin usar
- **WHEN** el paciente abre `GET /api/notifications/cancel?token=...`
- **THEN** el token se marca usado y se publica `TurnoCanceladoPorPacienteEvent`
- **AND** el turno queda en estado `CANCELADO` y su hueco queda libre para nuevas reservas

#### Scenario: Turno ya cancelado o inexistente no rompe el enlace (borde)
- **GIVEN** que el turno asociado al token ya está `CANCELADO` (o no existe)
- **WHEN** se abre el enlace de cancelación con un token válido
- **THEN** se registra el hecho con SLF4J y no se lanza excepción hacia el enlace
- **AND** el estado del turno existente no se modifica

### Requirement: Confirmación de asistencia desde el enlace único
El sistema SHALL cambiar el estado del turno a `CONFIRMADO` al recibir `AsistenciaConfirmadaEvent`, sin propagar excepciones hacia el enlace público.

#### Scenario: Confirmar desde el link pone el turno en CONFIRMADO (feliz)
- **GIVEN** que existe un turno `RESERVADO` con un token de confirmación válido y sin usar
- **WHEN** el paciente abre `GET /api/notifications/confirm?token=...`
- **THEN** el token se marca usado y se publica `AsistenciaConfirmadaEvent`
- **AND** el turno queda en estado `CONFIRMADO`

### Requirement: Sin regresión de seguridad ni de C-06
El sistema SHALL conservar la cadena de seguridad existente: los enlaces `/api/notifications/**` siguen siendo públicos mediante token y los endpoints de turnos siguen exigiendo autenticación; el módulo `com.consultorio.notification` SHALL permanecer sin modificaciones.

#### Scenario: Enlace público funciona y la API de turnos sigue protegida (seguridad)
- **GIVEN** un token válido de cancelación y un cliente sin credenciales
- **WHEN** el cliente abre el enlace público y luego intenta `GET /api/appointments`
- **THEN** el enlace se procesa correctamente y `GET /api/appointments` responde `401`/`403`
- **AND** no se modifica ningún archivo bajo `backend/src/main/java/com/consultorio/notification/`
