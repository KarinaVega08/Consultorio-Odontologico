# C-01 setup-base Specification

## Purpose

Levantar el esqueleto compilable del proyecto con todas las capas base configuradas, listas para que los cambios siguientes se apoyen sobre ellas sin tocar la infraestructura.

## Requirements

### Requirement: Proyecto compilable con el stack acordado
El sistema SHALL compilar con Java 21, Spring Boot 3.x, Gradle, JPA/Hibernate y API REST, organizado en los paquetes `domain`, `application`, `infrastructure` y `api`.

#### Scenario: Build limpio
- **WHEN** se ejecuta `./gradlew build`
- **THEN** el build termina en verde

### Requirement: Configuración externa y arranque local
La aplicación SHALL arrancar con `application.yml` y placeholders para `DB_URL`, `DB_USER`, `DB_PASSWORD`, `SMTP_*`, `MAILPIT_*` y `APP_PORT`, conectando a PostgreSQL local en ejecución.

#### Scenario: Arranque con variables de entorno
- **GIVEN** un PostgreSQL local disponible
- **WHEN** se inicia la aplicación con `application.yml`
- **THEN** el contexto de Spring arranca sin errores de configuración

### Requirement: Tests autocontenidos con H2
La suite SHALL ejecutarse con H2 en memoria, sin requerir PostgreSQL ni servicios externos levantados.

#### Scenario: Suite verde sin PostgreSQL
- **WHEN** se ejecuta `./gradlew test`
- **THEN** el build termina en verde con 0 fallos

### Requirement: Healthcheck público
El sistema SHALL exponer un endpoint de health check público que permita verificar que el backend está arriba.

#### Scenario: Consulta de salud
- **WHEN** se solicita `GET /api/health` sin token
- **THEN** el sistema responde `200 OK`

### Requirement: Datos ficticios y sin secretos
Los seeds SHALL contener exclusivamente datos ficticios (Ley 25.326) y el repositorio SHALL NO hardcodear secretos: estos se leen de variables de entorno.

#### Scenario: Seed sin datos reales
- **WHEN** se inspeccionan los scripts de carga y los tests
- **THEN** no aparecen nombres, DNIs ni correos reales de pacientes

#### Scenario: Sin secretos en el repositorio
- **WHEN** se inspecciona `application.yml`
- **THEN** las credenciales se leen de variables de entorno, nunca como valores fijos
