# DESARROLLO DE SOFTWARE – TRABAJO GLOBAL

**Trabajo de Integración: del Discovery al primer Change, con Spec-Driven Development y Active Stack**

---

## OBJETIVO GENERAL

Recorrer, de principio a fin y sobre un producto real, el flujo de trabajo que vieron en los videos de SDD con OpenSpec y de Active Stack: partir de una idea sin ningún código, investigarla con método (Discovery), fundar el proyecto con un agente de IA (base de conocimiento, roadmap y reglas del agente) y llevar una funcionalidad desde su especificación hasta su archivado mediante un ciclo OPSX completo.

El producto sobre el que van a trabajar es un **sistema de gestión de turnos y agenda de pacientes para consultorios odontológicos, pensado para el mercado argentino**. No van a programar “lo que se les ocurra”: van a decidir qué construir a partir de evidencia verificable, y a demostrar que cada decisión quedó registrada y es trazable.

---

## PUNTO DE PARTIDA

Este trabajo da por sabido lo explicado en los videos:
- **Video SDD + OpenSpec:** qué es una especificación, escenarios verificables y el ciclo `explore → propose → apply → archive`.
- **Active Stack (partes 1, 2 y 3):** harnesses, modos de instalación (Lite/Full/Custom), `/active-orchestrator`, las siete etapas de la fundación, el protocolo de inspección (Continuar / Ajustar / Parar acá), la bitácora compartida, el ciclo OPSX y Engram.

---

## MARCO TEÓRICO

| Concepto | Aplicación en este trabajo |
|---|---|
| **Discovery** | Convierte “tengo una idea” en “sé qué voy a construir y por qué”, antes de decidir arquitectura. Se resuelve con investigación de mercado exhaustiva y verificable. |
| **Harness** | Andamiaje que rodea al modelo (skills, configuración, herramientas externas). Active Stack instala ese andamiaje sobre su proyecto. |
| **Fundación del proyecto** | Las siete etapas de `/active-orchestrator`: OpenSpec Init, Discovery, KB Creator, Roadmap Generator, Find Skill, Skill Registry y Agent Instruction. |
| **Knowledge-base y CHANGES.md** | Mapa de contexto del proyecto y plan de implementación. De ellos sale la lista de changes de la cual eligen el que van a implementar. |
| **Protocolo de inspección** | Al final de cada etapa deciden entre Continuar, Ajustar o Parar acá. Se evalúa que hayan leído lo generado y corregido lo que estaba mal. |
| **Ciclo OPSX** | `Explore`, `propose`, `apply`, `verify` y `archive` sobre un cambio concreto. La especificación aprobada en propose es el contrato de la implementación. |
| **Engram** | Memoria persistente del agente entre sesiones. Van a mostrar qué quedó guardado y cómo se recupera. |

---

## MODALIDAD Y ORGANIZACIÓN

- Individual o en grupo. Todos los integrantes deben participar en alguna parte del flujo.
- En grupo, **EL LÍDER debe ser responsable de la etapa de la fundación y del ciclo OPSX** (conducir la etapa con el agente, revisar lo generado y decidir Continuar/Ajustar).
- Luego crear un repositorio con las ramas correspondientes para cada miembro del grupo y manejar la rama `main` para controlar los Pull Requests de cada change que suban los miembros.
- Dirigir la secuencialidad de los changes y el paralelismo que se muestren en el Roadmap.
- La participación de cada integrante tiene que ser demostrable: aparece explicada en la reflexión escrita, se ve en el historial de commits del repositorio (cada persona con sus propios commits) y cada uno habla en el video de defensa.
- Un único repositorio y una única entrega por grupo.

---

## CASO PRÁCTICO (8 ETAPAS)

### Etapa 0. Preparación del entorno
- Herramientas previas: Go, Git y Node instalados, y un agente de código (OpenCode o Claude Code) funcionando.
- Active Stack en modo Full: clonar repositorio oficial, compilar y correr el instalador en modo Full.
- Repositorio Git inicializado sin código previo.

### Etapa 1. Iniciar la fundación (`/active-orchestrator` y `openspec init`)
- Ejecutar `/active-orchestrator` dentro del repositorio vacío. Corre primero `openspec init`.
- Responder Sí a la pregunta de bifurcación (“¿hacemos estudio de mercado?”): el Discovery es obligatorio.
- Evidencia: archivo de estado `.active-orchestrator-state.json`.

### Etapa 2. Discovery: investigación de mercado
- Skill `discovery-research` respondiendo el checklist de once puntos: problema, usuarios, casos de uso, competidores, funcionalidades necesarias, opcionales, reglas de negocio, integraciones, restricciones, riesgos y preguntas abiertas.
- Búsqueda exhaustiva de sistemas odontológicos de turnos y agenda en Argentina, Latinoamérica e internacionales relevantes.
- Formato del informe de Discovery (`docs/discovery/informe-discovery.md` y `informe-discovery.pdf`):
  - **A. Tabla comparativa** de al menos 15 sistemas ordenados por relevancia para Argentina (distinguiendo entre comprobado, afirmación comercial y no evidenciado).
  - **B. Matriz de puntuación (0 a 5):** Turnos y automatización (25 %), Clínica odontológica (20 %), Integraciones locales + WhatsApp (15 %), Admin/cobros/facturación (15 %), Experiencia paciente (10 %), Seguridad/trazabilidad (10 %), Precio/adopción (5 %).
  - **C. Análisis competitivo:** estándar de mercado, diferenciadores reales, vacíos locales y oportunidades de innovación.
  - **D. Recomendación final:** 5 competidores para demo, 3 de referencia UX, y el MVP sugerido (imprescindible, diferenciador y etapas posteriores).
  - **Verificación de fuentes:** Comprobar personalmente al menos 5 fuentes citadas en el navegador, anotar si lo que dice el informe es real y corregir con Ajustar lo no respaldado.

### Etapa 3. Base de conocimiento (`kb-creator`)
- Generar la carpeta `knowledge-base/` con los diez archivos canónicos: visión, descripción, actores, modelo de datos, reglas de negocio, features, flujos, arquitectura, decisiones y preguntas abiertas.

### Etapa 4. Roadmap (`roadmap-generator`)
- Generar `CHANGES.md` a partir de la knowledge-base: dependencias entre tareas, asignación con contexto suficiente, camino crítico y riesgos, alineado al MVP.

### Etapa 5. Skills y reglas del agente (`find-skill`, `skill-registry`, `agent-instruction`)
- `find-skill`: justificar qué skills instalar según el stack.
- `skill-registry`: genera `.atl/skill-registry.md`.
- `agent-instruction`: genera `AGENTS.md` y `CLAUDE.md` con reglas del stack y reglas duras del dominio (ej. Ley 25.326: datos ficticios).

### Etapa 6. Ciclo OPSX sobre un change (`explore → propose → apply → verify → archive`)
- Elegir un change del MVP con reglas verificables. Ejemplo recomendado por la cátedra: **“Crear un turno evitando solapamientos por profesional y por sillón/box”** (turnos borde a borde permitidos).
- **Explore:** relevar el problema y estado del código antes de proponer.
- **Propose:** generar `proposal.md`, `design.md`, `specs/` y `tasks.md`. Los escenarios deben cubrir al menos 3 casos: uno feliz, uno de error de negocio y uno de borde.
- **Apply:** implementar contra la especificación aprobada sin inventar requisitos.
- **Verify:** tests automatizados en verde para cada escenario.
- **Archive:** cerrar y archivar el change.

### Etapa 7. Memoria del agente (Engram)
- Mostrar qué quedó guardado en Engram sobre este proyecto y realizar una consulta real (`mem_search`) recuperando contexto.

---

## ENTREGABLES FINALES

1. **Repositorio Git:**
   - Estructura limpia con `knowledge-base/`, `CHANGES.md`, `AGENTS.md`, `openspec/`, `docs/discovery/`.
   - `README.md` reproducible para clonar y correr `./gradlew test` en verde.
   - Historial de commits que demuestre la participación de cada integrante.
   - Cero secretos commiteados.

2. **Informe de Discovery en PDF (`docs/discovery/informe-discovery.pdf`):**
   - Portada, resumen ejecutivo, secciones A, B, C, D, verificación de fuentes (mínimo 5) y referencias completas.

3. **Video de defensa (8 a 12 minutos):**
   - Todos los integrantes aparecen con cámara encendida al presentarse y hablan de su parte.
   - Pantalla real trabajando (Discovery, KB, momento de Ajustar en Propose, tests en verde, Archive y Engram).

4. **Reflexión escrita (1 a 2 páginas, escrita sin IA):**
   - ¿Qué información del Discovery resultó incorrecta o inventada y cómo la detectaron?
   - ¿En qué etapa usaron Ajustar, qué estaba mal y qué habría pasado si continuaban?
   - ¿Qué diferencia notaron entre implementar con especificación aprobada vs prompt suelto?
   - ¿Qué parte de la fundación aportó más valor al ciclo OPSX y cuál menos?
   - ¿Qué hizo cada integrante en el equipo?

---

## CRITERIOS DE EVALUACIÓN (100 PUNTOS)

| Criterio | Qué se observa | Puntos |
|---|---|---|
| **Informe de Discovery** | Completitud A-D, mínimo 15 sistemas, URLs y fechas, uso de "No evidenciado", distinción comprobado vs comercial, criterio crítico. | 25 |
| **Verificación de fuentes** | Comprobación personal de al menos 5 fuentes y corrección de lo no respaldado. | 10 |
| **Fundación del proyecto** | Knowledge-base coherente, roadmap alineado al MVP, `AGENTS.md` con reglas revisadas, registro de skills. | 20 |
| **Change OPSX** | Elección justificada, spec con al menos 3 escenarios verificables, tests por escenario en verde, change archivado. | 25 |
| **Video de defensa** | Claridad, proceso real mostrado, participación de todos, evidencia de Engram. | 10 |
| **Reflexión escrita** | Honestidad, profundidad, evidencia de uso real del protocolo de inspección. | 5 |
| **Trazabilidad y participación** | Historial de commits distribuido, README reproducible, sin secretos. | 5 |
| **TOTAL** | | **100** |
