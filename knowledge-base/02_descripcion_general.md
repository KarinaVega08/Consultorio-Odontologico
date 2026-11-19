# Descripción General

## Stack tecnológico

| Capa | Tecnologías | Versión mínima |
|------|-------------|----------------|
| Backend | Java, Spring Boot 3, Gradle, JPA/Hibernate, API REST | Java 21, Spring Boot 3.x |
| Backend - Base de datos (ejecución) | PostgreSQL | Local |
| Backend - Base de datos (tests) | H2 en memoria | - |
| Frontend | TypeScript Vanilla (servido estático) | - |
| Email | SMTP / Gmail (desarrollo: Mailpit o mock SMTP) | - |
| Despliegue | Entorno reproducible local, sin Docker obligatorio | - |

## Arquitectura general

Monolito Spring Boot 3 que expone una API REST bajo `/api/...` y sirve los archivos estáticos del frontend en TypeScript Vanilla desde el mismo proceso. Un solo proceso para levantar y desplegar, sin problemas de CORS. La arquitectura sigue capas (domain, application, infrastructure) dentro del backend. Base de datos PostgreSQL para ejecución local y H2 en memoria para tests automatizados.

## Integraciones externas

| Servicio | Propósito | Tipo |
|----------|-----------|------|
| SMTP / Gmail | Recuperación de contraseña y recordatorios de turno | SMTP (webhook n/a) |
| Mailpit / mock SMTP | Envío de email en entorno de desarrollo/pruebas | SMTP (local, sin costo) |

## API REST (si aplica)

Endpoints principales agrupados por recurso:

- `POST /api/auth/register` — registro de paciente (nombre, DNI, contraseña)
- `POST /api/auth/login` — login con DNI + contraseña
- `POST /api/auth/recover-password` — solicitud de recuperación por Gmail
- `POST /api/auth/reset-password` — restablecimiento de contraseña con token
- `GET /api/patients/me` — perfil del paciente autenticado
- `GET /api/availability` — consulta de horarios disponibles (profesional, sillón, fecha, prestación)
- `POST /api/appointments` — creación de turno (paciente online o personal del consultorio)
- `PUT /api/appointments/{id}` — reprogramación de turno
- `DELETE /api/appointments/{id}` — cancelación de turno (estado CANCELADO, hueco liberado)
- `GET /api/appointments` — listado de turnos (filtrable por profesional, fecha, estado)
- `POST /api/admin/professionals` — alta/edición de profesionales (solo Admin)
- `POST /api/admin/consulting-rooms` — alta/edición de sillones/boxes (solo Admin)
- `POST /api/admin/services` — alta/edición de prestaciones con duración (solo Admin)
- `POST /api/admin/password-reset` — blanqueo presencial de contraseña (solo Recepcionista/Admin)
