# Archivo de Cierre — C-03 configuracion-consultorio

## Resumen
Change C-03 completado en rama `feat/c03-configuracion-consultorio`. Configuración administrativa del consultorio con profesionales, sillones/boxes, prestaciones de duración variable, horarios de atención y bloqueos.

## Artefactos generados
- `backend/src/main/java/com/consultorio/admin/domain/model/` — 5 entidades sobre `BaseEntity` (baja lógica).
- `backend/src/main/java/com/consultorio/admin/domain/repository/` — 5 repositorios JPA.
- `backend/src/main/java/com/consultorio/admin/application/dto/` — DTOs request/response con Bean Validation.
- `backend/src/main/java/com/consultorio/admin/application/service/ConfiguracionConsultorioService.java` — reglas RN-TU-03/RN-TU-05.
- `backend/src/main/java/com/consultorio/admin/api/controller/AdminConfigController.java` — endpoints `/api/admin/**`, incluye `PUT` de horarios y bloqueos.
- `backend/src/main/java/com/consultorio/infrastructure/config/AdminSecurityConfig.java` — `@EnableMethodSecurity` sin tocar `SecurityConfig`.
- `backend/src/main/java/com/consultorio/infrastructure/config/GlobalExceptionHandler.java` — manejo de `AccessDeniedException` → 403.
- `backend/build.gradle` — `spring-security-test` para tests de RBAC.
- `backend/src/test/resources/application.yml` — `spring.sql.init.mode: never` (data.sql es solo comentarios).
- `openspec/specs/C-03-configuracion-consultorio/spec.md` y artefactos del change.

## Tests (Strict TDD, todos en verde)
- `ConfiguracionConsultorioServiceTest` — duración > 0, día 1-7, hora fin > inicio, bloqueo con referencia, baja lógica.
- `ConfiguracionConsultorioCrudTest` — alta/edición/listado/baja de los 5 recursos y 404 de recursos inexistentes.
- `PrestacionRepositoryTest` + `AdminRepositoriesTest` — `@DataJpaTest` con H2 para los 5 repositorios (activos, filtros y baja lógica).
- `AdminConfigControllerTest` + `AdminConfigCrudEndpointsTest` — `@WebMvcTest` + RBAC: 201/200/400/403 en `POST/GET/PUT/PATCH`.
- `./gradlew test` → BUILD SUCCESSFUL (42 tests de C-03, 66 en total, H2 en memoria).

## Estado
`C-03-configuracion-consultorio` marcado como `[x]` en `CHANGES.md`. Habilita C-04 (disponibilidad) y C-05 (gestión de turnos).
