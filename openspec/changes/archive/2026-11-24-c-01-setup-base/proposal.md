# Proposal — C-01 Setup Base y Seguridad Inicial

## 1. Contexto y Resumen Ejecutivo
El proyecto **Odontológico — Gestión de Turnos** requiere una base arquitectónica robusta, modular y reproducible para dar soporte al MVP definido en la `knowledge-base/`. Este change inicial (**C-01**) establece el esqueleto completo del backend y frontend, configurando las dependencias, perfiles de base de datos (PostgreSQL local para ejecución, H2 en memoria para tests automáticos), capas arquitectónicas, manejo global de errores y la configuración fundacional de seguridad RBAC.

Con este setup, los líderes del proyecto destraban la **Oleada 1 (GATE 1 FORK)**, permitiendo que `C-02` (Auth pacientes), `C-03` (Configuración consultorio) y `C-04` (Disponibilidad de turnos) se desarrollen de manera paralela e independiente sin colisiones de archivos ni dependencias circulares.

## 2. Motivación y Problema a Resolver
Sin una base unificada:
- Los integrantes del equipo podrían usar distintas versiones de dependencias o convenciones de paquetes dispares.
- No existiría una convención común para el manejo de excepciones, DTOs y respuestas de API bajo `/api/...`.
- Los tests automáticos podrían acoplarse a bases de datos externas o contenedores Docker obligatorios, violando la regla dura **RN-EX-02** (tests autocontenidos en verde con H2).
- Se podría violar la Ley 25.326 (**RN-EX-01**) si no se define explícitamente desde el inicio la estrategia de seed data con datos ficticios.

## 3. Alcance Propuesto (In-Scope)
- **Estructura multi-módulo o unificada reproducible**: Raíz con Gradle wrapper, submódulo/carpeta `backend/` y `frontend/` estático.
- **Configuración Gradle (`build.gradle`)**: Java 21, Spring Boot 3.3.x/3.4.x, starters web, data-jpa, security, validation, lombok, PostgreSQL runtime driver y H2 test database.
- **Estructura de paquetes base**: Bajo `com.consultorio`:
  - `domain/` (abstracciones y contratos transversales, enums de roles y estados)
  - `application/` (servicios base, DTOs comunes)
  - `infrastructure/` (configuraciones de seguridad, beans JPA, perfiles de base de datos)
  - `api/` (controladores REST, manejo global de excepciones `@ControllerAdvice`, formato estándar de error)
- **Configuración externa (`application.yml`)**:
  - Perfil por defecto / local: variables de entorno (`DB_URL`, `DB_USER`, `DB_PASSWORD`, `APP_PORT`).
  - Perfil `test`: base H2 en memoria (`create-drop`), ejecución autónoma sin dependencias externas.
  - Servido de frontend estático desde `src/main/resources/static` o mapping web.
- **Seguridad fundacional y RBAC**:
  - `SecurityFilterChain` con CSRF deshabilitado para APIs REST stateless / habilitación CORS controlada.
  - Roles base definidos: `ROLE_PACIENTE`, `ROLE_RECEPCIONISTA`, `ROLE_ODONTOLOGO`, `ROLE_ADMINISTRADOR`.
  - Rutas públicas configuradas: `/api/auth/**`, `/api/availability`, `/api/health`, recursos estáticos (`/`, `/index.html`, `/css/**`, `/js/**`).
  - Rutas protegidas por defecto bajo autenticación.
  - Password encoder configurado con `BCryptPasswordEncoder`.
- **Manejo Global de Excepciones**:
  - `@ControllerAdvice` con captura de validaciones (`MethodArgumentNotValidException`), errores 404, errores de negocio (`BusinessException`) y 500.
- **Seeds de prueba ficticios (`data.sql`)**:
  - Inserciones iniciales únicamente con datos ficticios (DNI 99.999.999, Juan Pérez) en cumplimiento estricto de la Ley 25.326.
- **Frontend Shell**:
  - Estructura mínima con `index.html`, `app.ts` / `app.js` y estilos básicos servidos directamente por Spring Boot.
- **Test de humo y verificación**:
  - Tests en JUnit 5 que verifiquen el arranque del contexto Spring Boot y la ejecución en verde de `./gradlew test` con H2.

## 4. Fuera de Alcance (Non-Goals)
- Implementación de la lógica de negocio de registro/login de pacientes (reservado para `C-02`).
- CRUD de profesionales, sillones y prestaciones (reservado para `C-03`).
- Motor de cálculo de franjas y algoritmos de disponibilidad (reservado para `C-04`).
- Motor anti-solapamiento y ciclo de vida del turno (reservado para `C-05`).
- Adaptadores de email SMTP / Mailpit (reservado para `C-06`).
- Exclusiones globales del MVP: WhatsApp Business API, Mercado Pago, AFIP/ARCA, odontograma y multi-sucursal.

## 5. Criterios de Éxito y Verificación
1. `./gradlew test` (o `gradlew.bat test`) compila en Java 21 y ejecuta todos los tests en verde en menos de 10 segundos, sin requerir PostgreSQL ni Docker.
2. Endpoint `/api/health` o ping REST responde `200 OK` con metadata de la aplicación.
3. El servidor levanta en el puerto configurado (8080) y sirve el frontend estático en `/`.
4. El árbol de directorios de backend queda limpio y listo para que las ramas de `C-02`, `C-03` y `C-04` trabajen en paralelo sin colisiones.
