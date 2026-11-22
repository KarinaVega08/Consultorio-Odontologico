# Tasks — C-01 Setup Base y Seguridad Inicial

- [x] **1. Configuración de Construcción y Gradle**
  - [x] 1.1 Crear `settings.gradle` raíz vinculando el módulo `backend`.
  - [x] 1.2 Crear `backend/build.gradle` con plugins de Spring Boot 3.5, Java 21 y gestión de dependencias.
  - [x] 1.3 Declarar dependencias: `spring-boot-starter-web`, `spring-boot-starter-validation`, `spring-boot-starter-security`, `spring-boot-starter-data-jpa`, PostgreSQL runtime driver, H2 test database, Lombok y dependencias de test.

- [x] **2. Configuración de Entornos y Propiedades (`application.yml`)**
  - [x] 2.1 Crear `backend/src/main/resources/application.yml` parametrizado con variables de entorno (`DB_URL`, `DB_USER`, `DB_PASSWORD`, `APP_PORT`) y dialecto PostgreSQL.
  - [x] 2.2 H2 en memoria configurado vía propiedades por defecto en `application.yml` (tests autónomos sin base externa).

- [x] **3. Estructura de Paquetes Base y Clase Principal**
  - [x] 3.1 Crear `com.consultorio.ConsultorioApplication` con `@SpringBootApplication`.
  - [x] 3.2 Crear paquetes base: `domain/model`, `domain/repository`, `application/service`, `application/dto`, `infrastructure/config`, `infrastructure/persistence`, `api/controller`, `api/dto` (con `package-info.java`).

- [x] **4. Manejo Global de Errores**
  - [x] 4.1 Crear `com.consultorio.infrastructure.config.GlobalExceptionHandler` con `@ControllerAdvice` capturando errores 500 (respuesta JSON estructurada).

- [x] **5. Seguridad Fundacional y RBAC**
  - [x] 5.1 Crear `com.consultorio.infrastructure.config.SecurityConfig` con `SecurityFilterChain`.
  - [x] 5.2 Configurar whitelist de rutas públicas: `/api/health`, `/api/auth/**`, `/api/availability`.
  - [x] 5.3 CSRF deshabilitado (API stateless). Rutas restantes requieren autenticación.

- [x] **6. Semillas de Datos Iniciales (RN-EX-01 — Ley 25.326)**
  - [x] 6.1 Crear `backend/src/main/resources/data.sql` solo con comentarios (sin INSERT a tablas que no existen), cumpliendo cero datos reales de pacientes.

- [x] **7. API de Health Check**
  - [x] 7.1 Implementar `HealthController` con endpoint `GET /api/health` respondiendo status 200.
  - [x] 7.2 Frontend shell (TypeScript Vanilla servido estático) queda para fase posterior; `application.yml` define `app.frontend-path`.

- [x] **8. Suite de Tests Automatizados y Verificación**
  - [x] 8.1 Validar arranque del contexto sobre H2 con `./gradlew test`.
  - [x] 8.2 `HealthControllerTest` (`@WebMvcTest`) validando respuesta 200 en ruta pública.
  - [x] 8.3 `SecurityConfigTest` validando bloqueo 403 en rutas privadas no autenticadas.
  - [x] 8.4 `GlobalExceptionHandlerTest` validando respuesta de error estructurada en 500 (TestExceptionController vía `@TestConfiguration` solo en tests).
  - [x] 8.5 Ejecutar `./gradlew test` y comprobar que todos los tests corren en verde.
