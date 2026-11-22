# Specification — C-01 Setup Base y Seguridad Inicial

## 1. Requerimientos Técnicos y de Configuración

### 1.1 Stack de Ejecución
- **Lenguaje**: Java 21 (LTS).
- **Framework**: Spring Boot 3.3.x o 3.4.x.
- **Herramienta de Construcción**: Gradle (con Gradle Wrapper `gradlew` / `gradlew.bat`).
- **Persistencia**: Spring Data JPA con Hibernate.
- **Motor de BD Runtime**: PostgreSQL 15+.
- **Motor de BD Tests**: H2 en memoria (`jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL`).
- **Seguridad**: Spring Security 6.x con `BCryptPasswordEncoder`.
- **Validaciones**: Hibernate Validator (`spring-boot-starter-validation`).

### 1.2 Configuración Externa (`application.yml`)
Debe soportar parametrización limpia sin credenciales hardcodeadas:
- `server.port`: `${APP_PORT:8080}`
- `spring.datasource.url`: `${DB_URL:jdbc:postgresql://localhost:5432/consultorio}`
- `spring.datasource.username`: `${DB_USER:consultorio_user}`
- `spring.datasource.password`: `${DB_PASSWORD:consultorio_pass}`
- `spring.jpa.hibernate.ddl-auto`: `update` (en desarrollo local)
- `spring.jpa.properties.hibernate.dialect`: `org.hibernate.dialect.PostgreSQLDialect`
- `spring.mail.*`: Placeholders para `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD` sin secrets por defecto.

Perfil de test (`application-test.yml` o dentro de `src/test/resources/application.yml`):
- `spring.datasource.url`: `jdbc:h2:mem:consultorio_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL`
- `spring.datasource.driver-class-name`: `org.h2.Driver`
- `spring.jpa.hibernate.ddl-auto`: `create-drop`
- `spring.jpa.properties.hibernate.dialect`: `org.hibernate.dialect.H2Dialect`

## 2. Especificación de Seguridad y RBAC

### 2.1 Roles del Sistema
Se definen los siguientes roles en el enum `RolUsuario`:
- `PACIENTE` (Authority: `ROLE_PACIENTE`)
- `RECEPCIONISTA` (Authority: `ROLE_RECEPCIONISTA`)
- `ODONTOLOGO` (Authority: `ROLE_ODONTOLOGO`)
- `ADMINISTRADOR` (Authority: `ROLE_ADMINISTRADOR`)

### 2.2 Política de Rutas en `SecurityFilterChain`
- **Rutas Públicas (sin autenticación)**:
  - `GET /api/health` — Verificación de estado de la aplicación.
  - `POST /api/auth/register` — Registro de pacientes.
  - `POST /api/auth/login` — Autenticación DNI + contraseña.
  - `POST /api/auth/recover-password` — Solicitud de recuperación de contraseña.
  - `POST /api/auth/reset-password` — Reseteo con token.
  - `GET /api/availability` — Consulta pública de turnos/horarios disponibles.
  - Rutas estáticas: `/`, `/index.html`, `/css/**`, `/js/**`, `/favicon.ico`.
- **Rutas Protegidas (requieren autenticación)**:
  - Cualquier otra petición bajo `/api/**` requiere estar autenticado (`authenticated()`).
  - Las rutas bajo `/api/admin/**` quedan restringidas a `hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA')` o `hasRole('ADMINISTRADOR')` según corresponda a la matriz RBAC.
- **Configuración de Sesión y CSRF**:
  - CSRF deshabilitado para endpoints de API REST sin estado (`csrf(AbstractHttpConfigurer::disable)`).
  - CORS habilitado para desarrollo local si fuera necesario (`cors(Customizer.withDefaults())`).

## 3. Especificación de Manejo de Excepciones

### 3.1 Estructura Estándar de Error (`ErrorResponseDTO`)
Toda respuesta de error debe devolver JSON consistente:
```json
{
  "timestamp": "2026-11-22T02:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Mensaje descriptivo del error",
  "path": "/api/...",
  "fieldErrors": [
    {
      "field": "dni",
      "message": "El DNI debe tener entre 7 y 8 dígitos numéricos"
    }
  ]
}
```

### 3.2 Casos Capturados en `@ControllerAdvice`
1. `MethodArgumentNotValidException`: Devuelve HTTP 400 con lista de `fieldErrors`.
2. `BusinessException` (excepción base de reglas de negocio): Devuelve HTTP 400 / 422 con mensaje legible.
3. `ResourceNotFoundException`: Devuelve HTTP 404.
4. `AccessDeniedException`: Devuelve HTTP 403 Forbidden.
5. `Exception` no controlada: Devuelve HTTP 500 Internal Server Error con mensaje genérico seguro sin filtrar stack trace al cliente.

## 4. Semillas de Datos (`data.sql`) y Cumplimiento Ley 25.326 (RN-EX-01)
- No se incluirán nombres reales, DNIs reales ni correos personales en ningún archivo del repositorio.
- Seed data inicial en `backend/src/main/resources/data.sql` o vía inicializador controlado:
  - Usuario Administrador demo ficticio: DNI `00000000`, Nombre `Admin Consultorio`.
  - Paciente demo ficticio: DNI `99999999`, Nombre `Paciente Ejemplo`.
  - Sillones/boxes iniciales: `Sillón 1`, `Sillón 2`.
  - Prestaciones iniciales con duraciones estándar:
    - `Consulta/Diagnóstico`: 30 min
    - `Limpieza`: 30 min
    - `Obturación/Arreglo`: 45 min
    - `Extracción`: 45 min
    - `Endodoncia`: 60 min

## 5. Criterios de Aceptación (Gherkin BDD)

### Escenario 1: Ejecución limpia de la suite de pruebas sin servicios externos
- **Given** que el repositorio está recién clonado en una máquina con Java 21 instalada y sin bases de datos externas corriendo.
- **When** se ejecuta `./gradlew test` en la raíz o en `backend/`.
- **Then** el proyecto compila exitosamente.
- **And** se ejecutan todas las pruebas unitarias y de integración sobre H2.
- **And** el resultado final de la build es `BUILD SUCCESSFUL` con 0 tests fallidos.

### Escenario 2: Endpoint de Health Check público
- **Given** que la aplicación Spring Boot está iniciada en el puerto 8080.
- **When** un cliente realiza un `GET /api/health` sin headers de autenticación.
- **Then** el servidor responde con status HTTP 200 OK.
- **And** el cuerpo de la respuesta contiene `{"status": "UP", "app": "Consultorio Odontológico"}`.

### Escenario 3: Bloqueo de rutas protegidas sin credenciales
- **Given** que la aplicación Spring Boot está iniciada.
- **When** un cliente no autenticado realiza un `GET /api/patients/me` o `POST /api/admin/services`.
- **Then** el servidor responde con status HTTP 401 Unauthorized o 403 Forbidden.

### Escenario 4: Servido de archivos estáticos del frontend
- **Given** que la aplicación Spring Boot está iniciada.
- **When** un usuario navega a `http://localhost:8080/`.
- **Then** el servidor devuelve `index.html` con status 200 OK y el shell web del consultorio.
