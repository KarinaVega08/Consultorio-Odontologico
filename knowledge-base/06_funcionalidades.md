# Funcionalidades

Organizadas por **épica** y luego por **historia de usuario** (formato US-NNN).

## Épica 1: Gestión de Pacientes y Autenticación

### US-001 — Registro de paciente
**Como** paciente  
**Quiero** registrarme con mi nombre real, DNI y contraseña  
**Para** crear mi cuenta y acceder al sistema

**Criterios de aceptación**:
- [ ] CA-1: El sistema valida que el DNI tenga 7-8 dígitos numéricos
- [ ] CA-2: El sistema rechaza el registro si el DNI ya existe en la base de datos
- [ ] CA-3: Tras el registro exitoso, se invita a vincular un correo Gmail de recuperación (una sola vez)

**Reglas relacionadas**: RN-AU-01, RN-AU-02, RN-AU-03, RN-EX-01

### US-002 — Login de paciente
**Como** paciente  
**Quiero** iniciar sesión con mi DNI y contraseña  
**Para** acceder a mi cuenta y gestionar mis turnos

**Criterios de aceptación**:
- [ ] CA-1: El sistema autentica con DNI + contraseña
- [ ] CA-2: Tras el login, si el paciente no ha vinculado Gmail, se le solicita hacerlo (una sola vez)

**Reglas relacionadas**: RN-AU-03, RN-AU-04

### US-003 — Recuperación de contraseña
**Como** paciente  
**Quiero** recuperar mi contraseña por Gmail o solicitar un blanqueo presencial en el consultorio  
**Para** recuperar el acceso a mi cuenta si olvido la contraseña

**Criterios de aceptación**:
- [ ] CA-1: El sistema envía un enlace de recuperación al Gmail vinculado
- [ ] CA-2: El paciente puede acudir al consultorio para que Recepción blanquee su contraseña presencialmente

**Reglas relacionadas**: RN-AU-03, RN-AU-05

## Épica 2: Configuración del Consultorio (Administrador)

### US-004 — Alta de profesionales
**Como** administrador  
**Quiero** crear y editar profesionales del consultorio  
**Para** asignarles turnos y horarios de atención

**Criterios de aceptación**:
- [ ] CA-1: El administrador puede crear un profesional con nombre y matrícula
- [ ] CA-2: El administrador puede activar/desactivar profesionales

**Reglas relacionadas**: RN-TU-05

### US-005 — Alta de sillones/boxes
**Como** administrador  
**Quiero** crear y editar sillones/boxes del consultorio  
**Para** gestionar los recursos físicos de atención

**Criterios de aceptación**:
- [ ] CA-1: El administrador puede definir el nombre de cada sillón/box
- [ ] CA-2: El administrador puede activar/desactivar sillones/boxes

**Reglas relacionadas**: RN-TU-05

### US-006 — Catálogo de prestaciones
**Como** administrador  
**Quiero** crear y editar prestaciones con duración variable en minutos  
**Para** que el sistema calcule automáticamente la duración de cada turno

**Criterios de aceptación**:
- [ ] CA-1: Cada prestación tiene nombre y duración en minutos (> 0)
- [ ] CA-2: Ejemplos: Consulta 30 min, Limpieza 30 min, Obturación 45 min, Endodoncia 60 min

**Reglas relacionadas**: RN-TU-05, RN-TU-03

### US-007 — Horarios y bloqueos
**Como** administrador  
**Quiero** configurar horarios de atención y bloqueos por profesional y por sillón/box  
**Para** definir cuándo se puede atender y cuándo no hay disponibilidad

**Criterios de aceptación**:
- [ ] CA-1: El administrador define horarios de atención por profesional y día de semana
- [ ] CA-2: El administrador puede crear bloqueos (franjas no disponibles) por profesional y/o sillón

**Reglas relacionadas**: RN-TU-03

## Épica 3: Gestión de Turnos

### US-008 — Consulta de disponibilidad
**Como** paciente o personal del consultorio  
**Quiero** consultar los horarios disponibles para un profesional y sillón en una fecha  
**Para** saber cuándo se puede reservar un turno

**Criterios de aceptación**:
- [ ] CA-1: El sistema devuelve horarios libres considerando horarios de atención, bloqueos y turnos existentes
- [ ] CA-2: La duración del bloque se calcula según la prestación seleccionada

**Reglas relacionadas**: RN-TU-03

### US-009 — Creación de turno (autogestión online)
**Como** paciente  
**Quiero** reservar un turno online eligiendo profesional, sillón/box, prestación y fecha/hora  
**Para** gestionar mi turno sin llamar al consultorio

**Criterios de aceptación**:
- [ ] CA-1: El paciente solo puede reservar en horarios disponibles (sin solapamiento)
- [ ] CA-2: El turno se crea en estado RESERVADO
- [ ] CA-3: El sistema envía un email de confirmación al Gmail vinculado

**Reglas relacionadas**: RN-TU-01, RN-TU-02, RN-TU-03, RN-NT-01

### US-010 — Creación de turno (personal del consultorio)
**Como** recepcionista u odontólogo  
**Quiero** crear un turno asignando paciente, profesional, sillón/box, prestación y fecha/hora  
**Para** gestionar la agenda del consultorio

**Criterios de aceptación**:
- [ ] CA-1: El personal puede buscar pacientes por DNI
- [ ] CA-2: El sistema valida anti-solapamiento por profesional y sillón/box
- [ ] CA-3: El turno se crea en estado RESERVADO

**Reglas relacionadas**: RN-TU-01, RN-TU-02, RN-TU-03

### US-011 — Reprogramación de turno
**Como** paciente, recepcionista u odontólogo  
**Quiero** reprogramar un turno existente a otro horario disponible  
**Para** cambiar la cita sin perderla

**Criterios de aceptación**:
- [ ] CA-1: El nuevo horario debe respetar anti-solapamiento por profesional y sillón/box
- [ ] CA-2: Al reprogramar, el hueco anterior se libera inmediatamente

**Reglas relacionadas**: RN-TU-01, RN-TU-02, RN-TU-03, RN-TU-04

### US-012 — Cancelación de turno
**Como** paciente, recepcionista u odontólogo  
**Quiero** cancelar un turno existente  
**Para** liberar el horario para otro paciente

**Criterios de aceptación**:
- [ ] CA-1: El estado del turno pasa a CANCELADO (historial conservado, nunca borrado físico)
- [ ] CA-2: El hueco del turno cancelado vuelve a figurar disponible de inmediato

**Reglas relacionadas**: RN-TU-04

## Épica 4: Notificaciones

### US-013 — Recordatorio automático por email
**Como** sistema  
**Quiero** enviar recordatorios automáticos por email al Gmail vinculado del paciente  
**Para** reducir inasistencias y permitir confirmación de asistencia

**Criterios de aceptación**:
- [ ] CA-1: El sistema envía recordatorios con antelación configurable (horas antes del turno)
- [ ] CA-2: El recordatorio incluye enlace para confirmar asistencia

**Reglas relacionadas**: RN-NT-01, RN-NT-02
