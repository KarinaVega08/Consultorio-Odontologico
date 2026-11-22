# Tareas — C-06 notificaciones

## Tarea 1 — Núcleo de notificaciones
- [x] Record `TurnoNotificable`, eventos y puertos de integración con C-02/C-05.
- [x] Entidades `NotificacionToken` y `ConfiguracionNotificacion` + repositorios.
- [x] Test `@DataJpaTest` de tokens.

## Tarea 2 — Envío y service
- [x] `EmailSenderPort` + `SmtpEmailSender` con `JavaMailSender`.
- [x] `NotificacionService`: listeners, recordatorio idempotente, enlaces únicos, fallback SMTP.
- [x] Tests unitarios de los CA-1 a CA-6.
- [x] `SmtpDeliveryTest`: entrega real contra SMTP embebido (GreenMail, equivalente a Mailpit sin servicios externos).

## Tarea 3 — API y scheduler
- [x] Enlaces públicos `/api/notifications/confirm|cancel` y ajustes admin.
- [x] `@EnableScheduling` + `@Scheduled`, configuración SMTP por entorno.
- [x] Tests `@WebMvcTest` de enlaces y ajustes.

## Tarea 4 — Verificación y cierre
- [x] `./gradlew test` en verde con H2.
- [x] Marcar checkbox de C-06 en `CHANGES.md`.
- [x] Copiar spec vigente y archivar el change.
