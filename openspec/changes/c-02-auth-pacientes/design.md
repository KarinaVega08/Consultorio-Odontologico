# Diseño Técnico — C-02 auth-pacientes

## Arquitectura y ubicación de componentes
Se respeta la estructura por capas ya establecida en `C-01`:
- **Domain (`com.consultorio.domain`)**:
  - `model/Paciente.java`: extiende `BaseEntity`. Campos: `nombreCompleto` (`@Column(nullable = false)`), `dni` (`@Column(nullable = false, unique = true, length = 8)`), `passwordHash` (`@Column(nullable = false)`), `emailRecuperacion` (`@Column(unique = false, nullable = true)`).
  - `repository/PacienteRepository.java`: `JpaRepository<Paciente, UUID>` con `Optional<Paciente> findByDni(String dni)` y `boolean existsByDni(String dni)`.
- **Application (`com.consultorio.application`)**:
  - `dto/RegisterRequest.java`, `LoginRequest.java`, `AuthResponse.java`, `PatientProfileResponse.java`, `LinkRecoveryEmailRequest.java` (con `@NotBlank`, `@Pattern(regexp = "^\\d{7,8}$")`, `@Email`, validación `@gmail.com`).
  - `service/AuthService.java`: lógica transaccional (`@Transactional`) de registro, login, consulta de perfil y vinculación única de Gmail.
  - `service/TokenService.java`: interfaz para generación y validación de tokens Bearer stateless (sin acoplar `application` a detalles criptográficos).
- **Infrastructure (`com.consultorio.infrastructure`)**:
  - `security/HmacTokenService.java`: implementación HMAC-SHA256 stateless usando `${APP_JWT_SECRET:dev-only-insecure-change-in-prod-key-32bytes}` (configurable por variable de entorno, nunca credenciales reales).
  - `security/BearerTokenAuthenticationFilter.java`: `OncePerRequestFilter` que extrae `Authorization: Bearer <token>`, valida firma y expiración, y pobla `SecurityContextHolder` con `ROLE_PACIENTE` y el `patientId` / `dni` como principal.
  - `config/SecurityConfig.java`: mantiene exactamente las rutas públicas de `C-01` (`/api/health`, `/api/auth/**`, `/api/availability`, estáticos), agrega `BearerTokenAuthenticationFilter` antes de `UsernamePasswordAuthenticationFilter`.
  - `config/GlobalExceptionHandler.java`: agrega handler para `AuthenticationException` / `BadCredentialsException` → `401 Unauthorized` si no existe.
- **API (`com.consultorio.api.controller`)**:
  - `AuthController.java`: `POST /api/auth/register` (201) y `POST /api/auth/login` (200).
  - `PatientController.java`: `GET /api/patients/me` (200) y `POST /api/patients/me/recovery-email` (200).
- **Frontend estático (`backend/src/main/resources/static/`)**:
  - `login.html` y `js/auth.js`: tabs de Iniciar sesión / Registrarse y modal/sección post-login para vincular Gmail una sola vez + consulta a `/api/patients/me`.
  - `index.html`: enlace discreto hacia `/login.html` sin modificar la estructura visual existente.

## Decisiones de diseño
- **Token HMAC-SHA256 propio sin dependencias externas nuevas**: evita agregar librerías JJWT externas en Gradle y mantiene el build liviano y 100% offline/reproducible en tests H2.
- **Cero datos reales (Ley 25.326)**: todos los tests usan DNIs ficticios (`99999991`..`99999999`) y correos `@gmail.com` ficticios.
