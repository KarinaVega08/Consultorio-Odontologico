# Especificación — C-06 notificaciones

## Objetivo
Enviar recordatorios y confirmaciones por email con enlaces únicos, sin acoplar el envío al flujo de creación de turnos.

## Requisitos funcionales
1. Email de confirmación al crear turno, con enlace único de confirmar asistencia y otro de cancelar.
2. Emails de reprogramación y cancelación.
3. Recordatorio automático con antelación configurable por Administrador (default 24 h).
4. Enlaces únicos con vencimiento y un solo uso.
5. Fallo de SMTP logueado sin propagar excepción al flujo de turnos.

## Criterios de aceptación (Dado / Cuando / Entonces)
- **CA-1 (feliz)**: Dado un `TurnoCreadoEvent` con email ficticio, cuando se procesa, entonces se envía un correo con dos enlaces únicos y se persisten sus tokens.
- **CA-2 (feliz)**: Dado un turno dentro de la ventana de 24 h, cuando corre el scheduler, entonces se envía el recordatorio una sola vez.
- **CA-3 (error)**: Dado `JavaMailSender` que lanza `MailException`, cuando se envía una notificación, entonces no se lanza excepción hacia el llamador y queda registrado el error.
- **CA-4 (borde)**: Dado un token ya usado o vencido, cuando se abre el enlace, entonces recibe 400 y no se repite la acción.
- **CA-5 (borde)**: Dado un Administrador, cuando cambia la antelación a 48 h, entonces el próximo recordatorio usa esa ventana.
- **CA-6 (seguridad)**: Dado un token válido de cancelación, cuando se abre el enlace, entonces se marca usado y se publica `TurnoCanceladoPorPacienteEvent` para que C-05 cambie el estado.
- **CA-7 (entrega)**: Dado un servidor SMTP embebido (GreenMail, equivalente a Mailpit), cuando se envía un recordatorio, entonces el correo se entrega con destinatario, asunto y cuerpo verificables sin depender de servicios externos.

## Requisitos no funcionales
Java 21, Spring Boot 3, H2 en tests, SMTP real solo en producción, Mailpit/mock en desarrollo, secrets por entorno, datos ficticios (Ley 25.326).
