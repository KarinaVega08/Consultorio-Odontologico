# Modelo de Datos

## Dominios

- **Autenticación y usuarios**: gestión de credenciales, roles y recuperación de contraseña.
- **Consultorio**: configuración de profesionales, sillones/boxes, horarios de atención y bloqueos.
- **Prestaciones**: catálogo de tipos de consulta/tratamiento con duración en minutos.
- **Pacientes**: registro de pacientes con DNI único y datos de contacto.
- **Turnos**: agenda de citas con vinculación paciente/profesional/sillón/prestación/fecha.
- **Notificaciones**: recordatorios automáticos por email.

## ERD (Entity Relationship Diagram)

```
Paciente 1 ──< Turno >── 1 Profesional
Paciente 1 ──< Turno >── 1 SillonBox
Prestacion 1 ──< Turno >── 1 Turno (duración heredada)
Profesional 1 ──< Turno >── *
SillonBox 1 ──< Turno >── *
HorarioAtencion 1 ──* Profesional
Bloqueo 1 ──* Profesional | SillonBox
```

## Entidades

### Paciente
- Atributos:
  - id: UUID (PK)
  - nombre_completo: String (no nulo)
  - dni: String (7-8 dígitos, único, no nulo)
  - password_hash: String (no nulo)
  - email_recuperacion: String (Gmail, opcional, vinculado una sola vez)
  - fecha_registro: Timestamp (no nulo)
  - activo: Boolean (default true)
- Relaciones:
  - 1:N con Turno (un paciente tiene muchos turnos)
- Constraints:
  - dni único en la base de datos
  - formato numérico 7-8 dígitos
- Índices relevantes:
  - dni (unique, btree)

### Profesional
- Atributos:
  - id: UUID (PK)
  - nombre_completo: String (no nulo)
  - matricula: String (opcional)
  - activo: Boolean (default true)
- Relaciones:
  - 1:N con Turno
  - 1:N con HorarioAtencion
  - 1:N con Bloqueo (como profesional bloqueado)
- Índices relevantes:
  - activo (btree)

### SillonBox
- Atributos:
  - id: UUID (PK)
  - nombre: String (no nulo, ej. "Sillón 1", "Box A")
  - activo: Boolean (default true)
- Relaciones:
  - 1:N con Turno
  - 1:N con Bloqueo (como sillón bloqueado)
- Índices relevantes:
  - activo (btree)

### Prestacion
- Atributos:
  - id: UUID (PK)
  - nombre: String (no nulo, ej. "Consulta/Diagnóstico", "Obturación/Arreglo")
  - duracion_minutos: Integer (no nulo, > 0)
  - descripcion: String (opcional)
  - activo: Boolean (default true)
- Relaciones:
  - 1:N con Turno
- Constraints:
  - duracion_minutos > 0
- Índices relevantes:
  - activo (btree)

### Turno
- Atributos:
  - id: UUID (PK)
  - paciente_id: UUID (FK → Paciente, no nulo)
  - profesional_id: UUID (FK → Profesional, no nulo)
  - sillon_box_id: UUID (FK → SillonBox, no nulo)
  - prestacion_id: UUID (FK → Prestacion, no nulo)
  - fecha_hora_inicio: Timestamp (no nulo)
  - fecha_hora_fin: Timestamp (calculado = inicio + duracion_prestacion, no nulo)
  - estado: Enum (RESERVADO, CONFIRMADO, CANCELADO, COMPLETADO, default RESERVADO)
  - creado_por: String (no nulo, quién creó el turno: rol + dni)
  - fecha_creacion: Timestamp (no nulo)
  - fecha_actualizacion: Timestamp (no nulo)
- Relaciones:
  - N:1 con Paciente
  - N:1 con Profesional
  - N:1 con SillonBox
  - N:1 con Prestacion
- Constraints:
  - fecha_hora_fin > fecha_hora_inicio
  - fecha_hora_fin dentro del horario laboral del profesional
  - No solapamiento con otro turno del mismo profesional (excluyendo turno CANCELADO)
  - No solapamiento con otro turno del mismo sillón/box (excluyendo turno CANCELADO)
  - Turnos consecutivos borde a borde (fin = inicio del siguiente) están permitidos
- Índices relevantes:
  - (profesional_id, fecha_hora_inicio, fecha_hora_fin) con exclusión de solapamiento (constraint de exclusión o trigger)
  - (sillon_box_id, fecha_hora_inicio, fecha_hora_fin) con exclusión de solapamiento
  - estado (btree, para consultas de turnos pendientes/cancelados)

### HorarioAtencion
- Atributos:
  - id: UUID (PK)
  - profesional_id: UUID (FK → Profesional, no nulo)
  - dia_semana: Integer (1-7, lunes-domingo)
  - hora_inicio: Time (no nulo)
  - hora_fin: Time (no nulo, > hora_inicio)
  - activo: Boolean (default true)

### Bloqueo
- Atributos:
  - id: UUID (PK)
  - profesional_id: UUID (FK → Profesional, opcional)
  - sillon_box_id: UUID (FK → SillonBox, opcional)
  - fecha_hora_inicio: Timestamp (no nulo)
  - fecha_hora_fin: Timestamp (no nulo, > inicio)
  - motivo: String (opcional)

## Seed data inicial

- Rol por defecto para el primer usuario creado: Administrador (se asume que el primer registro es el dueño del consultorio)
- Prestaciones de ejemplo (configurables por el Administrador):
  - Consulta/Diagnóstico — 30 min
  - Limpieza — 30 min
  - Obturación/Arreglo — 45 min
  - Extracción — 45 min
  - Endodoncia — 60 min
  - Control Ortodoncia — 20 min
