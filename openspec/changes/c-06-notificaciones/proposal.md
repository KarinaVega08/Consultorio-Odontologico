# Proposal — C-06 Notificaciones

## 1. Contexto
El MVP define email como único canal de notificaciones (RN-NT-01, RN-NT-02). C-06 depende de C-02 (Gmail vinculado del paciente) y C-05 (ciclo de vida del turno), que aún no están en `main`. Para no bloquear la implementación, el change construye el núcleo de notificaciones desacoplado y define eventos/puertos de integración que C-05 publicará al mergearse.

## 2. Motivación
Sin recordatorios ni confirmación, el consultorio no reduce inasistencias (problema central del Discovery). Además, US-009 CA-3 exige email de confirmación al crear turno.

## 3. Alcance (In-Scope)
- Adaptador SMTP con `JavaMailSender` (Gmail en prod, Mailpit/mock en dev) y credenciales por variables de entorno.
- Recordatorio automático con antelación configurable por Administrador (default 24 h, IN-02).
- Emails de creación, reprogramación y cancelación de turno.
- Enlaces únicos de confirmación de asistencia y de cancelación, con vencimiento y un solo uso.
- Scheduler de Spring; fallos de SMTP se loguean sin romper el flujo de turnos.
- Tests con `JavaMailSender` mockeado, sin servicios externos.

## 4. Fuera de Alcance
- WhatsApp Business API, Mercado Pago, AFIP/ARCA, odontograma, multi-sucursal.
- Implementar C-02 o C-05: se consumen mediante eventos/puertos.

## 5. Reglas relacionadas
RN-NT-01, RN-NT-02, RN-EX-01, RN-EX-02.

## 6. Criterios de éxito
`./gradlew test` en verde con H2; ningún secret en el repo; enlace único no reutilizable; SMTP caído no lanza excepción hacia la creación de turnos.
