# Design — C-03 Configuración del Consultorio

## Estructura
Feature package `com.consultorio.admin` con subpaquetes `domain/model`, `domain/repository`, `application/dto`, `application/service`, `api/controller` e `infrastructure/config`. Se reutilizan `BaseEntity`, `BusinessException`, `ResourceNotFoundException` y `GlobalExceptionHandler` de `C-01`.

## Decisiones
1. **Baja lógica**: todas las entidades heredan `activo` de `BaseEntity`; no se ejecutan `DELETE` físicos.
2. **DTOs**: request records con Bean Validation y response records; las entidades JPA nunca se exponen en la API.
3. **RBAC sin colisiones**: `@EnableMethodSecurity` + `@PreAuthorize("hasRole('ADMINISTRADOR')")` en configuración propia del feature, para no editar `SecurityConfig` (que `C-02` también necesita modificar).
4. **Validaciones de negocio en el servicio**: `duracion_minutos > 0`, día de semana 1-7, `hora_fin > hora_inicio`, `fecha_hora_fin > fecha_hora_inicio` y al menos una referencia en `Bloqueo`. Las violaciones lanzan `BusinessException` (400); recursos inexistentes, `ResourceNotFoundException` (404).
5. **Persistencia**: `JpaRepository` con `@Column(nullable = false)`, `@ManyToOne` a `Profesional`/`SillonBox` y consultas por `activo`, profesional y sillón.
6. **Tests por capa**: `@DataJpaTest` (repositorios), tests unitarios Mockito (servicio) y `@WebMvcTest` (controlador y seguridad por rol). H2 en memoria, sin `@DirtiesContext`.

## Convenciones
SLF4J con mensajes parametrizados, servicios `@Transactional` y stateless, endpoints REST bajo `/api/admin/...`, respuestas 201 en creación y 204 en baja/alta.
