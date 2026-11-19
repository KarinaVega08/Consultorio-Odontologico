# Odontológico — Gestión de Turnos — Instrucciones para Agentes

## Stack Tecnológico

| Capa | Tecnologías | Versión mínima |
|------|-------------|----------------|
| Backend | Java, Spring Boot 3, Gradle, JPA/Hibernate, API REST | Java 21, Spring Boot 3.x |
| Backend - Base de datos (ejecución) | PostgreSQL | Local |
| Backend - Base de datos (tests) | H2 en memoria | - |
| Frontend | TypeScript Vanilla (servido estático) | - |
| Email | SMTP / Gmail (desarrollo: Mailpit o mock SMTP) | - |
| Despliegue | Entorno reproducible local, sin Docker obligatorio | - |

## Base de Conocimiento

La fuente de verdad del proyecto vive en `knowledge-base/`:

| Archivo | Contenido |
|---------|-----------|
| [01_vision_y_objetivos.md](knowledge-base/01_vision_y_objetivos.md) | Propósito, objetivos por actor, alcance v1.0, fuera de alcance, métricas de éxito |
| [02_descripcion_general.md](knowledge-base/02_descripcion_general.md) | Stack tecnológico, arquitectura general, integraciones externas, API REST |
| [03_actores_y_roles.md](knowledge-base/03_actores_y_roles.md) | Actores del sistema, matriz RBAC de permisos, rutas públicas |
| [04_modelo_de_datos.md](knowledge-base/04_modelo_de_datos.md) | Dominios, ERD, entidades, atributos, constraints, seed data |
| [05_reglas_de_negocio.md](knowledge-base/05_reglas_de_negocio.md) | Reglas codificadas por dominio (RN-XX) con códigos únicos |
| [06_funcionalidades.md](knowledge-base/06_funcionalidades.md) | Épicas y historias de usuario (US-NNN) con criterios de aceptación |
| [07_flujos_principales.md](knowledge-base/07_flujos_principales.md) | Flujos extremo a extremo (registro, reserva, creación por personal, cancelación, blanqueo) |
| [08_arquitectura_propuesta.md](knowledge-base/08_arquitectura_propuesta.md) | Patrones, estructura de directorios, seguridad, variables de entorno |
| [09_decisiones_y_supuestos.md](knowledge-base/09_decisiones_y_supuestos.md) | Decisiones documentadas (DD-XX) y supuestos inferidos (SU-XX) |
| [10_preguntas_abiertas.md](knowledge-base/10_preguntas_abiertas.md) | Inconsistencias detectadas y preguntas abiertas priorizadas |
| [README.md](knowledge-base/README.md) | Índice, quick start para desarrolladores, resumen ejecutivo |

## Skills Disponibles

Las skills instaladas y sus compact rules viven en `.atl/skill-registry.md` (generado por `skill-registry`; no versionado — no está en el repo). El orquestador resuelve los compact rules en runtime e inyecta en cada sub-agent prompt.

| Agente | Rol | Skills que carga |
|--------|-----|------------------|
| Backend Core | Java 21 + Spring Boot 3, JPA/Hibernate, API REST, seguridad | java-springboot |
| Backend Testing | JUnit 5, Mockito, Testcontainers, slice/full-context tests | spring-boot-test-patterns |
| Orquestación | OPSX/SDD, openspec CLI, fundación del proyecto | active-orchestrator, kb-creator, roadmap-generator, agent-instruction |

Los compact rules de cada skill los resuelve el orquestador desde `.atl/skill-registry.md` (generado por `skill-registry`; no versionado — no está en el repo).

## Roadmap de Changes

Ver `CHANGES.md` en la raíz para el detalle completo.

**6 changes en 4 fases:**

- **C-01 setup-base** (Fase 1, CRÍTICO/riesgo MEDIO): Java 21 + Spring Boot 3 + Gradle + PostgreSQL/H2, estructura por capas, seguridad inicial/RBAC. Ejecutan primero los líderes; suben la base limpia a `main`.
- **Oleada 1 (GATE 1 FORK)** — 3 ramas paralelas sin colisiones de archivos (dependen solo de C-01):
  - C-02 auth-pacientes (registro/login DNI+contraseña, Gmail de recuperación, blanqueo presencial)
  - C-03 configuracion-consultorio (CRUD profesionales, sillones/boxes, horarios, bloqueos, catálogo de prestaciones)
  - C-04 disponibilidad-turnos (cálculo de franjas libres por profesional, sillón/box y duración de prestación)
- **C-05 gestion-turnos** (Oleada 2, GATE 2, CRÍTICO/riesgo ALTO): crear, reprogramar y cancelar turnos evitando solapamientos por profesional y sillón/box (incluyendo turno consecutivo borde a borde). Depende de C-02, C-03, C-04.
- **C-06 notificaciones** (Fase 4, ALTO/riesgo MEDIO): recordatorios automáticos por email (SMTP/Mailpit) y confirmación/cancelación mediante enlace único. Depende de C-05 y C-02.

**Camino crítico:** `C-01 → C-02 → C-05 → C-06` (4 changes, mínimo irreducible).

**Primer change sugerido:** `/opsx:propose C-01-setup-base`

## Reglas Duras (específicas del proyecto)

Reglas globales ya definidas en `~/.claude/CLAUDE.md` (orquestador, governance, TDD, engram): el proyecto las hereda. Acá viven solo las reglas **específicas de este proyecto** (stack-aware, confirmadas por el usuario).

**Reglas del stack Java 21 + Spring Boot 3:**

- Usar Gradle (`build.gradle`) como herramienta de build
- Usar Spring Boot starters (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`) para dependencias
- Organizar código por feature/domain (ej. `com.example.app.turno`), no por capa
- `application.yml` para configuración externa; `@ConfigurationProperties` para objetos fuertemente tipados
- Spring Profiles (`application-dev.yml`, `application-prod.yml`) para config por entorno
- Nunca hardcodear secrets — variables de entorno o secret manager (HashiCorp Vault, AWS Secrets Manager)
- Spring Security para autenticación y autorización; BCrypt para encoding de contraseñas
- Prevenir SQL injection con Spring Data JPA o queries parametrizadas; prevenir XSS codificando output
- Endpoints RESTful claros y consistentes bajo `/api/...`
- Usar DTOs para exponer/consumir datos en la API — **NUNCA exponer entidades JPA directamente al cliente**
- Validar payloads de request con Java Bean Validation (`@Valid`, `@NotNull`, `@Size`) en DTOs
- Manejo global de errores con `@ControllerAdvice` + `@ExceptionHandler`
- Encapsular lógica de negocio en `@Service`; servicios stateless
- Usar `@Transactional` en métodos de servicio, en el nivel más granular necesario
- Usar Spring Data JPA repositories (`JpaRepository`, `CrudRepository`); `@Query` o Criteria API para queries complejas
- Usar proyecciones DTO para traer solo los datos necesarios de la base
- Logging con SLF4J; declarar `private static final Logger`; usar mensajes parametrizados (`logger.info("... {}", id)`)
- `@DataJpaTest` para repositorios, `@WebMvcTest` para controladores, `@SpringBootTest` solo para integración completa
- Preferir `@ServiceConnection` en Spring Boot 3.5+ sobre `@DynamicPropertySource` para Testcontainers
- Nunca usar `@DirtiesContext` a menos que sea absolutamente necesario (fuerza reconstrucción de contexto)
- Tests determinísticos: inicializar todos los datos de prueba explícitamente en `@BeforeEach`
- Organizar tests por capa para maximizar cacheo de contexto
- Reusar Testcontainers a nivel JVM (`withReuse(true)` + `TESTCONTAINERS_REUSE_ENABLE=true`)
- No confiar en el orden de ejecución de tests; cada test debe ser independiente
- Performance objetivo: unit < 50ms, slice < 100ms, integración < 500ms

**Reglas específicas del dominio (confirmadas por el usuario):**

1. **Privacidad de datos sensibles de salud (Ley 25.326 Argentina):** Nunca almacenar datos reales de pacientes (nombres, DNIs, correos o teléfonos reales) en el repositorio, scripts de carga, ejemplos ni tests. Usar exclusivamente datos ficticios (ej. DNI 99.999.999, Juan Pérez).
2. **Integridad de agenda y concurrencia:** Toda creación o reprogramación de turnos debe validar en transacción (`@Transactional`) la ausencia de solapamientos tanto por profesional como por sillón/box. Se permiten turnos consecutivos borde a borde (uno inicia exactamente cuando termina el anterior).
3. **Trazabilidad de turnos:** Nunca realizar borrado físico (`DELETE`) de turnos. Al cancelar un turno, pasa a estado `CANCELADO` y libera automáticamente el horario para nuevas reservas.
4. **Tests autocontenidos:** Toda la suite de tests automatizados (JUnit 5 + MockMvc/SpringBootTest) debe correr en verde con `./gradlew test` usando H2 en memoria, sin requerir PostgreSQL ni servicios externos levantados.

## Flujo de Trabajo

`KB → CHANGES → /opsx:propose → archive`

1. Leer `knowledge-base/` (fuente de verdad del dominio y arquitectura).
2. Leer `CHANGES.md` para identificar el change pendiente cuyas dependencias estén ya cumplidas.
3. Ejecutar `/opsx:propose C-NN-nombre-del-change` con el scope detallado.
4. Implementar, probar y marcar el checkbox del change como `[x]`.
5. Ejecutar `/opsx:archive C-NN-nombre-del-change` y continuar con el siguiente change desbloqueado.
