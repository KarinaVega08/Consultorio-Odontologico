# Spec Delta

## Purpose

Permite a pacientes y personal del consultorio consultar los horarios realmente libres de un profesional para una fecha y una prestación, considerando horarios laborales, bloqueos y turnos existentes antes de reservar.

## ADDED Requirements

### Requirement: Consulta de disponibilidad por profesional y prestación
El sistema SHALL exponer `GET /api/availability` como endpoint público y SHALL calcular las franjas libres usando el horario de atención vigente del profesional para el día de la semana de la fecha, la duración de la prestación seleccionada, los bloqueos de profesional y/o sillón/box y los turnos existentes (excluyendo `CANCELADO`). La respuesta SHALL ser `200 OK` con `profesionalId`, `fecha`, `duracionMinutos` y una lista de `franjas` con `inicio` y `fin`.

#### Scenario: Franjas libres en horario laboral
- **GIVEN** un profesional con horario de 09:00 a 12:00 los miércoles, sin bloqueos ni turnos, y una prestación de 30 minutos
- **WHEN** se consulta `GET /api/availability` para el 2026-11-25 con ese profesional y esa prestación
- **THEN** el sistema responde `200 OK` y la lista de franjas contiene 09:00, 09:30, 10:00, 10:30, 11:00 y 11:30

#### Scenario: Parámetros inválidos o faltantes
- **WHEN** se consulta `GET /api/availability` sin `profesionalId`, sin `prestacionId` o con una `fecha` con formato distinto de `YYYY-MM-DD`
- **THEN** el sistema responde `400` con errores de validación por campo

#### Scenario: Profesional o prestación inexistente
- **WHEN** se consulta disponibilidad con un `profesionalId` o un `prestacionId` que no existe
- **THEN** el sistema responde `404`

### Requirement: Exclusión de franjas ocupadas y regla de borde
El sistema SHALL considerar que dos intervalos se solapan solo cuando `inicioA < finB && inicioB < finA`; una franja que comienza exactamente cuando termina un turno o un bloqueo SHALL ser disponible. El sistema SHALL excluir toda franja cuya duración completa no quepa en una ventana de horario de atención o que se solape con un bloqueo del profesional/sillón/box o con un turno no cancelado.

#### Scenario: Turno existente borde a borde
- **GIVEN** un horario de 09:00 a 12:00, un turno confirmado de 09:30 a 10:00 y una prestación de 30 minutos
- **WHEN** se consulta disponibilidad
- **THEN** la franja 09:30 NO aparece y la franja 10:00 SÍ aparece

#### Scenario: Bloqueo por sillón/box
- **GIVEN** un horario de 09:00 a 12:00, un bloqueo del sillón "Sillón 1" de 10:00 a 11:00 y una prestación de 30 minutos
- **WHEN** se consulta disponibilidad para ese sillón
- **THEN** las franjas 10:00 y 10:30 NO aparecen, y las franjas 09:00, 09:30, 11:00 y 11:30 SÍ aparecen

#### Scenario: Duración que no cabe en el horario
- **GIVEN** un horario de 09:00 a 09:20 y una prestación de 30 minutos
- **WHEN** se consulta disponibilidad
- **THEN** el sistema responde `200 OK` con `franjas: []`

### Requirement: Ausencia de horario no es un error
El sistema SHALL responder `200 OK` con `franjas: []` cuando el profesional no tiene horario de atención registrado para el día de la semana de la fecha consultada, o cuando ninguna candidata cabe completa en las ventanas existentes.

#### Scenario: Sin horario de atención ese día
- **GIVEN** un profesional sin horario de atención registrado para el miércoles
- **WHEN** se consulta disponibilidad para el 2026-11-25
- **THEN** el sistema responde `200 OK` con la lista de franjas vacía

### Requirement: Privacidad de datos y ejecución autocontenida
El sistema SHALL usar exclusivamente datos ficticios en ejemplos y tests, y la verificación SHALL ejecutarse con H2 en memoria sin requerir PostgreSQL, Docker ni servicios externos.

#### Scenario: Suite de tests autocontenida
- **WHEN** se ejecuta `./gradlew test` en `backend/`
- **THEN** la suite finaliza con `BUILD SUCCESSFUL` usando solo H2 en memoria y datos ficticios
