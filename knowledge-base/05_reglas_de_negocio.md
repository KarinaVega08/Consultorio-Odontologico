# Reglas de Negocio

Cada regla tiene un código único `RN-{DOMINIO}-{NN}` para trazabilidad.

## Dominio: Autenticación (RN-AU)

- **RN-AU-01**: El DNI del paciente es único en el sistema; no se admiten registros duplicados. — Justificación: evita cuentas duplicadas y asegura el identificador principal del paciente.
- **RN-AU-02**: El DNI debe tener formato numérico de 7 u 8 dígitos. — Justificación: estándar argentino de DNI.
- **RN-AU-03**: El paciente se registra con nombre real, DNI y contraseña. El correo Gmail de recuperación se vincula una sola vez, después del primer login. — Justificación: flujo de registro simplificado; el Gmail no es obligatorio para entrar, solo para recuperación.
- **RN-AU-04**: El login se realiza con DNI + contraseña. El Gmail no se vuelve a pedir salvo que se solicite recuperación de contraseña desde la web. — Justificación: reduce fricción en el ingreso habitual.
- **RN-AU-05**: Solo la Recepcionista o el Administrador pueden blanquear presencialmente la contraseña de un paciente. — Justificación: operación sensible que requiere verificación de identidad en el consultorio.

## Dominio: Turnos y Agenda (RN-TU)

- **RN-TU-01**: Un mismo profesional no puede tener dos turnos superpuestos en fecha y horario. Dos turnos consecutivos donde uno empieza exactamente cuando termina el otro SÍ están permitidos. — Justificación: evita doble reserva y permite agenda continua sin huecos innecesarios.
- **RN-TU-02**: Un mismo sillón/box no puede tener dos turnos superpuestos en fecha y horario. Dos turnos consecutivos borde a borde SÍ están permitidos. — Justificación: el sillón es un recurso físico independiente del profesional.
- **RN-TU-03**: La duración del turno se calcula automáticamente según la prestación seleccionada y debe entrar completa dentro del horario laboral vigente del profesional y sin chocar con bloqueos. — Justificación: garantiza que el turno sea ejecutable en su totalidad.
- **RN-TU-04**: Al cancelar un turno, su estado pasa a CANCELADO (conservando historial, nunca borrado físico) y el hueco vuelve a figurar disponible de inmediato. — Justificación: trazabilidad de operaciones y reaprovechamiento del hueco.
- **RN-TU-05**: Solo el Administrador (o el Odontólogo con permisos de admin) puede crear/editar profesionales, sillones/boxes y prestaciones. — Justificación: configuración del consultorio es responsabilidad de nivel administrativo.

## Dominio: Notificaciones (RN-NT)

- **RN-NT-01**: Los recordatorios automáticos se envían por correo electrónico al Gmail vinculado del paciente. — Justificación: canal de recuperación y contacto definido en el registro.
- **RN-NT-02**: El recordatorio incluye opción de confirmar asistencia. — Justificación: permite reducir inasistencias mediante confirmación explícita.

## Dominio: Excepciones globales (RN-EX)

- **RN-EX-01**: Nunca se almacenan datos reales de pacientes en el repositorio, seeds ni tests. Se usan exclusivamente datos ficticios (Ley 25.326 Argentina, protección de datos sensibles de salud).
- **RN-EX-02**: El entorno debe ser reproducible en local sin depender de nube externa ni Docker obligatorio para correr tests (H2 en memoria para tests, PostgreSQL local para ejecución).
