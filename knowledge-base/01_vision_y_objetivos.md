# Visión y Objetivos

## Propósito del sistema

Sistema de gestión de turnos para consultorios odontológicos en Argentina. Permite a consultorios unipersonales y pequeños reemplazar agendas manuales (WhatsApp, planillas de Excel) por una plataforma centralizada con registro de pacientes, agenda de turnos con anti-solapamiento, catálogo de prestaciones con duración variable, roles diferenciados (Administrador, Odontólogo, Recepcionista, Paciente) y recordatorios automáticos por email. Objetivo: reducir inasistencias, eliminar reservas superpuestas y liberar tiempo operativo del equipo del consultorio.

## Objetivos por actor

| Actor | Objetivo principal | Objetivos secundarios |
|-------|-------------------|----------------------|
| Paciente | Reservar, confirmar y cancelar turnos online sin llamar al consultorio | Registrarse con DNI, recuperar contraseña por Gmail, consultar historial de turnos |
| Recepcionista / Secretaria | Crear, reprogramar y cancelar turnos para pacientes; controlar asistencia | Blanquear contraseña de pacientes presencialmente; gestionar la agenda del día |
| Odontólogo | Gestionar su propia agenda diaria/semanal y registrar la atención | Consultar turnos asignados; asumir rol de administrador en consultorios unipersonales |
| Administrador | Configurar el consultorio (profesionales, sillones/boxes, horarios, bloqueos, prestaciones) y supervisar la agenda global | Crear/editar profesionales, sillones y prestaciones; generar reportes de operación |

## Alcance v1.0

- Registro de pacientes con nombre real, DNI (identificador único, 7-8 dígitos) y contraseña
- Vinculación única de correo Gmail de recuperación (una sola vez después del primer login)
- Login con DNI + contraseña; recuperación de contraseña por Gmail o blanqueo presencial por Recepción
- Roles diferenciados: Administrador, Odontólogo, Recepcionista/Secretaria, Paciente
- Configuración administrativa de profesionales, sillones/boxes, horarios de atención, bloqueos y catálogo de prestaciones con duración variable en minutos
- Gestión de turnos: creación, reprogramación y cancelación (por personal del consultorio y por autogestión online del paciente)
- Motor de disponibilidad con validación estricta anti-solapamiento por profesional y por sillón/box (turnos consecutivos borde a borde están permitidos)
- Recordatorios automáticos por correo electrónico al Gmail vinculado del paciente, con opción de confirmar asistencia
- Historial de turnos conservado (cancelaciones pasan a estado CANCELADO, nunca borrado físico)

## Fuera de alcance

- Recordatorios y chatbot por WhatsApp Business API (post-MVP)
- Cobros online, señas con Mercado Pago, facturación electrónica AFIP/ARCA (post-MVP)
- Odontograma interactivo, periodontograma, radiografías e imágenes clínicas (post-MVP)
- Lista de espera automática y multi-sucursal (post-MVP)
- Integración con obras sociales y prepagas para liquidación automática (post-MVP)
- Almacenamiento de datos reales de pacientes en el repositorio, seeds o tests (solo datos ficticios, Ley 25.326)

## Métricas de éxito

- Reducción de inasistencias a turnos (medible mediante recordatorios por email y tasa de confirmación)
- Cero reservas superpuestas por profesional ni por sillón/box en el período de prueba
- Tiempo de configuración del consultorio (alta de profesionales, sillones y prestaciones) menor a 30 minutos
- Tasa de adopción de autogestión online por pacientes (turnos reservados por el paciente vs. por recepción)
