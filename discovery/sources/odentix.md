# Odentix

- **Proveedor**: Odentix
- **País de origen**: Argentina (`es_AR`, `https://www.odentix.com.ar/`)
- **URL oficial**: https://www.odentix.com.ar/
- **Fecha de consulta**: 2026-11-23 (verificación visual en navegador con JavaScript)
- **URLs consultadas**:
  - https://www.odentix.com.ar/ (SPA React/Vite; secciones `#planes`, funcionalidades y FAQ renderizadas con scroll)

## Segmento y modalidad
- Segmento: odontólogos independientes (Plan Starter), clínicas de hasta 10 usuarios (Plan Clinic) y multiclínicas/sucursales (Plan Max) — **Comprobado** (`https://www.odentix.com.ar/#planes`).
- Modalidad: aplicación web en la nube (SPA) — **Comprobado**.

## Gestión de agenda y turnos
- Agenda inteligente en tiempo real (vista día/semana/mes, reprogramación, turno rápido; agenda por profesionales/especialidades desde Plan Clinic) — **Afirmación comercial** en landing (`https://www.odentix.com.ar/`).
- Múltiples sillones/boxes o prevención estricta de solapamientos: **No evidenciado**.
- Reserva online 24/7 por el paciente: **No evidenciado** (Asistente Inteligente 24hs figura con botón "Próximamente").

## Automatización, clínica y administración
- Recordatorios automáticos por e-mail (ilimitados) y WhatsApp (hasta 150/mes en Starter; 250 por usuario/mes en Clinic) — **Comprobado** en tabla de planes (`https://www.odentix.com.ar/#planes`).
- Historia clínica, odontograma interactivo, presupuestos, control de pagos, estadísticas, dictado por voz y llamador de sala de espera (desde Clinic) — **Afirmación comercial** en landing.
- Facturación ARCA / obras sociales / seguridad Ley 25.326: **No evidenciado**.

## Modelo comercial (público en ARS en `#planes`)
- **Plan Starter**: `$42.900 ARS/mes` (`$38.610 ARS/mes` anual -10%), 1 usuario, pacientes ilimitados, 5 GB, e-mail ilimitado + 150 WhatsApp/mes — **Comprobado**.
- **Plan Clinic** *(botón "Próximamente")*: `$48.900 ARS/usuario/mes` (`$44.010` anual -10%), hasta 10 usuarios, 10 GB/usuario, 250 WhatsApp/usuario — **Comprobado**.
- **Plan Max**: cotización a medida (multiclínicas/sucursales) — **Comprobado**.

## Notas del investigador (Hallazgo de verificación 2026-11-23)
- En el relevamiento inicial (2026-11-19/20) el fetch estático sin JS no leía el cuerpo de la SPA y dejó todo como `No evidenciado` (`0.00`). Al abrirla en navegador con JS (Revisor: Matias), la página cargó completa con funcionalidades y precios públicos en ARS (corregido con **Ajustar**).
