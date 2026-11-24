# C-06 notificaciones Specification

## Purpose

Enviar notificaciones por email al gestionar turnos: confirmación, reprogramación, cancelación, recordatorios automáticos con antelación configurable y enlaces únicos de confirmación y cancelación.

## Requirements

### Requirement: Confirmación y cambios de turno por email
El sistema SHALL enviar correos de confirmación, reprogramación y cancelación cuando ocurren esas operaciones sobre un turno con correo de recuperación vinculado.

#### Scenario: Turno creado con correo
- **WHEN** se crea un turno para un paciente con Gmail vinculado
- **THEN** se envía el correo de confirmación con sus enlaces únicos

#### Scenario: Paciente sin correo
- **WHEN** se crea un turno para un paciente sin Gmail
- **THEN** no se intenta enviar correo ni se registra error de SMTP

### Requirement: Recordatorios automáticos
El sistema SHALL consultar los turnos en la ventana de antelación configurable y enviar un recordatorio por turno, de forma idempotente.

#### Scenario: Recordatorio una sola vez
- **GIVEN** un turno dentro de la ventana de antelación
- **WHEN** se ejecuta el scheduler dos veces
- **THEN** se envía un solo recordatorio

#### Scenario: Antelación configurable
- **WHEN** se actualiza la antelación a 48 horas
- **THEN** la ventana de recordatorios usa ese valor

### Requirement: Enlaces únicos de confirmación y cancelación
El sistema SHALL emitir tokens de un solo uso para confirmar asistencia o cancelar, con vencimiento, sin modificar la configuración de seguridad existente.

#### Scenario: Enlace reutilizado
- **WHEN** se intenta usar un enlace ya consumido o vencido
- **THEN** el sistema rechaza la operación

#### Scenario: Enlace público válido
- **WHEN** se abre un enlace vigente
- **THEN** se concreta la operación sin exigir sesión del paciente

### Requirement: Integración con la agenda sin regresión
El módulo de notificaciones SHALL consumir eventos de turnos y un puerto de consulta real, sin que fallos de SMTP interrumpan la creación o cancelación de turnos.

#### Scenario: SMTP caído
- **GIVEN** un SMTP no disponible
- **WHEN** se crea un turno
- **THEN** la operación de agenda termina correctamente y el fallo se registra en logs

#### Scenario: Contrato estable
- **WHEN** se revisa el diff de integración
- **THEN** el paquete `notification` y `SecurityConfig` no fueron modificados
