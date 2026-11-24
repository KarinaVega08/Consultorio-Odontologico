# Tasks — C-02 auth-pacientes

- [x] **T-01**: Crear entidad `Paciente` (hereda de `BaseEntity`) y `PacienteRepository` en `com.consultorio.domain`.
- [x] **T-02**: Crear DTOs de request/response con validaciones Bean Validation (`RegisterRequest`, `LoginRequest`, `AuthResponse`, `PatientProfileResponse`, `LinkRecoveryEmailRequest`).
- [x] **T-03**: Implementar `TokenService`, `HmacTokenService` y `BearerTokenAuthenticationFilter`, e integrarlo en `SecurityConfig` (manteniendo intactos los tests de `C-01`).
- [x] **T-04**: Implementar `AuthService` (registro, login, perfil, vinculación única de Gmail) y manejar `401` en `GlobalExceptionHandler`.
- [x] **T-05**: Implementar `AuthController` (`/api/auth/register`, `/api/auth/login`) y `PatientController` (`/api/patients/me`, `/api/patients/me/recovery-email`).
- [x] **T-06**: Crear vista `static/login.html` + `static/js/auth.js` y enlazarla desde `static/index.html`.
- [x] **T-07**: Escribir tests unitarios y de integración (`AuthServiceTest`, `AuthControllerTest`, `PatientControllerTest`) cubriendo los 6 escenarios del spec con datos ficticios y verificar `.\gradlew.bat test` en verde.
