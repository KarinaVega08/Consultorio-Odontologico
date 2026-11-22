# Tareas — C-01 setup-base

## Tarea 1 — Esqueleto del proyecto y build
- [ ] Crear carpeta `backend/` con `build.gradle` y `settings.gradle`.
- [ ] Agregar dependencias: Spring Web, Spring Data JPA, Spring Security, Validation, Lombok, PostgreSQL driver, H2 (test).
- [ ] Crear carpeta `frontend/` con `package.json` y `index.html` placeholder.
- [ ] Verificar que `./gradlew build` compila sin errores.

## Tarea 2 — Estructura de paquetes por capa
- [ ] Crear `com.consultorio.domain` (interfaces de repositorio, entidades placeholder).
- [ ] Crear `com.consultorio.application` (servicios, DTOs placeholder).
- [ ] Crear `com.consultorio.infrastructure` (implementaciones JPA, configuración).
- [ ] Crear `com.consultorio.api` (controladores REST placeholder).

## Tarea 3 — Configuración externa y variables de entorno
- [ ] Crear `src/main/resources/application.yml`.
- [ ] Definir placeholders para DB_URL, DB_USER, DB_PASSWORD.
- [ ] Definir placeholders para SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASSWORD.
- [ ] Definir placeholders para MAILPIT_HOST, MAILPIT_PORT, APP_PORT, FRONTEND_PATH.
- [ ] Crear `application-dev.yml` y `application-prod.yml` (perfiles de Spring).

## Tarea 4 — Base de datos y seed ficticio
- [ ] Configurar PostgreSQL como datasource por defecto.
- [ ] Configurar H2 en memoria para perfil de tests.
- [ ] Crear `data.sql` con seed data ficticia (DNI 99.999.999, Juan Pérez, roles de ejemplo).
- [ ] Verificar que no hay datos reales de pacientes (Ley 25.326, RN-EX-01).

## Tarea 5 — Seguridad inicial RBAC
- [ ] Configurar Spring Security con autenticación básica.
- [ ] Definir roles: Paciente, Recepcionista, Odontólogo, Administrador.
- [ ] Preparar BCrypt para encoding de contraseñas (uso real en C-02).
- [ ] No hardcodear secrets; usar variables de entorno.

## Tarea 6 — Healthcheck y smoke test
- [ ] Agregar endpoint `/actuator/health` o similar (opcional).
- [ ] Escribir un test de contexto que levante Spring Boot con H2.
- [ ] Ejecutar `./gradlew test` y confirmar que pasa en verde.

## Criterio de finalización
Todos los checkboxes marcados `[x]`, `./gradlew build` y `./gradlew test` en verde, y base lista para C-02, C-03, C-04.