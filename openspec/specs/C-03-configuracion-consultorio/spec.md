# C-03 configuracion-consultorio Specification

## Purpose

Proveer la configuración administrativa del consultorio: profesionales, sillones/boxes, catálogo de prestaciones con duración, horarios de atención y bloqueos, con acceso restringido y baja lógica de los recursos.

## Requirements

### Requirement: Catálogo administrativo
El sistema SHALL permitir gestionar profesionales, sillones/boxes, prestaciones con duración en minutos, horarios de atención y bloqueos mediante endpoints bajo `/api/admin/**`.

#### Scenario: Alta de prestación con duración
- **WHEN** se crea una prestación con duración de 45 minutos
- **THEN** queda disponible para el cálculo de disponibilidad

#### Scenario: Alta de sillón
- **WHEN** se crea un sillón/box
- **THEN** aparece en el catálogo y puede asignarse a turnos

### Requirement: Acceso restringido a administración
Los endpoints administrativos SHALL requerir rol ADMINISTRADOR; los pacientes SHALL NO poder consultarlos ni modificarlos.

#### Scenario: Paciente bloqueado
- **WHEN** un paciente solicita `GET /api/admin/professionals`
- **THEN** el sistema responde `403 Forbidden`

#### Scenario: Administrador autorizado
- **WHEN** un administrador consulta el catálogo administrativo
- **THEN** el sistema responde `200 OK` con los recursos

### Requirement: Baja lógica de los recursos
La eliminación de recursos administrativos SHALL ser lógica, marcando `activo = false`, sin borrado físico.

#### Scenario: Baja de profesional
- **WHEN** se da de baja un profesional
- **THEN** deja de listarse como activo pero permanece en la base de datos

### Requirement: Escenarios verificables del change
Los criterios de aceptación CA-1 a CA-6 del change SHALL haberse verificado en `openspec/changes/c-03-configuracion-consultorio`.

#### Scenario: Criterios cubiertos
- **WHEN** se revisa el archivo de escenarios del change
- **THEN** los seis criterios están documentados y verificados
