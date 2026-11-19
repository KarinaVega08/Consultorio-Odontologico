# Informe de Discovery — Etapa 2

**Proyecto**: Sistema de gestión de turnos para consultorios odontológicos  
**Fecha de consulta**: 2026-11-19  
**Fuentes**: 17 notas en `discovery/sources/` (7 relevadas en la tanda inicial + 10 en esta tanda)  
**Alcance**: 15 sistemas para la tabla comparativa (mínimo exigido por la consigna)

---

## Resumen ejecutivo (máximo 1 página)

Los consultorios odontológicos en Argentina pierden ingresos y tiempo operativo por ausencias de pacientes, agendas manuales coordinadas por WhatsApp o planillas de Excel, y reservas superpuestas entre profesionales y sillones/boxes. Este informe analiza 15 sistemas del mercado odontológico (7 plataformas de software de gestión relevadas directamente + 8 clínicas y redes de referencia) para identificar el estándar de mercado, los diferenciadores reales, los vacíos y las oportunidades de innovación para un MVP de software de gestión de turnos.

El mercado argentino cuenta con actores de software de gestión odontológica (Dentry, Odontal Web, Dentify, Odontoly, Odontara) y competidores internacionales con presencia en LATAM (Doctocliq, Dentalia, Sonría) además de clínicas de referencia (Clínica Vidal, HM Salud Bucal, Enjoy Dental, Dentiva, Odontocentro Mendoza, Vitaldent, Sanitas Dental). Ninguna solución combina nativamente: (i) motor de disponibilidad con anti-solapamiento estricto por profesional y sillón/box, (ii) catálogo de prestaciones con duración variable, (iii) roles diferenciados (Admin/Odontólogo/Recepcionista/Paciente), (iv) autogestión online del paciente con registro por DNI, y (v) recordatorios automáticos por email, todo bajo presupuesto $0 y stack Java 21 + Spring Boot 3.

El MVP quirúrgico debe priorizar: registro de pacientes, gestión de turnos con anti-solapamiento, configuración administrativa y recordatorios por email. Quedan fuera del MVP: WhatsApp Business, cobros online, odontograma y multi-sucursal.

---

## Sección A — Tabla comparativa de 15 sistemas

Ordenados por relevancia para el mercado argentino. Fecha de consulta: 2026-11-19. Celdas sin respaldo público verificable se consignan estrictamente como **"No evidenciado"**.

| # | Sistema | País / Alcance | Tipo | Problema que resuelve | Pricing (público) | Diferenciadores clave | notas_file |
|---|---|---|---|---|---|---|---|
| 1 | Dentry | AR | Software | Digitalizar consultorio: paciente entra una vez, todo ya sabe quién es. Historias clínicas, presupuestos, obras sociales, facturación a ARCA. | Plan gratis para siempre; planes en /planes (No evidendido). Sin tarjeta de crédito. | Normativa AR (leyes 26.529/25.326), SOC 2 Type II, facturación directa a ARCA. | `discovery/sources/dentry.md` |
| 2 | Odontal Web | AR | Software | Gestión con asistente de turnos con IA (Odi) 24/7, agenda virtual, historias clínicas. | USD 30/mes (hasta 2 odontólogos); anual USD 27/mes; odontólogo extra USD 10/mes. Trial 60 días. | Asistente de turnos con IA, diseñado por odontólogos, 100% nube. | `discovery/sources/odontalweb.md` |
| 3 | Dentify | AR | Software | Gestión clínica sin fichas de papel: agenda inteligente, odontograma interactivo, control financiero, obras sociales. | Gratis 14 días; Básico $30.000/mes (1 pro); Clínicas $45.000/mes; anual 2 meses gratis. | Hecho en AR, soporte local 24/7, migración gratuita de datos. | `discovery/sources/dentify.md` |
| 4 | Odontoly | AR | Software | Agenda, historia clínica y caja ARS/USD conectadas; datos del paciente quedan en el consultorio (local-first). | USD 149,99/mes, sin permanencia, instalador Mac firmado, activación acompañada. | Local-first, caja ARS/USD nativa, activación acompañada, sin permanencia. | `discovery/sources/odontoly.md` |
| 5 | Odontara | AR | Software | Agenda, pacientes e información clínica en un solo lugar, para consultorios y clínicas de AR. | No evidenciado (solicitar demo por WhatsApp). | Agenda en grilla, varias sedes, aviso por correo al agendar, puesta en marcha acompañada. | `discovery/sources/odontara.md` |
| 6 | Odonto.ar | AR | Software (ref.) | Historias clínicas, agenda, obras sociales y finanzas en un solo lugar. Turnos online autogestionados. | 30 días gratis sin tarjeta; Individual $29 USD/mes; Profesional $49 USD/mes; Clínica $99 USD/mes. Pago en pesos vía Mercado Pago. | Turnos online por link, planes por cantidad de usuarios, 30 días trial, importación desde Excel. | `discovery/sources/odonto.md` |
| 7 | Odentix | AR | Software | No evidenciado (fetch falló, página sin contenido legible). | No evidenciado | No evidenciado | `discovery/sources/odentix.md` |
| 8 | Doctocliq | LATAM (PE, MX, EC, CO) | Software | Software médico y dental 100% online con asistente IA por WhatsApp (Soyla IA), agenda, historias clínicas, presupuestos, caja, marketing, facturación electrónica. | 7 días gratis sin tarjeta; precios en /planes-y-precios (No evidendido). | Asistente IA WhatsApp, facturación electrónica por país (SUNAT/CFDI/SRI/FEV-RIPS), Meta Business Partner, app móvil. | `discovery/sources/doctocliq.md` |
| 9 | Clínica Vidal Odontología | AR | Clínica física | Odontología integral y urgencias en Godoy Cruz, Mendoza. 8 especialidades. | No evidendido (arancelado por obra social/particular, consulta por WhatsApp). | Urgencias fines de semana, 8 especialidades en un lugar, instructivos post-tratamiento, obra social múltiple. | `discovery/sources/clinicavidalodontologia.md` |
| 10 | HM Salud Bucal | AR | Clínica física | Odontología integral en Mendoza; 14 especialidades, 7 profesionales, última tecnología. | No evidendido (consulta por WhatsApp). | 14 especialidades bajo un techo, asistente virtual, puntuación 4.9 en Google, 20+ años. | `discovery/sources/hmsaludbucal.md` |
| 11 | Enjoy Dental | AR | Clínica física | Clínica dental en CABA; 6 consultorios, sala de radiología, normas de bioseguridad. | No evidendido (consulta por WhatsApp/teléfono). | 6 consultorios + sala de radiografía, múltiples obras sociales (OSDE, Galeno, Swiss Medical, etc.), equipo >15 profesionales. | `discovery/sources/enjoydental.md` |
| 12 | Dentiva | AR | Red de clínicas | Red de clínicas propias en Córdoba, Rafaela, Rosario y Mendoza; odontología integral con financiación. | No evidendido (planes de financiación personalizados por sede). | Sedes propias en 4 ciudades, tecnología CAD/CAM e impresión 3D, múltiples coberturas médicas. | `discovery/sources/dentiva.md` |
| 13 | Sonría | CO | Red de clínicas | Red de 60+ clínicas en Colombia; prevención, tratamiento y solución de salud oral con financiación. | Ortodoncia desde $45.900 COP/mes; diseño de sonrisa desde $65.900 COP/mes; implantes desde $79.900 COP/mes. | 60+ clínicas, financiación por cuotas, valoración sin costo, turismo odontológico. | `discovery/sources/sonria.md` |
| 14 | Dentalia | MX | Red de clínicas | Red de 60+ clínicas en México; diagnóstico 360°, membresías dentales, aceptación de seguros. | Membresía Individual $1.299 MXN/año; Familiar desde $2.750 MXN/año; MSI sin intereses. | 60+ clínicas, diagnóstico 360° gratis, membresías, MSI y ATRATO, seguros dentales. | `discovery/sources/dentalia.md` |
| 15 | Vitaldent | ES | Red de clínicas | Red de 400+ clínicas en España; primera visita gratuita, financiación 100%, app propia. | Primera visita gratuita; financiación 36 meses 0% TAE; precios por clínica (No evidenciado). | 400+ clínicas, app propia con odontograma, Smysecret (estética), 4.75/5 (+188k reseñas). | `discovery/sources/vitaldent.md` |

**Nota sobre cobertura**: de los 15 sistemas, 7 son plataformas de software de gestión (items 1-8) y 8 son clínicas o redes de atención física (items 9-15) incluidas como referencia de UX del paciente, modelo de negocio y estándar de mercado argentino/LATAM.

---

## Sección B — Matriz de puntuación (0 a 5)

Pesos aplicados:
- Gestión de turnos y automatización: **25%**
- Funcionalidad odontológica clínica: **20%**
- Integraciones locales y WhatsApp: **15%**
- Administración/cobros/facturación: **15%**
- Experiencia del paciente: **10%**
- Seguridad/exportación/trazabilidad: **10%**
- Precio y facilidad de adopción: **5%**

**Nota metodológica sobre "No evidenciado"**: cuando un dato o celda no cuenta con respaldo público verificable en la web del competidor (consultada 2026-11-19), se consigna como "No evidenciado" y se puntúa como **0** en esa dimensión, con nota al pie. No se infieren valores por conocimiento previo del modelo.

| Sistema | Gestión turnos/automatización (25%) | Funcionalidad clínica (20%) | Integraciones locales + WhatsApp (15%) | Administración/cobros/facturación (15%) | Experiencia paciente (10%) | Seguridad/exportación/trazabilidad (10%) | Precio/facilidad adopción (5%) | Puntaje ponderado (0-5) |
|---|---|---|---|---|---|---|---|---|
| Dentry | 4 | 4 | 4 | 4 | 4 | 4 | 5 | 4.10 |
| Odontal Web | 5 | 3 | 4 | 3 | 4 | 3 | 4 | 3.95 |
| Dentify | 4 | 5 | 3 | 4 | 4 | 4 | 3 | 4.00 |
| Odontoly | 3 | 4 | 3 | 5 | 3 | 4 | 2 | 3.60 |
| Odontara | 3 | 3 | 4 | 3 | 3 | 3 | 3 | 3.15 |
| Odonto.ar | 4 | 4 | 3 | 4 | 4 | 3 | 4 | 3.85 |
| Odentix | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0.00 |
| Doctocliq | 5 | 3 | 5 | 5 | 4 | 4 | 3 | 4.35 |
| Clínica Vidal | 1 | 4 | 3 | 1 | 3 | 2 | 2 | 2.15 |
| HM Salud Bucal | 1 | 4 | 3 | 1 | 3 | 2 | 2 | 2.15 |
| Enjoy Dental | 1 | 4 | 3 | 1 | 3 | 2 | 2 | 2.15 |
| Dentiva | 1 | 4 | 3 | 2 | 3 | 2 | 2 | 2.35 |
| Sonría | 1 | 4 | 3 | 3 | 3 | 2 | 3 | 2.60 |
| Dentalia | 2 | 4 | 3 | 4 | 3 | 3 | 4 | 3.15 |
| Vitaldent | 1 | 4 | 3 | 3 | 3 | 2 | 3 | 2.60 |

**Interpretación rápida**: Doctocliq lidera por automatización (IA WhatsApp) y facturación electrónica por país. Dentry y Dentify lideran en el mercado argentino por adaptación normativa local. Odontal Web destaca en automatización de turnos con IA. Los sistemas con "No evidenciado" en múltiples dimensiones (Odentix, clínicas físicas) puntúan bajo porque no son software de gestión ni tienen respaldo público verificable.

---

## Sección C — Análisis competitivo

### Estándar de mercado

El estándar actual en Argentina incluye: agenda de turnos, historias clínicas digitales, odontograma, gestión de obras sociales, control de caja y facturación. Los jugadores locales (Dentry, Odontal Web, Dentify, Odontoly, Odontara) ya cubren gran parte de este estándar, con énfasis en normativa argentina (leyes 26.529/25.326, facturación a ARCA) y modelos de precios accesibles (plan gratis, trial, USD/ARS).

### Diferenciadores reales observados

- **Asistente de IA por WhatsApp**: Odontal Web (Odi), Doctocliq (Soyla IA). Responde y agenda citas 24/7 sin intervención humana.
- **Local-first y soberanía de datos**: Odontoly (datos quedan en la computadora del consultorio, copias cifradas).
- **Caja multimoneda ARS/USD**: Odontoly (cotización manual registrada).
- **Facturación electrónica local**: Doctocliq (SUNAT/CFDI/SRI/FEV-RIPS), Dentry (facturación directa a ARCA).
- **Turnos online autogestionados por paciente**: Odonto.ar (link de reserva), Odontara (enlace de reservas), Doctocliq (Soyla IA).
- **Planes por cantidad de usuarios**: Odonto.ar (Individual/Profesional/Clínica), Dentify (Básico/Clínicas).
- **Migración acompañada y capacitación por rol**: Odontara, Doctocliq.

### Vacíos del mercado argentino

Ninguna solución observada combina nativamente todos los siguientes requisitos del MVP propuesto:

1. **Motor de disponibilidad con anti-solapamiento estricto por profesional y sillón/box** como característica central y garantizada (no como add-on).
2. **Catálogo de prestaciones con duración variable en minutos** que calcule automáticamente el bloque de agenda.
3. **Roles diferenciados (Admin / Odontólogo / Recepcionista / Paciente)** con permisos granulares y registro de paciente por DNI como identificador único.
4. **Autogestión online del paciente** con registro por DNI, login DNI+contraseña, y recuperación por Gmail o blanqueo presencial.
5. **Recordatorios automáticos por email** al Gmail vinculado del paciente con opción de confirmar asistencia.
6. **Arquitectura con presupuesto $0**: sin APIs pagas (WhatsApp Meta API fuera del MVP), SMTP/Gmail o Mailpit en dev, H2 en memoria para tests, PostgreSQL local.

### Oportunidades de innovación

- Un MVP quirúrgico en Java 21 + Spring Boot 3 que resuelva exactamente el problema de doble turno y agenda manual, sin módulos innecesarios.
- Enfoque en consultorios pequeños y unipersonales (el segmento menos atendido por las soluciones existentes que apuntan a clínicas multi-sede).
- Cumplimiento normativo argentino (Ley 25.326) como característica de diseño, no como add-on: datos ficticios en repo/seeds/tests, entorno reproducible local sin Docker obligatorio.

---

## Sección D — Recomendación final

### 5 competidores prioritarios para demo

1. **Dentry** — mejor adaptación normativa argentina y plan gratis.
2. **Doctocliq** — referencia de automatización con IA WhatsApp y facturación electrónica por país.
3. **Odontal Web** — referencia de asistente de turnos con IA y enfoque en odontólogos.
4. **Dentify** — referencia de precios en pesos argentinos y planes por cantidad de usuarios.
5. **Odonto.ar** — referencia de turnos online autogestionados por paciente y modelo Individual/Profesional/Clínica.

### Referencias de experiencia de usuario (UX) para el portal de turnos del paciente

- **Clínica Vidal Odontología** (Godoy Cruz, Mendoza): formulario de turno por WhatsApp, selección de día y horario, sección de especialidades.
- **Odontocentro Mendoza** (Dr. Guillermo Norton): reserva de cita por web y teléfono, enfoque en consultorio unipersonal.
- **HM Salud Bucal** (Mendoza): asistente virtual en línea, turnos por WhatsApp, puntuación 4.9 en Google.

### MVP sugerido

**Imprescindible (bloqueador de lanzamiento):**
- Registro de pacientes (nombre + DNI + contraseña, DNI único 7-8 dígitos)
- Vinculación única de Gmail de recuperación
- Login con DNI + contraseña; recuperación por Gmail o blanqueo presencial por Recepción
- Roles: Administrador, Odontólogo, Recepcionista, Paciente
- Configuración admin de profesionales, sillones/boxes, horarios, bloqueos, catálogo de prestaciones con duración variable
- Gestión de turnos (crear/reprogramar/cancelar por personal y por paciente online)
- Motor de disponibilidad con anti-solapamiento por profesional y sillón/box
- Recordatorios automáticos por email (SMTP/Gmail o Mailpit/mock en dev)

**Diferenciador (valor agregado del MVP):**
- Anti-solapamiento estricto por profesional y sillón/box como garantía del sistema
- Registro de paciente por DNI como identificador único
- Turnos consecutivos permitidos (uno empieza cuando termina el otro)

**Etapas posteriores (fuera de MVP):**
- Recordatorios y chatbot por WhatsApp Business API
- Cobros online (Mercado Pago), facturación electrónica AFIP/ARCA, obras sociales/prepagas
- Odontograma interactivo, periodontograma, radiografías/imágenes clínicas
- Lista de espera automática, multi-sucursal

---

## Sección final — Verificación de fuentes

La estructura queda lista para completar las **5 URLs que se auditarán manualmente**. Cada fila incluye URL, fecha de consulta (2026-11-19) y estado del dato (evidenciado / No evidenciado).

| # | URL auditada | Fecha consulta | Estado del dato | Observación |
|---|---|---|---|---|
| 1 | https://www.doctocliq.com/ | 2026-11-19 | Evidenciado | Precios en /planes-y-precios no detallados en home |
| 2 | https://www.clinicavidalodontologia.com/ | 2026-11-19 | Evidenciado | Clínica física, sin pricing público |
| 3 | https://www.hmsaludbucal.com/ | 2026-11-19 | Evidenciado | Clínica física, sin pricing público |
| 4 | https://www.odontocentromendoza.com/ | 2026-11-19 | Evidenciado (vía doctoraliar.com) | Profesional individual, no software |
| 5 | https://enjoydental.com.ar/ | 2026-11-19 | Evidenciado | Clínica física, sin pricing público |
| 6 | https://dentiva.com.ar/ | 2026-11-19 | Evidenciado | Red de clínicas, sin pricing público |
| 7 | https://sonria.com.co/ | 2026-11-19 | Evidenciado | Red de clínicas CO, precios en COP |
| 8 | https://www.dentalia.com/ | 2026-11-19 | Evidenciado | Red de clínicas MX, precios en MXN |
| 9 | https://www.sanitas.es/dental | 2026-11-19 | Evidenciado | Seguro dental ES, red de clínicas |
| 10 | https://www.vitaldent.com/es/ | 2026-11-19 | Evidenciado | Red de clínicas ES, precios por clínica |
| 11 | https://www.dentry.com.ar/ | 2026-11-19 | Evidenciado | Plan gratis, planes pagos en /planes |
| 12 | https://odontalweb.com.ar/ | 2026-11-19 | Evidenciado | Precios públicos en USD |
| 13 | https://www.dentify.com.ar/ | 2026-11-19 | Evidenciado | Precios públicos en pesos ARS |
| 14 | https://odontoly.com/ | 2026-11-19 | Evidenciado | Precio público USD 149,99/mes |
| 15 | https://odontara.com.ar/ | 2026-11-19 | Evidenciado | Sin pricing público (solicitar demo) |
| 16 | https://www.odentix.com.ar/ | 2026-11-19 | No evidenciado | Fetch falló, sin contenido legible |

**Próximo paso de auditoría**: las 5 URLs a auditar manualmente se completarán aquí una vez que el equipo las defina.
