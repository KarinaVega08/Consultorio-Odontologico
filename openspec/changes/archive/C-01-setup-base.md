# Archivo de Cierre — C-01 setup-base

## Resumen
Change C-01 (setup-base) completado en rama `feat/c01-setup-base`. Esqueleto del backend Java 21 + Spring Boot 3 + Gradle con seguridad inicial RBAC y base de datos PostgreSQL/H2.

## Artefactos generados
- `backend/build.gradle`, `backend/settings.gradle` (Spring Boot 3.5.0, Java 21)
- `backend/src/main/java/com/consultorio/ConsultorioApplication.java` (`@SpringBootApplication`)
- `backend/src/main/resources/application.yml` (variables de entorno: DB_URL, SMTP_*, APP_PORT, etc.)
- `backend/src/main/resources/data.sql` (solo comentarios, Ley 25.326: cero datos reales)
- `backend/src/main/java/com/consultorio/api/controller/HealthController.java` (`GET /api/health`)
- `backend/src/main/java/com/consultorio/infrastructure/config/SecurityConfig.java` (whitelist rutas públicas, CSRF deshabilitado)
- `backend/src/main/java/com/consultorio/infrastructure/config/GlobalExceptionHandler.java` (`@ControllerAdvice`, errores 500)
- Paquetes base bajo `com.consultorio` (domain, application, infrastructure, api) con `package-info.java`

## Tests (Strict TDD, todos en verde)
- `HealthControllerTest` — `/api/health` → 200
- `SecurityConfigTest` — rutas públicas permitadas, rutas privadas bloqueadas (403)
- `GlobalExceptionHandlerTest` — excepción → 500 con cuerpo JSON estructurado (`TestExceptionController` vía `@TestConfiguration` solo en tests)
- `./gradlew test` → BUILD SUCCESSFUL (4 tests, H2 en memoria)

## Estado
`C-01-setup-base` marcado como `[x]` completado en `CHANGES.md`. Listo para iniciar C-02 (`auth-pacientes`).
