# Specification — C-04 Disponibilidad de Turnos

## 1. Requerimientos Funcionales

- **RF-01**: El sistema expone `GET /api/availability` para consultar horarios libres.
- **RF-02**: La consulta considera el horario laboral vigente del profesional para el día de la semana de la fecha.
- **RF-03**: La consulta excluye franjas afectadas por bloqueos de profesional y/o sillón/box.
- **RF-04**: La consulta excluye franjas ocupadas por turnos existentes (excluyendo `CANCELADO`).
- **RF-05**: La duración del bloque se deriva de la prestación seleccionada (`duracion_minutos`).
- **RF-06**: Los turnos consecutivos borde a borde (fin = inicio del siguiente) están permitidos.
- **RF-07**: Si no hay horario de atención o ninguna franja cabe completa, la respuesta es `200` con lista vacía.

## 2. Restricciones

- **RR-01**: El endpoint es público (ya configurado en `SecurityConfig` de `C-01`).
- **RR-02**: Nunca se almacenan datos reales de pacientes (RN-EX-01).
- **RR-03**: Tests autocontenidos en H2, sin Docker ni servicios externos (RN-EX-02).
- **RR-04**: La lógica de cálculo debe ser reutilizable por `C-05`.

## 3. Escenarios Verificables (Gherkin)

### Escenario 1 — Franjas libres en horario laboral (feliz)

- **Given** que el profesional "Dr. Demo" tiene horario de atención de 09:00 a 12:00 los miércoles.
- **And** no tiene bloqueos ni turnos existentes para el 2026-11-25.
- **And** la prestación "Consulta" dura 30 minutos.
- **When** un cliente consulta `GET /api/availability?profesionalId=...&fecha=2026-11-25&prestacionId=...`
- **Then** el servidor responde `200 OK`.
- **And** la lista de franjas contiene: 09:00, 09:30, 10:00, 10:30, 11:00 y 11:30.

### Escenario 2 — Sin horario de atención ese día (error de negocio)

- **Given** que el profesional no tiene horario de atención registrado para el miércoles.
- **When** un cliente consulta disponibilidad para el 2026-11-25.
- **Then** el servidor responde `200 OK`.
- **And** la lista de franjas está vacía.

### Escenario 3 — Turno existente borde a borde (borde)

- **Given** que el profesional tiene horario de 09:00 a 12:00.
- **And** existe un turno confirmado de 09:30 a 10:00.
- **And** la prestación dura 30 minutos.
- **When** se consulta disponibilidad.
- **Then** la franja 09:30 NO aparece (ocupada).
- **And** la franja 10:00 SÍ aparece (empieza exactamente cuando termina el turno anterior).

### Escenario 4 — Bloqueo por sillón/box (borde)

- **Given** que el profesional tiene horario de 09:00 a 12:00.
- **And** el sillón "Sillón 1" tiene un bloqueo de 10:00 a 11:00.
- **And** la prestación dura 30 minutos.
- **When** se consulta disponibilidad para el sillón "Sillón 1".
- **Then** las franjas 10:00 y 10:30 NO aparecen.
- **And** las franjas 09:00, 09:30, 11:00 y 11:30 SÍ aparecen.

### Escenario 5 — Duración no ajustable al horario (borde)

- **Given** que el profesional tiene horario de 09:00 a 09:20.
- **And** la prestación dura 30 minutos.
- **When** se consulta disponibilidad.
- **Then** el servidor responde `200 OK`.
- **And** la lista de franjas está vacía (la prestación no cabe completa en la ventana).

## 4. Criterios de Aceptación Globales

1. `./gradlew test` en `backend/` finaliza con `BUILD SUCCESSFUL` y 0 tests fallidos.
2. Los 5 escenarios anteriores tienen al menos un test automatizado cada uno en verde.
3. El endpoint responde `200` con `franjas: []` (no `404` ni `500`) cuando no hay disponibilidad.
4. La lógica de solapamiento usa la regla estricta `inicioA < finB && inicioB < finA` (borde permitido).
