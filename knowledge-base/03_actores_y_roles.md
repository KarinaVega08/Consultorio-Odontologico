# Actores y Roles

## Actores del sistema

| Actor | Descripción | Cómo interactúa |
|-------|-------------|-----------------|
| Paciente | Persona que se registra con nombre real, DNI y contraseña; vincula Gmail de recuperación una sola vez; consulta disponibilidad y reserva/confirma/cancela sus turnos | Frontend web (autogestión online) |
| Recepcionista / Secretaria | Gestiona la agenda del consultorio: crea, reprograma y cancela turnos; controla asistencia; blanquea contraseñas presencialmente | Frontend web (panel de personal) |
| Odontólogo | Gestiona su propia agenda diaria/semanal, registra la atención y consulta sus turnos asignados. En consultorios unipersonales, puede asumir también el rol de Administrador | Frontend web (panel de profesional) |
| Administrador | Configura el consultorio (profesionales, sillones/boxes, horarios, bloqueos y catálogo de prestaciones) y supervisa la agenda global | Frontend web (panel de administración) |

## RBAC — Matriz de permisos

| Rol | Recurso | Permisos (CRUD) |
|-----|---------|-----------------|
| Paciente | Propio perfil | Leer, Actualizar (datos de contacto, Gmail) |
| Paciente | Propios turnos | Crear, Leer, Actualizar (reprogramar), Cancelar |
| Paciente | Propia recuperación de contraseña | Solicitar (por Gmail) |
| Recepcionista | Turnos (todos) | Crear, Leer, Actualizar, Cancelar |
| Recepcionista | Pacientes | Leer (buscar por DNI), Actualizar contraseña (blanqueo presencial) |
| Recepcionista | Agenda global | Leer |
| Odontólogo | Propios turnos | Leer, Actualizar (registrar atención) |
| Odontólogo | Pacientes (asignados) | Leer (historial, datos de contacto) |
| Odontólogo | Agenda propia | Leer |
| Administrador | Profesionales | Crear, Leer, Actualizar, Desactivar |
| Administrador | Sillones/boxes | Crear, Leer, Actualizar, Desactivar |
| Administrador | Prestaciones (catálogo) | Crear, Leer, Actualizar, Desactivar |
| Administrador | Horarios y bloqueos | Crear, Leer, Actualizar |
| Administrador | Agenda global | Leer, Supervisar |
| Administrador | Blanqueo de contraseñas | Ejecutar (presencial) |

## Rutas públicas

- `GET /api/availability` (consulta pública de horarios, sin autenticación — para el widget de reserva del paciente no autenticado, opcional)
- Página de registro público (`POST /api/auth/register`)
- Página de login público (`POST /api/auth/login`)
- Página de recuperación de contraseña pública (`POST /api/auth/recover-password`)

Todas las demás rutas requieren autenticación (DNI + contraseña o sesión activa).
