# Design — C-06 Notificaciones

## Estructura
Feature package `com.consultorio.notification` con `domain/model`, `domain/repository`, `application/dto`, `application/event`, `application/port`, `application/service`, `api/controller` e `infrastructure/email`.

## Desacoplamiento de C-02 y C-05
- `TurnoNotificable` es un record con los datos mínimos (turnoId, pacienteId, DNI, email, profesional, inicio, prestación). No es una entidad JPA, por lo que no colisiona con el `Turno` de C-05.
- Eventos `TurnoCreadoEvent`, `TurnoReprogramadoEvent`, `TurnoCanceladoEvent`: C-05 los publicará cuando esté integrado. Hoy los tests los publican directamente.
- `TurnoParaRecordarPort`: C-05 aportará la consulta real de turnos próximos. Hasta entonces, `NotificationIntegrationConfig` expone un bean no-op con `@ConditionalOnMissingBean`, para que el contexto arranque sin C-05.
- `TurnoCanceladoPorPacienteEvent` y `AsistenciaConfirmadaEvent` se publican desde los enlaces únicos para que C-05 aplique el cambio de estado.

## Seguridad
Los enlaces públicos `/api/notifications/**` viven en una `SecurityFilterChain` propia con `@Order(1)` y `securityMatcher`, sin editar `SecurityConfig` (evita colisiones con C-02). La configuración de antelación es admin-only vía `@PreAuthorize("hasRole('ADMINISTRADOR')")`.

## Decisiones
1. Tokens UUID sin guiones, `unique`, con `tipo`, `turnoId`, `emailDestino`, `fechaExpiracion` y `usado` (un solo uso).
2. Idempotencia del recordatorio: se registra un token `RECORDATORIO` por turno y se verifica antes de enviar.
3. Envío vía `EmailSenderPort`; `SmtpEmailSender` usa `JavaMailSender`. Errores de entrega se convierten en `EmailDeliveryException`, que `NotificacionService` captura y loguea (SLF4J) sin propagar.
4. Configuración: `spring.mail.*` y `notification.*` desde variables de entorno (`SMTP_*`, `REMINDER_HOURS_BEFORE`, `APP_BASE_URL`). Sin secrets hardcodeados.
5. Tests por capa: unitarios Mockito para el servicio y el adapter, `@WebMvcTest` para controladores, `@DataJpaTest` para tokens. H2, datos ficticios.

## Convenciones
DTOs en la API, `@Valid`, `@ControllerAdvice`, servicios `@Transactional` y stateless, mensajes parametrizados, sin `@DirtiesContext`.
