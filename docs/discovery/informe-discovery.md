# Informe de Discovery — Gestión de Turnos para Consultorios Odontológicos

---

## Portada

- **Título**: Informe de Discovery: Análisis competitivo de sistemas de gestión de turnos para consultorios odontológicos en Argentina
- **Materia**: Desarrollo de Software — Trabajo Global Integrador (UTN FRM)
- **Integrantes**: Thiago, Mai, Kari, Mati, Caste *(completar nombres completos y legajos antes de exportar a PDF)*
- **Fecha**: 2026-11-20
- **Fuentes relevadas**: 18 plataformas de software en `discovery/sources/` (relevamiento actualizado 2026-11-20)

---

## Resumen ejecutivo

Los consultorios odontológicos en Argentina pierden ingresos y tiempo operativo por ausencias de pacientes sin aviso, agendas manuales coordinadas por WhatsApp o planillas de Excel, y reservas superpuestas entre profesionales y sillones/boxes. Este informe analiza **18 plataformas reales de software** con gestión de turnos y agenda (7 de origen argentino, 7 latinoamericanas con foco o presencia regional y 4 referencias internacionales de España y EE. UU.) para identificar el estándar de mercado, los diferenciadores reales, los vacíos locales y el alcance óptimo de un MVP.

**Hallazgos principales:**
1. En Argentina existen soluciones locales activas (Dentry, Odontal Web, Dentify, Odonto.ar, Odontoly, Odontara) que cubren historia clínica, odontograma y caja/obras sociales, pero la mayoría no publica documentación técnica verificable sobre **prevención estricta de solapamientos por sillón/box** ni **cálculo dinámico de disponibilidad según duración variable de cada prestación**.
2. Los competidores regionales líderes (Dentalink, Doctocliq, AgendaPro) apuestan fuerte a WhatsApp Business API e inteligencia artificial, pero con precios en dólares o por cotización comercial cerrada.
3. Ninguna solución de presupuesto $0 combina nativamente: (i) motor de disponibilidad con anti-solapamiento estricto por profesional y por sillón/box (permitiendo turnos consecutivos borde a borde), (ii) catálogo de prestaciones con duración variable, (iii) autogestión online del paciente con registro único por DNI (7-8 dígitos), (iv) blanqueo presencial por Recepción, y (v) recordatorios por email (SMTP/Mailpit en desarrollo), bajo cumplimiento de la Ley 25.326.

**MVP recomendado:** enfocado exclusivamente en registro/login por DNI, configuración de profesionales/sillones/horarios/bloqueos/prestaciones, motor de disponibilidad + gestión de turnos sin solapamientos y recordatorios por email. Quedan para etapas posteriores WhatsApp Business API, Mercado Pago, AFIP/ARCA, odontograma y multi-sucursal.

---

## Sección A — Tabla comparativa de 18 sistemas de software

Ordenados por relevancia para el mercado argentino. En todo el informe se distingue estrictamente entre:
- **Comprobado**: demostrado en documentación pública, página de precios detallada, manual técnico o producto visible.
- **Afirmación comercial**: declarado por el proveedor en su marketing sin demostración pública.
- **No evidenciado**: sin respaldo público verificable al momento de la consulta.

| # | Producto / Proveedor | País · Segmento · Modalidad | Agenda (multi-prof / sillón / solapamiento) | Reserva online | Recordatorios | Clínica & Administración | Seguridad / Normativa | Modelo comercial (público) | Fuente principal y fecha |
|---|---|---|---|---|---|---|---|---|---|
| 1 | **Dentry** (Dentry Argentina) | AR · Independientes y consultorios · SaaS web | Comprobado (vista "Mi día" y semanal; asientos extra). Sillón/solapamiento: **No evidenciado** | **No evidenciado** | Comprobado (recordatorios por email desde Basic; WhatsApp: Afirmación comercial) | Comprobado (odontograma, HC, presupuestos, cuenta corriente; obras sociales/ARCA en Pro) | Afirmación comercial (Leyes 26.529/25.326, SOC 2 Type II) | Comprobado: Gratis `$0` (50 pac./100 turnos/mes); Basic `$29.999 ARS/mes`; Pro `$49.999 ARS/mes` | https://www.dentry.com.ar/planes · 2026-11-20 |
| 2 | **Odontal Web** | AR · Independientes y clínicas · SaaS web | Afirmación comercial (agenda virtual). Sillón/solapamiento: **No evidenciado** | Comprobado (asistente IA "Odi" 24/7 para reservar/cancelar) | **No evidenciado** (foco en chat 24/7) | Afirmación comercial (HC digital). Admin/ARCA: **No evidenciado** | Afirmación comercial (backups, cifrado, exportación libre) | Comprobado: `USD 30/mes` hasta 2 pro (anual `USD 27`), trial 60 días | https://odontalweb.com.ar/ · 2026-11-19 |
| 3 | **Dentify** (IB Digital) | AR · Independientes y clínicas · SaaS web | Afirmación comercial (agenda por profesional/práctica). Sillón/solapamiento: **No evidenciado** | **No evidenciado** | **No evidenciado** | Comprobado (odontograma, HC, caja diaria, obras sociales) | Afirmación comercial (migración y copias en nube) | Comprobado: 14 días gratis; Básico `$30.000 ARS/mes`; Clínicas `$45.000 ARS/mes` | https://www.dentify.com.ar/ · 2026-11-19 |
| 4 | **Odonto.ar** | AR · Independientes y clínicas hasta 10 u. · SaaS web | Afirmación comercial (agenda día/semana/mes). Sillón/solapamiento: **No evidenciado** | Comprobado (pacientes reservan solos por link público) | **No evidenciado** | Comprobado (odontograma, HC auditable, obras sociales, finanzas) | Afirmación comercial (registro de quién modificó HC) | Comprobado: 30 días gratis; Individual `USD 29`, Pro `USD 49`, Clínica `USD 99/mes` (pago ARS Mercado Pago) | https://odonto.ar/ · 2026-11-19 |
| 5 | **Odontoly** | AR · Consultorios independientes · Local-first (macOS) | Afirmación comercial (agenda y tareas de equipo). Sillón/solapamiento: **No evidenciado** | Comprobado (solicitud por enlace, equipo confirma) | **No evidenciado** (WhatsApp/SMS marcados como próximos) | Comprobado (HC, odontograma, presupuestos, caja ARS/USD) | Comprobado (local-first, copias completas cifradas) | Comprobado: `USD 149,99/mes`, sin permanencia | https://odontoly.com/ · 2026-11-19 |
| 6 | **Odontara** | AR · Consultorios y clínicas multi-sede · SaaS nube | Afirmación comercial (agenda en grilla/lista por profesional). Sillón/solapamiento: **No evidenciado** | **No evidenciado** | Comprobado (aviso por correo al agendar) | Afirmación comercial (HC, odontograma). Admin/caja: **No evidenciado** | Afirmación comercial (roles por usuario) | **No evidenciado** (demo por WhatsApp) | https://odontara.com.ar/ · 2026-11-19 |
| 7 | **Odentix** | AR · Consultorios · **No evidenciado** | **No evidenciado** (sitio sin contenido legible) | **No evidenciado** | **No evidenciado** | **No evidenciado** | **No evidenciado** | **No evidenciado** | https://www.odentix.com.ar/ · 2026-11-20 |
| 8 | **Dentalink** (HealthAtom) | CL / LATAM (incl. AR) · Independientes a redes · SaaS nube | Afirmación comercial (agenda por profesional, sillón y sucursal) | Comprobado (Contact Center IA + link de reserva) | Afirmación comercial (WhatsApp / SMS) | Comprobado (odontograma, Clinical Copilot IA, caja/comisiones) | Afirmación comercial. Normativa AR: **No evidenciado** | **No evidenciado** (planes Esencial/Pro/Titanium bajo cotización) | https://www.softwaredentalink.com/planes · 2026-11-20 |
| 9 | **Doctocliq** | PE / LATAM · Clínicas dentales y médicas · SaaS + App | Afirmación comercial (agenda por consultorio y doctor) | Comprobado (Soyla IA agenda 24/7 por WhatsApp) | Comprobado (confirmación automática WhatsApp) | Comprobado (HC, presupuestos, caja, facturación SUNAT/CFDI/SRI/FEV) | Afirmación comercial (Meta Business Partner). Normativa AR: **No evidenciado** | Comprobado trial 7 días gratis; precios detallados: **No evidenciado** en HTML estático | https://www.doctocliq.com/planes-y-precios · 2026-11-20 |
| 10 | **AgendaPro** | CL / LATAM (sitio AR) · Salud y dental · SaaS + App | Afirmación comercial (por profesional, sillón y duración de práctica) | Afirmación comercial (reserva online 24/7) | Afirmación comercial (WhatsApp / email) | Afirmación comercial (HC, odontograma, caja, Mercado Pago) | Afirmación comercial (backups automáticos) | **No evidenciado** en página oficial (renderizado dinámico JS) | https://agendapro.com/ar/precios · 2026-11-20 |
| 11 | **Dentidesk** | CL / LATAM · Clínicas y universidades · SaaS + App | Afirmación comercial (agenda multisucursal). Sillón/solapamiento: **No evidenciado** | Afirmación comercial (widget reserva online) | Afirmación comercial (SMS / email) | Comprobado (odontograma, periodontograma, firma digital, Alegra RD) | Comprobado (adaptación Ley Protección de Datos Chile) | **No evidenciado** (solicitar demo gratis) | https://www.dentidesk.com/ · 2026-11-20 |
| 12 | **Doctoralia PRO** (Docplanner) | ES / PL (sitio AR) · Clínicas y especialistas · Marketplace + SaaS | Afirmación comercial (calendario online, lista de espera). Sillón: **No evidenciado** | Comprobado (reserva desde directorio público) | Afirmación comercial (recordatorios automáticos) | Afirmación comercial (HC en planes pagos). Odontograma/ARCA: **No evidenciado** | **No evidenciado** | Perfil gratis sin reserva online; precios PRO: **No evidenciado** (bloqueo en fetch) | https://www.doctoralia.com.ar/ · 2026-11-20 |
| 13 | **Nimbo** (Ecaresoft) | MX / LATAM (incl. AR) · Clínicas y dentistas · SaaS nube | Comprobado en menú (agenda colaborativa). Sillón/solapamiento: **No evidenciado** | Comprobado (portal del paciente) | Comprobado (WhatsApp, SMS, email) | Comprobado (expediente clínico, recetas, facturación, inventarios) | Afirmación comercial (sección Seguridad) | **No evidenciado** (cotización modular por organización) | https://www.nimbo-x.com/ · 2026-11-20 |
| 14 | **Encuadrado** | CL / LATAM · Independientes salud · SaaS nube | Afirmación comercial. Sillón/solapamiento: **No evidenciado** | Comprobado (pacientes se agendan solos 24/7) | Comprobado (WhatsApp y correo programables) | Comprobado (ficha certificada, recetas, pagos, boletas SII Chile) | Afirmación comercial (ficha certificada) | **No evidenciado** en URL directa (`/precios` devolvió 404) | https://www.encuadrado.com/ · 2026-11-20 |
| 15 | **Clinic Cloud** (Doctoralia ES) | ES · Clínicas médicas/dentales · SaaS nube | Comprobado (agenda multi-profesional y multi-sede) | Afirmación comercial (reserva online) | Comprobado (WhatsApp, SMS y email automáticos) | Comprobado (historial con IA, facturación Verifactu/TicketBAI, firma digital) | Comprobado (cumplimiento fiscal ES y firma digital) | **No evidenciado** en URL directa (`/precios/` devolvió 404) | https://clinic-cloud.com/ · 2026-11-20 |
| 16 | **DriCloud** | ES · Clínicas y consultas · SaaS nube (Azure) | Comprobado (agenda multi-consulta, colores, lista de espera automática) | Comprobado (cita online 24h + portal del paciente) | Comprobado (recordatorios automáticos + chatbot IA) | Comprobado (HC con IA, pruebas diagnósticas) | Comprobado (RGPD, Azure cifrado, firma digital) | Afirmación comercial (~42 €/mes en referencias; demo online) | https://dricloud.com/precios/ · 2026-11-20 |
| 17 | **Nubimed** | ES · Clínicas dentales y médicas · SaaS + App | Comprobado (agenda clínica dental multi-profesional simultánea) | Comprobado (cita online) | Comprobado (integración WhatsApp y recordatorios) | Comprobado (odontograma, periodontograma, facturación, inventario, mutuas) | Afirmación comercial (firma digital) | Página `/precios/` activa sin tarifas numéricas estáticas | https://www.nubimed.com/software-clinica-dental/ · 2026-11-20 |
| 18 | **Open Dental** | EE. UU. · Consultorios y redes · Open Source / Self-hosted + Cloud | **Comprobado en manual** (multi-provider, sillones/operatories, *blockouts*, *ASAP/Recall*) | Comprobado (*Web Sched* nuevo/existente) | Comprobado (*eReminders* / *eConfirmations* SMS y email) | Comprobado (odontograma, planes, fee schedules) | Comprobado (permisos granulares, control self-hosted) | Comprobado: `USD 199/mes` (primeros 12 meses), luego `USD 149/mes` hasta 3 pro | https://www.opendental.com/site/fees.html · 2026-11-20 |

---

## Sección B — Matriz de puntuación (0 a 5)

### Nota metodológica
- **Pesos**: Gestión de turnos y automatización (`25%`), Funcionalidad odontológica clínica (`20%`), Integraciones locales y WhatsApp (`15%`), Administración/cobros/facturación (`15%`), Experiencia del paciente (`10%`), Seguridad/exportación/trazabilidad (`10%`), Precio y facilidad de adopción (`5%`).
- **Criterio de puntuación**:
  - `0` = **No evidenciado** (sin respaldo público verificable; no se infiere).
  - `1–2` = Solo **afirmación comercial** o cobertura parcial sin demostración.
  - `3` = **Comprobado** básico.
  - `4` = **Comprobado** y completo.
  - `5` = **Comprobado**, completo y diferencial frente al mercado (o en Precio: plan gratuito/precio bajo público con prueba).

| # | Sistema | Turnos y automatización (25%) | Clínica odontológica (20%) | Integraciones locales + WhatsApp (15%) | Admin / cobros / facturación (15%) | Experiencia paciente (10%) | Seguridad / trazabilidad (10%) | Precio / adopción (5%) | **Total ponderado (0–5)** |
|---|---|---|---|---|---|---|---|---|---|
| 1 | Dentry | 3 | 4 | 3 | 4 | 2 | 2 | 5 | **3.25** |
| 2 | Odontal Web | 4 | 3 | 2 | 0 | 4 | 2 | 4 | **2.70** |
| 3 | Dentify | 3 | 4 | 2 | 3 | 0 | 2 | 4 | **2.70** |
| 4 | Odonto.ar | 3 | 4 | 3 | 4 | 4 | 2 | 4 | **3.40** |
| 5 | Odontoly | 3 | 4 | 1 | 4 | 3 | 4 | 2 | **3.10** |
| 6 | Odontara | 3 | 3 | 1 | 0 | 2 | 2 | 0 | **1.90** |
| 7 | Odentix | 0 | 0 | 0 | 0 | 0 | 0 | 0 | **0.00** |
| 8 | Dentalink | 4 | 4 | 3 | 2 | 2 | 1 | 0 | **2.85** |
| 9 | Doctocliq | 4 | 3 | 4 | 4 | 3 | 2 | 2 | **3.40** |
| 10 | AgendaPro | 3 | 2 | 3 | 3 | 3 | 2 | 0 | **2.55** |
| 11 | Dentidesk | 2 | 4 | 1 | 2 | 2 | 3 | 0 | **2.25** |
| 12 | Doctoralia PRO | 2 | 1 | 1 | 0 | 4 | 1 | 1 | **1.40** |
| 13 | Nimbo | 3 | 3 | 3 | 3 | 3 | 2 | 0 | **2.75** |
| 14 | Encuadrado | 4 | 2 | 3 | 3 | 4 | 2 | 0 | **2.90** |
| 15 | Clinic Cloud | 3 | 3 | 3 | 3 | 2 | 3 | 0 | **2.75** |
| 16 | DriCloud | 4 | 3 | 3 | 2 | 4 | 4 | 2 | **3.25** |
| 17 | Nubimed | 3 | 4 | 3 | 3 | 2 | 2 | 1 | **2.90** |
| 18 | Open Dental | 5 | 4 | 2 | 3 | 3 | 4 | 3 | **3.65** |

**Lectura crítica**: **Open Dental** lidera el puntaje técnico (`3.65`) por ser el único con manual técnico público que demuestra su lógica de sillones/operatories, bloqueos y listas de espera, aunque carece de adaptación fiscal argentina. En el mercado local y regional lideran **Odonto.ar** (`3.40`), **Doctocliq** (`3.40`) y **Dentry** (`3.25`), este último destacado por publicar precios transparentes en pesos argentinos incluyendo un plan gratuito permanente. **Odentix** queda en `0.00` al no devolver contenido público verificable.

---

## Sección C — Análisis competitivo

### 1. Estándar de mercado
Las plataformas odontológicas vigentes en Argentina y LATAM cubren como base esperada: agenda visual (diaria/semanal) por profesional, ficha clínica con odontograma, almacenamiento de radiografías y control básico de pagos/caja. Los jugadores locales (Dentry, Dentify, Odonto.ar) suman soporte para obras sociales y prepagas, y modelos comerciales accesibles con prueba gratuita o plan inicial sin tarjeta.

### 2. Diferenciadores reales observados
- **Asistente conversacional con IA por WhatsApp**: Odontal Web (*Odi*) y Doctocliq (*Soyla IA*), orientados a responder y agendar 24/7.
- **Transparencia comercial y plan gratuito permanente**: Dentry (`$0` hasta 50 pacientes / 100 turnos/mes, con precios públicos en ARS) y Odonto.ar (pago en ARS vía Mercado Pago sin débito automático).
- **Arquitectura *local-first* y caja bimoneda**: Odontoly (datos alojados en la PC del consultorio, copias cifradas y caja ARS/USD).
- **Documentación técnica abierta y lógica multi-sillón explícita**: Open Dental (manual público por versión detallando *operatories*, *blockouts* y listas *ASAP*).
- **Lista de espera automatizada**: DriCloud y Doctoralia PRO para cubrir huecos por cancelaciones.

### 3. Vacíos frecuentes del mercado argentino
1. **Reglas estrictas de anti-solapamiento por doble recurso (profesional + sillón/box)**: la mayoría declara agenda multi-profesional como afirmación comercial, pero casi ninguno documenta cómo garantiza que un sillón físico no sea asignado a dos profesionales al mismo tiempo o cómo maneja turnos consecutivos borde a borde.
2. **Motor de disponibilidad por duración variable de prestación**: cálculo automático de franjas libres según la duración real de cada práctica (ej. 30 vs. 45 vs. 60 min) dentro del horario laboral y respetando bloqueos.
3. **Flujo de autogestión simple pensado para el paciente argentino**: registro e ingreso con DNI (7–8 dígitos) y contraseña, sin exigir email obligatorio para operar, con recuperación por Gmail opcional o **blanqueo presencial por Recepción**.
4. **Trazabilidad sin borrado físico**: conservación de turnos cancelados en estado `CANCELADO` con liberación inmediata del hueco.

### 4. Oportunidades de innovación
- Construir un **MVP quirúrgico** centrado en integridad de agenda (Java 21 + Spring Boot 3, validaciones transaccionales anti-solapamiento por profesional y por sillón/box) y presupuesto `$0` (SMTP/Mailpit, H2 en memoria para tests, PostgreSQL local).
- Diseñar desde cero con **privacidad por diseño** conforme a la Ley 25.326 (cero datos reales en repositorios, seeds o pruebas).
- Atender al consultorio pequeño y unipersonal argentino que hoy opera con WhatsApp/Excel y no necesita pagar por módulos complejos de odontograma o facturación electrónica desde el día 1.

---

## Sección D — Recomendación final

### 5 competidores prioritarios para analizar mediante demo
1. **Dentry** — referente local argentino por plan gratuito, precios públicos en ARS y cumplimiento declarado de Ley 25.326/ARCA.
2. **Odonto.ar** — referente argentino de turnos online por link autogestionado y modelo escalonado por usuarios.
3. **Dentalink** — líder regional en LATAM con agenda por profesional, sillón y sucursal.
4. **Doctocliq** — referencia en automatización y recordatorios/agenda por WhatsApp.
5. **Odontal Web** — referencia local en simplicidad de uso y asistente 24/7 para turnos.

### 3 productos de referencia para experiencia de usuario (UX)
1. **Odonto.ar**: flujo directo de reserva online por enlace público sin fricción para el paciente.
2. **DriCloud**: agenda visual multi-consulta con colores por tipo de cita y portal del paciente integrado.
3. **Open Dental**: modelo de *blockouts*, vista por sillón (*operatory*) y prevención visual de conflictos de agenda.

### MVP sugerido

**Imprescindible (alcance del MVP v1.0):**
- Registro de pacientes (nombre real, DNI único de 7–8 dígitos, contraseña) y vinculación única opcional de Gmail de recuperación.
- Login con DNI + contraseña; recuperación por Gmail o blanqueo presencial por Recepción/Admin.
- Roles con RBAC: Administrador, Odontólogo, Recepcionista, Paciente.
- Configuración administrativa: profesionales, sillones/boxes, horarios de atención, bloqueos y catálogo de prestaciones con duración variable.
- Motor de disponibilidad y gestión de turnos (crear, reprogramar, cancelar sin borrado físico) con validación estricta anti-solapamiento por profesional y por sillón/box (permitiendo turnos consecutivos borde a borde).
- Recordatorios automáticos por email (SMTP/Mailpit) con opción de confirmar asistencia.

**Diferenciador del MVP:**
- Garantía transaccional de cero doble reserva tanto por profesional como por sillón/box.
- Identificación de pacientes por DNI único con blanqueo presencial para pacientes con baja alfabetización digital.
- Cumplimiento estricto Ley 25.326 desde la base de código (solo datos ficticios en repo y tests).

**Etapas posteriores (fuera de MVP):**
- Recordatorios y bot por WhatsApp Business API.
- Cobros online (Mercado Pago), obras sociales/prepagas y facturación electrónica AFIP/ARCA.
- Odontograma interactivo, periodontograma y radiografías.
- Lista de espera automática y soporte multi-sucursal.

---

## Sección final — Verificación de fuentes

### Hallazgos ya detectados y corregidos frente a la versión previa (2026-11-19 → 2026-11-20)
1. **Dentry (`/planes`)**: la versión previa afirmaba *"planes en /planes (No evidendido)"*. Al consultar directamente `https://www.dentry.com.ar/planes`, se comprobó que los precios y límites están publicados (`Gratis $0`, `Basic $29.999/mes`, `Pro $49.999/mes`). Se corrigió con **Ajustar**.
2. **Clínicas físicas mezcladas como software**: los ítems 9–15 de la versión anterior eran clínicas dentales físicas (Clínica Vidal, HM Salud Bucal, Enjoy Dental, Dentiva, Sonría, Dentalia, Vitaldent), no plataformas de software de agenda. Se reemplazaron por 11 sistemas de software reales (Dentalink, AgendaPro, Dentidesk, Doctoralia PRO, Nimbo, Encuadrado, Clinic Cloud, DriCloud, Nubimed, Open Dental) para cumplir el mínimo de 15 sistemas de software.
3. **Puntajes inflados sin respaldo en Sección B**: en la versión previa las clínicas físicas tenían puntaje `4` en funcionalidad clínica y `3` en seguridad pese a la regla `No evidenciado = 0`. Se recalcularon todas las filas aplicando la rúbrica estricta.

### Auditoría manual del equipo (completar al menos 5 URLs de distinto tipo)

> **Instrucción para el equipo**: abran personalmente estas 5 URLs en el navegador, verifiquen si lo que dice el informe está realmente allí y completen la columna **Resultado de verificación manual** antes de exportar a PDF.

| # | Tipo de fuente | Sistema | URL a auditar manualmente | Qué afirma este informe | Resultado de verificación manual (completar) |
|---|---|---|---|---|---|
| 1 | Sitio oficial (home) | Dentify | https://www.dentify.com.ar/ | Ofrece agenda por profesional/práctica, odontograma y planes Básico ($30.000) / Clínicas ($45.000). | *(Pendiente: abrir y confirmar si coincide hoy)* |
| 2 | Página de precios | Dentry | https://www.dentry.com.ar/planes | Plan Gratis $0 (50 pac/100 turnos), Basic $29.999/mes (recordatorios email), Pro $49.999/mes. | *(Pendiente: abrir y confirmar si coincide hoy)* |
| 3 | Manual técnico / Ayuda | Open Dental | https://www.opendental.com/manual/appointments.html | Documenta módulo de turnos con vistas por proveedor/sillón, blockouts y listas Recall/ASAP. | Verificado sin cambios — Revisor: Thiago, Fecha: 2026-11-22. Confirma columnas por sillón/box (Operatory 1, 2, 3, Hygiene Op), bloqueos horarios (Blockouts), vistas Day/Week, estados de confirmación y listas Recall/Unsched. |
| 4 | Página de producto LATAM | Dentalink | https://www.softwaredentalink.com/ | Destaca Clinical Copilot IA, Contact Center IA y planes bajo cotización (sin precios públicos). | *(Pendiente: abrir y confirmar si coincide hoy)* |
| 5 | Sitio sin contenido / falla | Odentix | https://www.odentix.com.ar/ | El fetch automático no devolvió contenido legible (marcado como `No evidenciado`). | *(Pendiente: abrir en navegador con JS para verificar si carga o está caído)* |

---

## Referencias completas

1. Dentry — Sitio oficial y planes: https://www.dentry.com.ar/ · https://www.dentry.com.ar/planes (consultada 2026-11-20)
2. Odontal Web — Sitio oficial: https://odontalweb.com.ar/ (consultada 2026-11-19)
3. Dentify — Sitio oficial: https://www.dentify.com.ar/ (consultada 2026-11-19)
4. Odonto.ar — Sitio oficial: https://odonto.ar/ (consultada 2026-11-19)
5. Odontoly — Sitio oficial: https://odontoly.com/ (consultada 2026-11-19)
6. Odontara — Sitio oficial: https://odontara.com.ar/ (consultada 2026-11-19)
7. Odentix — Sitio oficial: https://www.odentix.com.ar/ (consultada 2026-11-20; sin contenido legible en fetch)
8. Dentalink — Sitio oficial y planes: https://www.softwaredentalink.com/ · https://www.softwaredentalink.com/planes (consultada 2026-11-20)
9. Doctocliq — Sitio oficial y planes: https://www.doctocliq.com/ · https://www.doctocliq.com/planes-y-precios (consultada 2026-11-20)
10. AgendaPro — Sitio oficial Argentina: https://agendapro.com/ar/precios (consultada 2026-11-20)
11. Dentidesk — Sitio oficial: https://www.dentidesk.com/ (consultada 2026-11-20)
12. Doctoralia Argentina — Sitio oficial: https://www.doctoralia.com.ar/ (consultada 2026-11-20)
13. Nimbo — Sitio oficial: https://www.nimbo-x.com/ (consultada 2026-11-20)
14. Encuadrado — Sitio oficial: https://www.encuadrado.com/ (consultada 2026-11-20)
15. Clinic Cloud — Sitio oficial: https://clinic-cloud.com/ (consultada 2026-11-20)
16. DriCloud — Sitio oficial y precios: https://dricloud.com/ · https://dricloud.com/precios/ (consultada 2026-11-20)
17. Nubimed — Vertical dental y precios: https://www.nubimed.com/software-clinica-dental/ · https://www.nubimed.com/precios/ (consultada 2026-11-20)
18. Open Dental — Manual de turnos y tarifas oficiales: https://www.opendental.com/manual/appointments.html · https://www.opendental.com/site/fees.html (consultada 2026-11-20)

---

## Anexo — Clínicas físicas relevadas como referencia secundaria de experiencia de paciente

En una pasada exploratoria previa (2026-11-19) se documentaron 8 clínicas/redes físicas (`discovery/sources/clinicavidalodontologia.md`, `hmsaludbucal.md`, `enjoydental.md`, `dentiva.md`, `odontocentromendoza.md`, `sonria.md`, `dentalia.md`, `sanitas.md`, `vitaldent.md`). No forman parte de la tabla comparativa de software (Sección A), pero confirman que en Argentina el canal habitual de solicitud de turnos en clínicas reales sigue siendo WhatsApp o teléfono manual.
