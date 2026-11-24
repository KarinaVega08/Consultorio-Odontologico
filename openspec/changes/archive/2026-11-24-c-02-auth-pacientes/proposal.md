# Propuesta — C-02 auth-pacientes (Registro, Login con DNI y Vinculación única de Gmail)

## Justificación de la elección
Se elige este change porque pertenece al **MVP imprescindible** definido en la Sección D del Discovery (registro único por DNI de 7–8 dígitos, login sin fricción y vinculación opcional única de Gmail), desbloquea la autenticación de pacientes para `C-05` (`gestion-turnos`) y posee reglas de negocio verificables (`RN-AU-01` a `RN-AU-04`, `RN-EX-01`).

## Alcance incluido (rama `feat/c02-registro-pacientes`)
- Entidad `Paciente` (hereda de `BaseEntity`: UUID, `activo`, `fechaCreacion`, `fechaActualizacion`) con `nombreCompleto`, `dni` (7–8 dígitos numéricos, único), `passwordHash` (BCrypt), `emailRecuperacion` (opcional, vinculable una sola vez).
- `POST /api/auth/register`: registro de paciente validando formato y unicidad de DNI y hashing de contraseña con `BCryptPasswordEncoder`.
- `POST /api/auth/login`: autenticación por DNI + contraseña, devolviendo token Bearer stateless e indicador `requiereVincularGmail`.
- `GET /api/patients/me`: consulta del perfil del paciente autenticado (DTO sin `passwordHash`).
- `POST /api/patients/me/recovery-email`: vinculación única de correo `@gmail.com` de recuperación tras el primer login.
- Filtro de autenticación Bearer en `SecurityConfig` para validar tokens sin romper rutas públicas ni tests existentes de `C-01`.
- Vista estática `static/login.html` (login, registro e invitación a vincular Gmail una sola vez) enlazada desde `static/index.html`.

## Fuera de alcance en este change
- Recuperación por email (`recover-password` / `reset-password` con SMTP/Mailpit, diferido a integración con `C-06`).
- Blanqueo presencial por Recepción/Admin (`POST /api/admin/password-reset`, fuera del recorte asignado en esta rama).
- Datos reales de pacientes (cumplimiento estricto `RN-EX-01` / Ley 25.326: solo datos ficticios).

## Leer antes
- `knowledge-base/03_actores_y_roles.md` — §RBAC, §Rutas públicas
- `knowledge-base/04_modelo_de_datos.md` — §Paciente
- `knowledge-base/05_reglas_de_negocio.md` — §RN-AU-01 a RN-AU-04, §RN-EX-01
- `knowledge-base/07_flujos_principales.md` — §Flujo 1
