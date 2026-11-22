# Archivo de Cierre — C-06 notificaciones

## Resumen
Change C-06 completado en rama `feat/c06-notificaciones`. Núcleo de notificaciones por email desacoplado de C-02/C-05 mediante eventos y puertos, con recordatorios configurables y enlaces únicos de confirmación/cancelación.

## Artefactos generados
- `backend/src/main/java/com/consultorio/notification/domain/model/` — `TurnoNotificable`, `NotificacionToken`, `ConfiguracionNotificacion`, `TipoNotificacionToken`.
- `backend/src/main/java/com/consultorio/notification/domain/repository/` — repositorios de tokens y configuración.
- `backend/src/main/java/com/consultorio/notification/application/event/` — `TurnoCreadoEvent`, `TurnoReprogramadoEvent`, `TurnoCanceladoEvent`, `AsistenciaConfirmadaEvent`, `TurnoCanceladoPorPacienteEvent`.
- `backend/src/main/java/com/consultorio/notification/application/port/` — `EmailSenderPort`, `TurnoParaRecordarPort` (bean no-op hasta C-05).
- `backend/src/main/java/com/consultorio/notification/application/service/NotificacionService.java` — envío con fallback, idempotencia, tokens de un solo uso y scheduler.
- `backend/src/main/java/com/consultorio/notification/infrastructure/email/` — `SmtpEmailSender`, `EmailDeliveryException`.
- `backend/src/main/java/com/consultorio/notification/api/controller/` — enlaces públicos y ajustes admin.
- `backend/src/main/java/com/consultorio/infrastructure/config/NotificationSecurityConfig.java` — cadena pública con `@Order(1)` sin tocar `SecurityConfig`.
- `backend/src/main/java/com/consultorio/infrastructure/config/NotificationIntegrationConfig.java` — `@EnableScheduling` + puerto no-op.
- `backend/src/main/resources/application.yml` — `spring.mail.*` y `notification.*` por variables de entorno (sin secrets).
- `backend/build.gradle` — `spring-boot-starter-mail`.
- `openspec/specs/C-06-notificaciones/spec.md` y artefactos del change.

## Tests (Strict TDD, todos en verde)
- `NotificacionServiceTest` — 8 tests: dos enlaces únicos, fallback SMTP, antelación 24 h e idempotencia, token usado/vencido/inexistente, evento de cancelación para C-05, cambio de antelación y rechazo de 0.
- `SmtpEmailSenderTest` — MimeMessage y conversión de `MailException`.
- `NotificationControllerTest` — enlaces públicos 200/400 y RBAC admin (200/403).
- `NotificacionTokenRepositoryTest` — `@DataJpaTest` con H2.
- `./gradlew test` → BUILD SUCCESSFUL (37 tests, H2 en memoria).

## Estado
`C-06-notificaciones` marcado como `[x]` en `CHANGES.md`. Integración pendiente cuando se mergeen C-02 y C-05: publicar los eventos y aportar la implementación real de `TurnoParaRecordarPort`.
