# Discovery — Gestión de turnos para consultorios odontológicos

**Fecha**: 2026-11-19  
**Fuentes investigadas**: 7 competidores (ver `discovery/sources/`); 1 con fetch fallido (Odentix).

## 1. Problema que resuelve

Los consultorios odontológicos en Argentina pierden ingresos y tiempo operativo por tres causas principales: ausencias de pacientes sin aviso, agendas manuales coordinadas por WhatsApp o planillas de Excel, y reservas superpuestas entre profesionales y sillones/boxes. El proyecto propone un sistema de gestión de turnos que centralice agenda, pacientes, prestaciones y caja, permitiendo la autogestión online del paciente y el control anti-solapamiento del consultorio.

## 2. Usuarios / roles

- **Administrador**: configura el consultorio (profesionales, sillones/boxes, horarios de atención, bloqueos y catálogo de prestaciones) y supervisa la agenda global. En consultorios unipersonales, el odontólogo puede asumir también este rol.
- **Odontólogo**: gestiona su propia agenda diaria/semanal, registra la atención y consulta sus turnos asignados.
- **Recepcionista / Secretaria**: agenda, reprograma y cancela turnos, controla la asistencia y puede blanquear o generar una nueva contraseña presencialmente a pacientes que lo soliciten en el consultorio.
- **Paciente**: se registra con nombre real, DNI (como identificador de usuario) y contraseña; vincula un correo Gmail de recuperación una sola vez; inicia sesión con DNI y contraseña; consulta disponibilidad y reserva, confirma o cancela sus turnos desde su cuenta.

## 3. Casos de uso

1. Como paciente, quiero registrarme con mi nombre real, DNI y contraseña, y poder recuperar mi contraseña por Gmail o mediante blanqueo presencial en el consultorio.
2. Como paciente, quiero consultar los horarios disponibles y reservar un turno online desde mi cuenta.
3. Como recepcionista, odontólogo o administrador, quiero crear un turno asignando paciente, profesional, sillón/box, prestación (con duración variable) y fecha/hora, evitando automáticamente cualquier solapamiento por profesional y por sillón/box.
4. Como paciente, recepcionista u odontólogo, quiero confirmar, reprogramar o cancelar un turno existente, liberando el hueco automáticamente al cancelar.

## 4. Competidores / soluciones existentes

Se investigaron 7 soluciones del mercado (6 con contenido extraído, 1 con fetch fallido):

| Competidor | Problema que resuelve | Pricing | Diferenciadores | notas_file |
|---|---|---|---|---|
| Dentry | Digitalizar consultorio: paciente entra una vez, todo ya sabe quién es. Historias clínicas, presupuestos, obras sociales, facturación a ARCA. | Plan gratis para siempre; planes en /planes (no detallados). Sin tarjeta de crédito. | Normativa argentina (leyes 26.529/25.326), SOC 2 Type II, facturación directa a ARCA. | `discovery/sources/dentry.md` |
| Odontal Web | Gestión con asistente de turnos con IA (Odi) 24/7, agenda virtual, historias clínicas. | USD 30/mes (hasta 2 odontólogos); anual USD 27/mes; odontólogo extra USD 10/mes. Trial 60 días. | Asistente de turnos con IA, diseñado por odontólogos, sin manuales, 100% nube. | `discovery/sources/odontalweb.md` |
| Dentify | Gestión clínica sin fichas de papel: agenda inteligente, odontograma interactivo, control financiero, obras sociales. | Gratis 14 días; Básico $30.000/mes (1 pro); Clínicas $45.000/mes; anual 2 meses gratis. | Hecho en Argentina, soporte local 24/7, migración gratuita de datos. | `discovery/sources/dentify.md` |
| Odontoly | Agenda, historia clínica y caja ARS/USD conectadas; datos del paciente quedan en el consultorio (local-first). | USD 149,99/mes, sin permanencia, instalador Mac firmado, activación acompañada. | Local-first, caja ARS/USD nativa, activación acompañada, sin permanencia. | `discovery/sources/odontoly.md` |
| Odontara | Agenda, pacientes e información clínica en un solo lugar, para consultorios y clínicas de Argentina. | No publicado (solicitar demostración por WhatsApp). | Agenda en grilla, varias sedes, aviso por correo al agendar, puesta en marcha acompañada. | `discovery/sources/odontara.md` |
| Odonto.ar | Historias clínicas, agenda, obras sociales y finanzas en un solo lugar. Turnos online autogestionados. | 30 días gratis sin tarjeta; Individual $29 USD/mes; Profesional $49 USD/mes; Clínica $99 USD/mes. Pago en pesos vía Mercado Pago. | Turnos online por link, planes por cantidad de usuarios, 30 días trial, importación desde Excel. | `discovery/sources/odonto.md` |
| Odentix | (sin datos — fetch falló, página no devolvió contenido legible) | (sin datos) | (sin datos) | `discovery/sources/odentix.md` |

Sustitutos directos (no software especializado): agenda manual por WhatsApp, Google Calendar, Excel.

Nota: el usuario solicitó un mínimo de 15 sistemas competidores para el informe de mercado. Hasta el momento se relevaron 7 (6 con datos públicos, 1 sin datos). Los 9 restantes pueden completarse con nuevas URLs o marcarse como "No evidenciado" cuando no exista fuente pública verificable.

## 5. Funcionalidades necesarias (MVP)

- Registro de pacientes: nombre real, DNI (identificador único, 7-8 dígitos), contraseña; validación de formato y unicidad en base de datos.
- Vinculación posterior única de correo Gmail de recuperación (una vez creada la cuenta).
- Login con DNI y contraseña; recuperación de contraseña por Gmail o blanqueo presencial por Recepción.
- Roles diferenciados: Administrador, Odontólogo (puede asumir rol admin en consultorios unipersonales), Recepcionista/Secretaria, Paciente.
- Configuración administrativa de profesionales, sillones/boxes, horarios de atención, bloqueos y catálogo de prestaciones con duración variable en minutos (configurada por Admin u Odontólogo con permisos admin).
- Gestión de turnos: creación, reprogramación y cancelación, tanto por personal del consultorio como por autogestión online del paciente.
- Motor de disponibilidad con validación estricta anti-solapamientos por profesional y por sillón/box. Dos turnos consecutivos (uno empieza exactamente cuando termina el otro) están permitidos.
- Recordatorios automáticos por correo electrónico al Gmail vinculado del paciente, con opción de confirmar asistencia.

## 6. Funcionalidades opcionales (post-lanzamiento)

- Recordatorios y chatbot por WhatsApp Business API.
- Cobros online, señas con Mercado Pago, facturación electrónica AFIP/ARCA y obras sociales/prepagas.
- Odontograma interactivo, periodontograma y radiografías/imágenes clínicas.
- Lista de espera automática y multi-sucursal.

## 7. Reglas de negocio

- Un mismo profesional no puede tener dos turnos superpuestos en fecha y horario.
- Un mismo sillón/box no puede tener dos turnos superpuestos en fecha y horario.
- La duración del turno se calcula automáticamente según la prestación seleccionada y debe entrar completa dentro del horario laboral vigente del profesional y sin chocar con bloqueos.
- El DNI del paciente es único en el sistema; no se admiten registros duplicados.
- Solo el Administrador (o el Odontólogo con permisos de admin) puede crear/editar profesionales, sillones/boxes y prestaciones.
- Solo la Recepcionista o el Administrador pueden blanquear presencialmente la contraseña de un paciente.
- Al cancelar un turno, su estado pasa a CANCELADO (conservando historial, nunca borrado físico) y el hueco vuelve a figurar disponible de inmediato.

## 8. Integraciones

MVP: servicio de correo electrónico (SMTP / Gmail) para recuperación de contraseña y recordatorios de turno. En entorno de desarrollo/pruebas se usa Mailpit o mock SMTP.

Etapas posteriores (fuera de MVP): WhatsApp Business API, Mercado Pago, Google Calendar y facturación electrónica AFIP/ARCA.

## 9. Restricciones

- Plazo de entrega fijo: miércoles 25 de noviembre de 2026 (proyecto académico UTN, grupo de 3 integrantes).
- Presupuesto $0: no se contratan servicios pagos ni APIs de terceros con costo (WhatsApp Meta API queda fuera del MVP).
- Privacidad y protección de datos sensibles de salud (Ley 25.326 Argentina): nunca almacenar datos reales de pacientes en el repositorio, seeds ni tests; usar exclusivamente datos ficticios.
- Entorno reproducible en local sin depender de nube externa ni Docker obligatorio para correr tests (base de datos H2 en memoria para tests automatizados y PostgreSQL local para ejecución).
- Stack técnico del equipo: backend en Java 21 con Spring Boot 3, Gradle, JPA/Hibernate y API REST; frontend liviano en TypeScript Vanilla (servido estático o desacoplado, sin frameworks pesados).

## 10. Riesgos

- **Concurrencia y condiciones de carrera (race conditions)**: que dos usuarios (o recepción y un paciente online) intenten reservar el mismo horario/profesional/sillón en simultáneo y se genere un doble turno.
- **Alcance desbordado (scope creep)**: intentar implementar demasiados módulos en 6 días en lugar de dejar un MVP quirúrgico bien especificado y probado.
- **Alucinaciones en el relevamiento de mercado**: que el informe asuma funcionalidades de competidores que no estén respaldadas públicamente en sus webs (por eso se exige marcar "No evidenciado" cuando no haya fuente verificable).

## 11. Preguntas abiertas

- ¿Cuántas horas exactas antes del turno se dispara el recordatorio automático por correo electrónico (ej. 24 hs o 48 hs antes) y hasta cuántas horas antes puede cancelar un paciente desde el link?
- ¿Cuál es la duración mínima del bloque/grilla de agenda (ej. intervalos de 15 o 30 minutos) para calcular los horarios disponibles?
- ¿Qué estrategia de bloqueo/transacción en base de datos se usará para evitar reservas simultáneas (ej. lock optimista/pesimista o restricción única por profesional/sillón/franja)?
- ¿Qué funcionalidades de los competidores relevados no tienen evidencia pública verificable (tarifas exactas, cumplimiento normativo argentino explícito, APIs públicas) y quedarán marcadas como "No evidenciado"?
