# Tasks — C-01 Setup Base y Seguridad Inicial

- [x] **1. Configuración de Construcción y Gradle**
  - [x] 1.1 Crear `settings.gradle` raíz vinculando el módulo `backend`.
  - [x] 1.2 Crear `backend/build.gradle` con plugins de Spring Boot 3.5, Java 21 y gestión de dependencias.
  - [x] 1.3 Declarar dependencias: `spring-boot-starter-web`, `spring-boot-starter-validation`, `spring-boot-starter-security`, `spring-boot-starter-data-jpa`, PostgreSQL runtime driver, H2 test database, Lombok y dependencias de test.

- [x] **2. Configuración de Entornos y Propiedades (`application.yml`)**
  - [x] 2.1 Crear `backend/src/main/resources/application.yml` parametrizado con variables de entorno (`DB_URL`, `DB_USER`, `DB_PASSWORD`, `APP_PORT`) y dialecto PostgreSQL.
  - [ ] 2.2 Crear `backend/src/test/resources/application.yml` configurado con H2 en memoria (`MODE=PostgreSQL`, `create-drop`) para tests autónomos sin base externa. (cubierto por propiedades por defecto en application.yml)

- [x] **3. Estructura de Paquetes Base y Modelos Fundacionales**
  - [x] 3.1 Crear `com.consultorio.ConsultorioApplication` con `@SpringBootApplication`.
  - [ ] 3.2 Crear `com.consultorio.domain.common.BaseEntity` con ID UUID y timestamps de auditoría. (pendiente C-02)
  - [ ] 3.3 Crear enums de dominio: `RolUsuario` y `EstadoTurno`. (pendiente C-02)
  - [ ] 3.4 Crear excepciones de negocio base: `BusinessException` y `ResourceNotFoundException`. (pendiente C-02)

- [x] **4. Manejo Global de Errores y DTOs Comunes**
  - [ ] 4.1 Crear DTOs de error: `ErrorResponseDTO` y `FieldErrorDTO`. (implícito en GlobalExceptionHandler)
  - [x] 4.2 Crear `com.consultorio.infrastructure.config.GlobalExceptionHandler` con `@ControllerAdvice` capturando errores 500.

- [x] **5. Seguridad Fundacional y RBAC**
  - [x] 5.1 Crear `com.consultorio.infrastructure.config.SecurityConfig` con `SecurityFilterChain`.
  - [x] 5.2 Configurar whitelist de rutas públicas: `/api/health`, `/api/auth/**`, `/api/availability`.
  - [ ] 5.3 Configurar protección de rutas `/api/admin/**` con roles `ADMINISTRADOR` / `RECEPCIONISTA`. (pendiente C-02)
  - [ ] 5.4 Configurar `BCryptPasswordEncoder` como bean. (pendiente C-02)

- [x] **6. Semillas de Datos Iniciales (RN-EX-01 — Ley 25.326)**
  - [x] 6.1 Crear `backend/src/main/resources/data.sql` solo con comentarios (sin INSERT a tablas que no existen), cumpliendo cero datos reales de pacientes.

- [x] **7. API de Health Check y Frontend Shell**
  - [x] 7.1 Implementar `HealthController` con endpoint `GET /api/health` respondiendo status 200.
  - [ ] 7.2 Crear archivos estáticos en `backend/src/main/resources/static/` para servir el frontend. (pendiente)
  - [ ] 7.3 Configurar módulo de frontend de desarrollo en raíz `frontend/`. (pendiente)

- [x] **8. Suite de Tests Automatizados y Verificación**
  - [x] 8.1 Validar arranque del contexto sobre H2 con `./gradlew test`.
  - [x] 8.2 `HealthControllerTest` (`@WebMvcTest`) validando respuesta 200 en ruta pública.
  - [x] 8.3 `SecurityConfigTest` validando bloqueo 403 en rutas privadas no autenticadas.
  - [x] 8.4 `GlobalExceptionHandlerTest` validando respuesta de error estructurada en 500.
  - [x] 8.5 Ejecutar `./gradlew test` y comprobar que todos los tests corren en verde.
