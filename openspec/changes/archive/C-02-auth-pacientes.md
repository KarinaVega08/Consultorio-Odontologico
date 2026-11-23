# Archivo de Cierre — C-02 auth-pacientes

## Resumen
Change C-02 (auth-pacientes) completado e integrado en rama `feat/c02-registro-pacientes` sobre `main`. Autenticación y gestión de cuenta de pacientes: registro/login por DNI, vinculación única de Gmail de recuperación, recuperación de contraseña, reset con token único y blanqueo presencial.

## Artefactos generados
- `backend/src/main/java/com/consultorio/api/controller/AuthController.java` — endpoints públicos `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/recover-password`, `POST /api/auth/reset-password`
- `backend/src/main/java/com/consultorio/api/controller/PatientController.java` — endpoint autenticado `GET /api/patients/me`, `POST /api/patients/me/recovery-email`
- `backend/src/main/java/com/consultorio/api/controller/AdminPasswordController.java` — `POST /api/admin/password-reset` protegido con `@PreAuthorize("hasAnyRole('RECEPCIONISTA', 'ADMINISTRADOR')")` (RN-AU-05)
- `backend/src/main/java/com/consultorio/application/service/AuthService.java` — registro, login, link de Gmail, recuperación, reset, blanqueo presencial
- `backend/src/main/java/com/consultorio/application/service/TokenService.java` + `HmacTokenService.java` — firma HMAC-SHA256 de tokens Bearer
- `backend/src/main/java/com/consultorio/infrastructure/security/BearerTokenAuthenticationFilter.java` — validación de encabezado `Authorization: Bearer`
- `backend/src/main/java/com/consultorio/domain/model/Paciente.java` — entidad sobre `BaseEntity` (id UUID, DNI único 7-8 dígitos, password_hash, email_recuperacion opcional)
- `backend/src/main/java/com/consultorio/domain/repository/PacienteRepository.java`
- `backend/src/main/java/com/consultorio/domain/model/RecoveryToken.java` — token de recuperación de un solo uso con vencimiento
- `backend/src/main/java/com/consultorio/domain/repository/RecoveryTokenRepository.java`
- `backend/src/main/java/com/consultorio/application/dto/AuthResponse.java`, `LoginRequest.java`, `RegisterRequest.java`, `PatientProfileResponse.java`, `RecoveryRequest.java`, `ResetPasswordRequest.java`, `LinkRecoveryEmailRequest.java`
- `backend/src/main/resources/static/login.html` + `js/auth.js` — vista frontend de login/registro/vinculación Gmail
- `backend/src/main/resources/static/index.html` — botón de acceso "Ingresar / Registrarse (Pacientes)"
- `backend/src/main/java/com/consultorio/infrastructure/config/SecurityConfig.java` — `@Order(2)`, filtro Bearer, BCryptPasswordEncoder, whitelist de rutas públicas
- `backend/src/test/java/com/consultorio/api/controller/PatientAuthIntegrationTest.java` — 11 escenarios de integración (datos ficticios, Ley 25.326, H2 en memoria)
- `backend/src/test/java/com/consultorio/application/service/AuthServiceTest.java` — tests unitarios de servicio
- `backend/src/test/resources/application.yml` — configuración de tests con H2 y Mailpit

## Tests (Strict TDD, todos en verde)
- `PatientAuthIntegrationTest` — 11 tests: registro/login, DNI duplicado, DNI inválido, contraseña errónea, vinculación de Gmail única, recuperación con/sin Gmail, reset con token válido/inválido, admin reset sin auth (403), vista estática pública
- `AuthServiceTest` — 5 tests unitarios: registro con hash, DNI duplicado, login válido/inválido, vinculación de Gmail única y rechazo de segundo intento
- `./gradlew clean test` → BUILD SUCCESSFUL (87 tests totales: C-01 + C-02 + C-03 + C-06, H2 en memoria)

## Estado
`C-02-auth-pacientes` marcado como `[x]` completado en `CHANGES.md`. Integrado con C-01 (base), C-03 y C-06 (código ya en main). Listo para habilitar C-05 (gestión de turnos) una vez que C-03 y C-04 estén mergeados.
