# Explore — C-02 auth-pacientes

## Estado actual relevado (`2026-11-22`)
- **Base compilable (`C-01 setup-base`)**: archivada en `openspec/changes/archive/C-01-setup-base/`. `./gradlew.bat test --no-daemon` pasa en verde usando H2 en memoria (`MODE=PostgreSQL`).
- **Estructura de paquetes existente**: `com.consultorio.{domain,application,infrastructure,api}`.
- **Componentes reutilizables ya disponibles**:
  - `BaseEntity` (`id` UUID, `activo`, `fechaCreacion`, `fechaActualizacion`).
  - `RolUsuario` (`PACIENTE`, `RECEPCIONISTA`, `ODONTOLOGO`, `ADMINISTRADOR`).
  - `BusinessException` (400), `ResourceNotFoundException` (404) y `GlobalExceptionHandler` (`@ControllerAdvice`).
  - `SecurityConfig`: `SessionCreationPolicy.STATELESS`, `BCryptPasswordEncoder` bean, whitelist pública (`/api/health`, `/api/auth/**`, `/api/availability`, `/`, `/index.html`, `/css/**`, `/js/**`, `/static/**`), resto `.authenticated()`.
  - `static/index.html`: landing estática servida por Spring Boot.
- **Brechas detectadas para C-02**:
  1. `SecurityConfig` no cuenta todavía con un filtro de autenticación por token (Bearer/HMAC-JWT), necesario para proteger `GET /api/patients/me` y la vinculación de Gmail tras el login.
  2. No existe aún la entidad `Paciente`, su repositorio JPA ni los endpoints `/api/auth/register`, `/api/auth/login`, `/api/patients/me`, `/api/patients/me/recovery-email`.
  3. No existe vista web de Login/Registro/Vinculación de Gmail en `static/`.
