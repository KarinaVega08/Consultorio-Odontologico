# Propuesta — C-01 setup-base

## Resumen
Setup base del proyecto Odontológico — Gestión de Turnos. Esqueleto del backend (Java 21 + Spring Boot 3 + Gradle + JPA/Hibernate + API REST) y frontend (TypeScript Vanilla servido estático), con seguridad inicial RBAC y configuración de base de datos (PostgreSQL local / H2 en memoria para tests).

## Alcance del change
- Módulo `backend/` y módulo `frontend/`.
- `build.gradle` / `settings.gradle` con dependencias: Spring Web, Spring Data JPA, Spring Security, PostgreSQL driver, H2 (tests), Lombok, Bean Validation.
- Estructura de carpetas por capas bajo `com.consultorio`: `domain/`, `application/`, `infrastructure/`, `api/`.
- `application.yml` + variables de entorno (DB_URL, DB_USER, DB_PASSWORD, SMTP_*, MAILPIT_*, APP_PORT).
- Base de datos: PostgreSQL local para ejecución, H2 en memoria para tests.
- Seguridad inicial: Spring Security, RBAC por rol (Paciente, Recepcionista, Odontólogo, Administrador), seeds solo con datos ficticios (Ley 25.326, RN-EX-01).

## Dependencias
Ninguna. Es el primer change (GATE 0).

## Governance / Riesgo
- Governance: CRÍTICO (modelos core e infraestructura de seguridad que todo lo demás referencia).
- Riesgo: MEDIO (configuración de conexión DB, versiones Java 21 / SB 3, seguridad base).

## Leer antes
- `knowledge-base/08_arquitectura_propuesta.md` — §Estructura de directorios, §Seguridad, §Variables de entorno
- `knowledge-base/02_descripcion_general.md` — §Stack tecnológico, §Integraciones externas
- `knowledge-base/05_reglas_de_negocio.md` — §RN-EX-01, §RN-EX-02