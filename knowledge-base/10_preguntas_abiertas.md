# Preguntas Abiertas

## Inconsistencias detectadas

### IN-01 — Alcance de multi-sucursal
**Documento A (Discovery) dice**: "Multi-sucursal está en funcionalidades opcionales (post-lanzamiento)."
**Documento B (Reglas de negocio) dice**: "Solo el Administrador crea/edita profesionales, sillones/boxes y prestaciones" — sin aclarar si aplica a una sola sucursal o varias.
**Impacto**: Si se decide multi-sucursal en el MVP, el modelo de datos necesita un campo `sucursal_id` en Profesional, SillonBox y Turno, y el RBAC debe filtrar por sucursal.
**Resolución propuesta**: Mantener mono-sucursal en el MVP; añadir `sucursal_id` como mejora post-lanzamiento.

### IN-02 — Horas de antelación para recordatorio por email
**Documento A (Discovery) dice**: "¿Cuántas horas exactas antes del turno se dispara el recordatorio automático por correo electrónico (ej. 24 hs o 48 hs antes)?"
**Documento B (Funcionalidades) dice**: "El sistema envía recordatorios con antelación configurable (horas antes del turno)" — sin valor por defecto.
**Impacto**: Sin un valor por defecto, el recordatorio no se dispara hasta que se defina.
**Resolución propuesta**: Valor por defecto de 24 horas, configurable por el Administrador.

### IN-03 — Duración mínima del bloque/grilla de agenda
**Documento A (Discovery) dice**: "¿Cuál es la duración mínima del bloque/grilla de agenda (ej. intervalos de 15 o 30 minutos)?"
**Documento B (Modelo de datos) no define** un campo `slot_duration_minutos` en la configuración del consultorio.
**Impacto**: El motor de disponibilidad necesita saber la granularidad de los horarios disponibles.
**Resolución propuesta**: Valor por defecto de 30 minutos, configurable por el Administrador.

### IN-04 — Estrategia de bloqueo en base de datos
**Documento A (Discovery) dice**: "¿Qué estrategia de bloqueo/transacción en base de datos se usará para evitar reservas simultáneas?"
**Documento B (Modelo de datos) dice**: "Constraint de exclusión o trigger" para anti-solapamiento, sin definir cuál.
**Impacto**: Sin una estrategia definida, existe riesgo de race conditions (doble turno).
**Resolución proposta**: Usar constraint de exclusión de PostgreSQL (`EXCLUDE USING gist`) sobre profesional y rango de tiempo, y sobre sillón/box y rango de tiempo.

### IN-05 — Funcionalidades de competidores sin evidencia pública verificable
**Documento A (Discovery) dice**: "¿Qué funcionalidades de los competidores relevados no tienen evidencia pública verificable y quedarán marcadas como 'No evidenciado'?"
**Impacto**: La tabla comparativa del informe de Discovery marca varias celdas como "No evidenciado"; no se debe asumir funcionalidades que no están respaldadas.
**Resolución propuesta**: Mantener el marcado "No evidenciado" en el informe y no usar esos datos como requisitos del MVP.

## Preguntas abiertas (priorizadas)

| Prioridad | Pregunta | Bloquea | Decisor |
|-----------|----------|---------|---------|
| Alta | ¿Cuántas horas antes del turno se dispara el recordatorio por email (24h, 48h, otra)? | Sprint 1 | Product Owner |
| Alta | ¿Valor por defecto de la grilla de agenda: intervalos de 15 o 30 minutos? | Sprint 1 | Equipo técnico |
| Alta | ¿Estrategia de bloqueo en BD: constraint de exclusión PostgreSQL o trigger? | Sprint 1 | Equipo técnico |
| Media | ¿Se necesita multi-sucursal en el MVP o es estrictamente post-lanzamiento? | Sprint 2 | Product Owner |
| Media | ¿El Gmail de recuperación es obligatorio o opcional para todos los pacientes? | Sprint 2 | Product Owner |
| Baja | ¿Qué funcionalidades de competidores quedan definitivamente como "No evidenciado" en el informe final? | Lanzamiento | Equipo técnico |
| Baja | ¿Hay requisitos de auditoría (audit trail append-only) para turnos cancelados? | Lanzamiento | Equipo técnico |
