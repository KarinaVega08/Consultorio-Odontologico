# Gestión de turnos para consultorios odontológicos — Base de Conocimiento

Base de conocimiento generada a partir de la etapa de Discovery y la definición de arquitectura del proyecto.

## Índice de Archivos

| Archivo | Contenido |
|---------|-----------|
| [01_vision_y_objetivos.md](01_vision_y_objetivos.md) | Propósito, objetivos por actor, alcance v1.0, fuera de alcance, métricas de éxito |
| [02_descripcion_general.md](02_descripcion_general.md) | Stack tecnológico, arquitectura general, integraciones externas, API REST |
| [03_actores_y_roles.md](03_actores_y_roles.md) | Actores del sistema, matriz RBAC de permisos, rutas públicas |
| [04_modelo_de_datos.md](04_modelo_de_datos.md) | Dominios, ERD, entidades, atributos, constraints, seed data |
| [05_reglas_de_negocio.md](05_reglas_de_negocio.md) | Reglas codificadas por dominio (RN-XX) con códigos únicos |
| [06_funcionalidades.md](06_funcionalidades.md) | Épicas y historias de usuario (US-NNN) con criterios de aceptación |
| [07_flujos_principales.md](07_flujos_principales.md) | Flujos extremo a extremo (registro, reserva, creación por personal, cancelación, blanqueo) |
| [08_arquitectura_propuesta.md](08_arquitectura_propuesta.md) | Patrones, estructura de directorios, seguridad, variables de entorno |
| [09_decisiones_y_supuestos.md](09_decisiones_y_supuestos.md) | Decisiones documentadas (DD-XX) y supuestos inferidos (SU-XX) |
| [10_preguntas_abiertas.md](10_preguntas_abiertas.md) | Inconsistencias detectadas y preguntas abiertas priorizadas |

## Quick Start para Desarrolladores

1. Entender el dominio → [01](01_vision_y_objetivos.md), [03](03_actores_y_roles.md)
2. Entender los datos → [04](04_modelo_de_datos.md)
3. Entender las reglas → [05](05_reglas_de_negocio.md)
4. Entender la arquitectura → [02](02_descripcion_general.md), [08](08_arquitectura_propuesta.md)
5. Implementar → [07](07_flujos_principales.md), [06](06_funcionalidades.md)
6. Antes de codificar → [10](10_preguntas_abiertas.md)

## Resumen Ejecutivo

Sistema de gestión de turnos para consultorios odontológicos en Argentina. Resuelve inasistencias de pacientes, agendas manuales por WhatsApp/planillas y reservas superpuestas entre profesionales y sillones/boxes. Stack: Java 21 + Spring Boot 3 + Gradle + JPA/Hibernate + API REST + TypeScript Vanilla, PostgreSQL local y H2 para tests. Presupuesto $0, plazo fijo 25 nov 2026, cumplimiento Ley 25.326 (datos ficticios en repo). Roles: Administrador, Odontólogo, Recepcionista, Paciente. Motor de disponibilidad con anti-solapamiento estricto y recordatorios automáticos por email.
