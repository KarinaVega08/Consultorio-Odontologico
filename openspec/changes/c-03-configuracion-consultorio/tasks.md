# Tareas — C-03 configuracion-consultorio

## Tarea 1 — Dominio y persistencia
- [x] Entidades `Profesional`, `SillonBox`, `Prestacion`, `HorarioAtencion`, `Bloqueo`.
- [x] Repositorios JPA con consultas por `activo`, profesional y sillón.
- [x] Test `@DataJpaTest` de persistencia y baja lógica.

## Tarea 2 — Servicio y reglas de negocio
- [x] DTOs request/response con Bean Validation.
- [x] `ConfiguracionConsultorioService` con CRUD, activar/desactivar y validaciones.
- [x] Tests unitarios de reglas de negocio y recursos inexistentes.

## Tarea 3 — API y seguridad
- [x] `AdminConfigController` con endpoints `/api/admin/professionals`, `consulting-rooms`, `services`, `schedules`, `blockers`.
- [x] `@PreAuthorize("hasRole('ADMINISTRADOR')")` y `@EnableMethodSecurity`.
- [x] Tests `@WebMvcTest` de 201/400/403 y errores estructurados.

## Tarea 4 — Cobertura CRUD completa
- [x] `PUT /api/admin/schedules/{id}` y `PUT /api/admin/blockers/{id}` (edición de horarios y bloqueos).
- [x] Tests de repositorio para los 5 recursos (activos, baja lógica y filtros).
- [x] Tests de servicio: alta/edición/listado/baja de cada recurso y 404 de recursos inexistentes.
- [x] Tests `@WebMvcTest` de `GET/PUT/PATCH` y RBAC por recurso.

## Tarea 5 — Verificación y cierre
- [x] `./gradlew test` en verde con H2.
- [x] Marcar checkbox de C-03 en `CHANGES.md`.
- [x] Copiar spec vigente y archivar el change.
