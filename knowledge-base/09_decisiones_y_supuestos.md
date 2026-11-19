# Decisiones y Supuestos

## Decisiones documentadas

### DD-01 — Stack tecnológico
**Decisión**: Java 21 + Spring Boot 3 + Gradle + JPA/Hibernate + API REST (backend); TypeScript Vanilla servido estático (frontend); PostgreSQL local (ejecución); H2 en memoria (tests); SMTP/Gmail para email.
**Contexto**: El equipo académico (UTN, grupo de 3) ya eligió este stack. Presupuesto $0 obliga a tecnologías gratuitas y de código abierto.
**Alternativas consideradas**: Node.js + Express, Python + FastAPI, .NET Core, frontend con React/Vue/Angular.
**Justificación**: Conocimiento del equipo, ecosistema maduro, despliegue sencillo en un solo proceso, cumple presupuesto $0.
**Trade-offs aceptados**: Menos desacoplamiento backend/frontend que una SPA separada; sin embargo, evita CORS y complejidad de despliegue, crítico para 6 días de desarrollo.

### DD-02 — Enfoque arquitectónico: Monolito
**Decisión**: Monolito Spring Boot que expone API REST y sirve frontend estático desde el mismo proceso.
**Contexto**: Plazo de 6 días, equipo pequeño, un solo despliegue.
**Alternativas consideradas**: Backend API + frontend SPA desacoplado (CDN/nginx); microservicios.
**Justificación**: Un solo proceso para levantar y desplegar, sin problemas de CORS, menor complejidad operativa.
**Trade-offs aceptados**: Menos desacoplamiento; escalado horizontal limitado (no crítico para el plazo ni para consultorios pequeños).

### DD-03 — Anti-solapamiento por profesional y sillón/box
**Decisión**: Validación estricta de no solapamiento tanto por profesional como por sillón/box, con turnos consecutivos borde a borde permitidos.
**Contexto**: Reglas de negocio obligatorias del dominio odontológico.
**Alternativas consideradas**: Solo anti-solapamiento por profesional (ignorar sillón); no permitir turnos consecutivos.
**Justificación**: El sillón es un recurso físico independiente; permitir turnos consecutivos evita huecos innecesarios en la agenda.
**Trade-offs aceptados**: Mayor complejidad en la consulta de disponibilidad (dos dimensiones de exclusión).

### DD-04 — Registro por DNI y recuperación por Gmail
**Decisión**: El paciente se registra con nombre+DNI+contraseña; el Gmail de recuperación se vincula una sola vez después del primer login; login con DNI+contraseña; recuperación por Gmail o blanqueo presencial.
**Contexto**: Definido en el Discovery. DNI es identificador único en Argentina.
**Alternativas consideradas**: Registro con email como identificador; login solo con email.
**Justificación**: DNI es el identificador natural del paciente argentino; reduce fricción en el ingreso habitual.
**Trade-offs aceptados**: El Gmail no es obligatorio para entrar, solo para recuperación; requiere flujo de blanqueo presencial.

### DD-05 — Datos ficticios en repo/seeds/tests
**Decisión**: Nunca almacenar datos reales de pacientes en el repositorio, seeds ni tests.
**Contexto**: Ley 25.326 Argentina, protección de datos sensibles de salud.
**Alternativas consideradas**: Usar datos reales anonimizados.
**Justificación**: Cumplimiento normativo y privacidad desde el diseño.
**Trade-offs aceptados**: Los datos de prueba no representan casos reales completos.

## Supuestos inferidos

### SU-01 — system_type = web_app
**Supuesto**: El sistema es una aplicación web completa (backend API REST + interfaz web liviana).
**Origen**: Confirmado en ronda estratégica de kb-creator (P0-sys).
**Riesgo si es falso**: Si el usuario quería solo API, el frontend estático sería innecesario.
**Cómo validar**: Confirmar con el usuario antes de implementar el frontend.

### SU-02 — scale = equipo pequeño (< 50 personas)
**Supuesto**: Consultorios unipersonales y pequeños (1-5 profesionales, 1-3 sillones/boxes).
**Origen**: Confirmado en ronda estratégica de kb-creator (P0-scale).
**Riesgo si es falso**: Si se espera multi-tenant desde el día 1, la arquitectura necesitaría separación de datos por tenant.
**Cómo validar**: Confirmar con el usuario antes de modelar datos multi-tenant.

### SU-03 — needs_infra = true
**Supuesto**: El sistema requiere infraestructura propia (SMTP/Gmail para email, PostgreSQL local, H2 para tests).
**Origen**: Confirmado en ronda estratégica de kb-creator (P4).
**Riesgo si es falso**: Si no se necesita email, se simplifica la arquitectura.
**Cómo validar**: Confirmar que el envío de email es requerido en el MVP.

### SU-04 — Multi-sucursal es post-MVP
**Supuesto**: La funcionalidad multi-sucursal está fuera del alcance de la versión 1.0.
**Origen**: Definido en el Discovery (funcionalidades opcionales).
**Riesgo si es falso**: Si se requiere multi-sucursal en el MVP, el modelo de datos y la arquitectura necesitan ajuste.
**Cómo validar**: Confirmar con el usuario antes de finalizar el alcance v1.

### SU-05 — Plazo de 6 días es realista para el MVP quirúrgico
**Supuesto**: El alcance definido (registro, roles, gestión de turnos, anti-solapamiento, recordatorios email) es completable en 6 días por un equipo de 3.
**Origen**: Restricción de plazo del Discovery (miércoles 25 nov 2026).
**Riesgo si es falso**: Scope creep o entrega incompleta.
**Cómo validar**: Revisar el plan de sprint y priorizar las funcionalidades imprescindibles.
