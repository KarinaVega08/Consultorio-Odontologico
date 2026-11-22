# Tasks — C-01 Setup Base y Seguridad Inicial

- [ ] **1. Configuración de Construcción y Gradle**
  - [ ] 1.1 Crear `settings.gradle` raíz vinculando el módulo `backend`.
  - [ ] 1.2 Crear `backend/build.gradle` con plugins de Spring Boot 3.3.x/3.4.x, Java 21 y gestión de dependencias.
  - [ ] 1.3 Declarar dependencias: `spring-boot-starter-web`, `spring-boot-starter-validation`, `spring-boot-starter-security`, `spring-boot-starter-data-jpa`, PostgreSQL runtime driver, H2 test database, Lombok y dependencias de test.

- [ ] **2. Configuración de Entornos y Propiedades (`application.yml`)**
  - [ ] 2.1 Crear `backend/src/main/resources/application.yml` parametrizado con variables de entorno (`DB_URL`, `DB_USER`, `DB_PASSWORD`, `APP_PORT`) y dialecto PostgreSQL.
  - [ ] 2.2 Crear `backend/src/test/resources/application.yml` configurado con H2 en memoria (`MODE=PostgreSQL`, `create-drop`) para tests autónomos sin base externa.

- [ ] **3. Estructura de Paquetes Base y Modelos Fundacionales**
  - [ ] 3.1 Crear `com.consultorio.ConsultorioApplication` con `@SpringBootApplication`.
  - [ ] 3.2 Crear `com.consultorio.domain.common.BaseEntity` con ID UUID y timestamps de auditoría.
  - [ ] 3.3 Crear enums de dominio: `RolUsuario` (`PACIENTE`, `RECEPCIONISTA`, `ODONTOLOGO`, `ADMINISTRADOR`) y `EstadoTurno` (`RESERVADO`, `CONFIRMADO`, `CANCELADO`, `COMPLETADO`).
  - [ ] 3.4 Crear excepciones de negocio base: `BusinessException` y `ResourceNotFoundException`.

- [ ] **4. Manejo Global de Errores y DTOs Comunes**
  - [ ] 4.1 Crear DTOs de error: `ErrorResponseDTO` y `FieldErrorDTO`.
  - [ ] 4.2 Crear `com.consultorio.api.common.GlobalExceptionHandler` con `@RestControllerAdvice` capturando validaciones, excepciones de negocio, recursos no encontrados y errores 500.

- [ ] **5. Seguridad Fundacional y RBAC**
  - [ ] 5.1 Crear `com.consultorio.infrastructure.config.SecurityConfig` con `SecurityFilterChain`.
  - [ ] 5.2 Configurar whitelist de rutas públicas: `/api/health`, `/api/auth/**`, `/api/availability`, static assets.
  - [ ] 5.3 Configurar protección de rutas `/api/admin/**` con roles `ADMINISTRADOR` / `RECEPCIONISTA` y el resto autenticado.
  - [ ] 5.4 Configurar `BCryptPasswordEncoder` como bean.

- [ ] **6. Semillas de Datos Iniciales (RN-EX-01 — Ley 25.326)**
  - [ ] 6.1 Crear `backend/src/main/resources/data.sql` con datos 100% ficticios para sillones/boxes y catálogo de prestaciones con duraciones variables.

- [ ] **7. API de Health Check y Frontend Shell**
  - [ ] 7.1 Implementar `HealthController` con endpoint `GET /api/health` respondiendo status UP.
  - [ ] 7.2 Crear archivos estáticos en `backend/src/main/resources/static/` (`index.html`, `css/styles.css`, `js/app.js`) para servir el frontend desde el mismo proceso.
  - [ ] 7.3 Configurar módulo de frontend de desarrollo en raíz `frontend/` (`package.json`, `tsconfig.json`, `src/app.ts`).

- [ ] **8. Suite de Tests Automatizados y Verificación**
  - [ ] 8.1 Crear `ConsultorioApplicationTests` (`@SpringBootTest`) para validar arranque del contexto sobre H2.
  - [ ] 8.2 Crear `HealthControllerTest` (`@WebMvcTest`) para validar respuesta 200 en ruta pública.
  - [ ] 8.3 Crear `SecurityConfigTest` para validar bloqueo 401/403 en rutas privadas no autenticadas.
  - [ ] 8.4 Ejecutar `./gradlew test` y comprobar que todos los tests corren en verde en < 5 segundos.
