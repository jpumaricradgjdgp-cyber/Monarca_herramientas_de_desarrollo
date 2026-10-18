# Informe del Avance de Proyecto Final 2

**Proyecto:** Monarca. **Curso:** Herramientas de Desarrollo. **Grupo:** G7. **Fecha de preparación:** 18 de octubre de 2026.

Completar en la portada de Word los datos académicos que solicite el docente: sección, códigos, docente y formato institucional. Este informe organiza evidencias existentes y pendientes; las actividades pendientes están marcadas expresamente.

## 1. Objetivo

Desarrollar e integrar mejoras del e-commerce Monarca mediante trabajo colaborativo con Git y GitHub, asignando tareas y ramas por integrante, revisando cambios mediante Pull Requests y verificando la integración con pruebas automáticas para mantener una versión estable en main.

Objetivos específicos:

- Mejorar el acceso con teclado y la presentación móvil de las páginas.
- Comprobar la compatibilidad del esquema de datos con las entidades del backend.
- Incorporar la actualización administrativa de pedidos y pagos manuales con protección del inventario.
- Automatizar la ejecución de pruebas e identificar problemas de integración.
- Documentar el flujo de trabajo, las decisiones, las evidencias y las limitaciones.

## 2. Alcance y fases

Esta tabla reconstruye el proceso observado en el repositorio y organiza el cierre actual. No pretende acreditar una planificación anterior a las actividades.

| Fase | Actividad | Entregable | Estado |
| --- | --- | --- | --- |
| Inspección | Revisar código, restricciones de datos y requisitos del avance | Diagnóstico del alcance y distribución de tareas | Realizada durante el trabajo; consolidada en este informe |
| Desarrollo | Incorporar cambios mediante cinco ramas feature/* | Commits y PR #4 a #8 | Integrados en develop |
| Revisión | Revisar los cambios desde otras cuentas del equipo | Aprobaciones formales de PR #4 a #8 | Registradas en GitHub |
| Integración | Ejecutar pruebas y resolver el fallo de sincronización de un test | Workflow, PR #9 y resultados de CI | Integrado; el PR #9 no registra aprobación formal |
| Versión integrada | Fusionar develop en main | PR #10 y commit 0bde0f8 | Fusionado; el PR #10 no registra aprobación formal |
| Preparación de entrega | Organizar informe, evidencias y notas de lanzamiento | Rama release/avance-2 y documentos actuales | En preparación y revisión |
| Validación real y sustentación | Prueba manual con Supabase, capturas y explicación del equipo | Registro de pruebas y exposición | Pendiente de evidencia |

## 3. Participantes, actividades y ramas

| Integrante | Cuenta | Rama | Aporte de este avance | PR y revisor |
| --- | --- | --- | --- | --- |
| Jordy Jacobo Espinoza Molina | Jordy1111 | feature/jordy-contacto-nosotros | Botón accesible de cierre, Escape, control y retorno del foco en la matriz de Nosotros | [#4](https://github.com/jpumaricradgjdgp-cyber/Monarca_herramientas_de_desarrollo/pull/4), aprobado por jpumaricradgjdgp-cyber |
| Ian Arturo Callirgos Huamán | IanCallirgos | feature/ian-base-datos | Consultas de diagnóstico, guía de ejecución y evidencias de columnas, restricciones e índices | [#5](https://github.com/jpumaricradgjdgp-cyber/Monarca_herramientas_de_desarrollo/pull/5), aprobado por jpumaricradgjdgp-cyber |
| Luz Daniela Grandez Cayo | luzgrandez | feature/luz-catalogo | Filtros accesibles y encabezado móvil compacto conservando los elementos visuales del proyecto | [#6](https://github.com/jpumaricradgjdgp-cyber/Monarca_herramientas_de_desarrollo/pull/6), aprobado por Jordy1111 |
| Diego Alexander Chavez Villegas | diegochaaaavez45 | feature/diego-pruebas | Instalación reproducible de pruebas, base temporal para tests y workflow de GitHub Actions | [#7](https://github.com/jpumaricradgjdgp-cyber/Monarca_herramientas_de_desarrollo/pull/7), aprobado por jpumaricradgjdgp-cyber |
| Josue Daniel Gonzales Pumaricra | jpumaricradgjdgp-cyber | feature/josue-pedidos | Gestión de estados, confirmación de pago con referencia, cancelación transaccional y pruebas del flujo | [#8](https://github.com/jpumaricradgjdgp-cyber/Monarca_herramientas_de_desarrollo/pull/8), aprobado por luzgrandez |

Las cuentas y revisiones corresponden a acciones verificables en GitHub. Cada integrante debe revisar esta descripción y explicar su trabajo y la asistencia técnica utilizada. El aporte de Ian es de diagnóstico y documentación técnica: no se presenta como una nueva pantalla o migración.

## 4. Estrategia de ramas y fusión

```text
feature/jordy-contacto-nosotros -- PR #4 --+
feature/ian-base-datos --------- PR #5 ---+
feature/luz-catalogo ----------- PR #6 ---+--> develop --> PR #10 --> main
feature/diego-pruebas ---------- PR #7 ---+
feature/josue-pedidos ---------- PR #8 ---+
fix/prueba-actualizacion-pedidos - PR #9 -+

develop --> release/avance-2 --> revisión de entrega --> main
                                  pendiente de integración y retorno a develop
```

- **main:** versión integrada que se presenta. No equivale automáticamente a un backend desplegado.
- **develop:** rama de integración de las tareas del equipo.
- **feature/*:** cambios concretos por integrante, revisados antes de fusionar en develop.
- **fix/*:** corrección de un defecto del desarrollo o sus pruebas; en #9 se corrigió una espera del test del administrador.
- **release/avance-2:** rama actual, creada desde develop para organizar la entrega. No altera el flujo histórico del PR #10. Su integración y etiquetado se detallan en [LANZAMIENTO.md](LANZAMIENTO.md).
- **hotfix/*:** rama que se usaría para una reparación urgente de una versión de producción, partiendo de main y devolviendo la reparación a develop. No se ha ejecutado un hotfix de producción.

El flujo inicial fue una adaptación con main, develop, feature y fix. La preparación de release se añade ahora; no se afirma que se haya aplicado Git Flow completo desde el inicio. Se conservaron los commits y las fusiones de los aportes.

Comandos utilizados o que el equipo debe poder explicar:

```text
git clone / git fetch / git pull --ff-only
git switch / git switch -c / git branch
git status / git diff / git add / git commit
git push / git log --graph --oneline --all
```

Una solicitud de revisión no es una aprobación. Se comprobaron aprobaciones formales en #4 a #8; #9 y #10 se fusionaron sin una aprobación formal registrada. En los siguientes PR se propone revisar los archivos y verificar CI antes de fusionar.

## 5. Funciones y pruebas

**Interfaz:** los filtros usan botones y estados accesibles; las opciones cerradas no interfieren con el teclado. En pantallas táctiles se ven las acciones de los productos. El encabezado móvil mantiene logo, perfil y carrito en una barra compacta. El diálogo de Nosotros permite Escape y devuelve el foco al control de apertura.

**Base de datos:** existen consultas de solo lectura en backend/db. Los CSV muestran 43 restricciones validadas y 35 índices. El usuario informó que todas las filas del diagnóstico de columnas dieron OK; el CSV de esa primera consulta está pendiente de adjuntar.

**Pedidos:** solo ADMIN puede actualizar estados. Confirmar el pago exige una referencia y guarda PAGADO tanto en pedido como en pago, conforme al CHECK observado. La cancelación de un pedido pendiente devuelve el stock una sola vez. Los cambios se realizan en transacciones y usan bloqueos para serializar operaciones concurrentes. El pago se verifica manualmente.

**Pruebas:** se documentaron 44 tests de backend y seis suites de navegador. La prueba de contexto utiliza H2 y las de navegador simulan la API. [Actions de main](https://github.com/jpumaricradgjdgp-cyber/Monarca_herramientas_de_desarrollo/actions/runs/39141758866) aprobó Backend Java 21 y Frontend Chromium sobre 0bde0f8. Los detalles y límites están en [PRUEBAS.md](../PRUEBAS.md) y [PEDIDOS.md](../PEDIDOS.md).

Un fallo posterior a la integración de #8 mostró que el test leía el selector antes de terminar la recarga de la tabla. El PR #9 añadió esperas por el nuevo estado y una respuesta simulada demorada. Se reprodujo el fallo antes de corregirlo y CI aprobó la versión corregida. Este incidente es un fallo de prueba, no un conflicto de merge.

## 6. Conflictos de fusión

No se encontró evidencia documentada de un conflicto real en las integraciones revisadas. Se prepara un [ejercicio controlado](CONFLICTOS.md) basado en el manual de clase, en un repositorio temporal separado de Monarca. Su ejecución y sustentación por el equipo siguen pendientes hasta adjuntar los resultados reales.

## 7. Herramientas colaborativas

| Herramienta | Uso declarado o comprobado | Evidencia |
| --- | --- | --- |
| Git y GitHub | Control de versiones, ramas, revisión e integración | PR #4 a #10 e historial |
| Trello | Consolidación actual de aportes y seguimiento del cierre | [Tablero del equipo](https://trello.com/b/inNPYEny/monarca-avance-2-trabajo-del-equipo); privado, requiere acceso para otros integrantes y docente |
| GitHub Actions | Verificación automática del backend y frontend | Workflow y ejecución aprobada de main |
| WhatsApp | Coordinación del grupo, indicada por el usuario | Pendiente de una captura autorizada de una decisión concreta, con contexto y fecha |
| Word en línea | Trabajo con documento compartido, indicado por el usuario | Pendiente de enlace de lectura e historial/comentarios; confirmar si se almacena en OneDrive |
| Videollamadas | El usuario indicó "meet en zoom" | Confirmar si se utilizó Zoom, Google Meet o ambos y adjuntar evidencia real de la reunión |

No se registran fechas ni reuniones inventadas. Adjuntar solo capturas pertinentes y proteger datos personales. El [tablero de Trello](https://trello.com/b/inNPYEny/monarca-avance-2-trabajo-del-equipo), creado el 18 de octubre de 2026, consolida los PR y organiza pendientes actuales; no se presenta como planificación previa al desarrollo. Tiene 14 tarjetas en cuatro listas. Los responsables se indican en los títulos y descripciones; la incorporación de las demás cuentas del equipo a Trello está pendiente.

## 8. Correspondencia con la rúbrica

| Criterio | Máximo | Cómo se presenta |
| --- | ---: | --- |
| Objetivo | 2 | Sección 1 |
| Planificación y fases | 2 | Sección 2, más tareas actuales |
| Actividades, roles y ramas | 4 | Sección 3 y PR por integrante |
| Estrategia Git | 7 | Secciones 4 a 6 y notas de lanzamiento |
| Exposición grupal y enlace | 5 | Guion, evidencia real de cada integrante y enlace al repositorio |
| Total | 20 | La valoración final corresponde al docente |

Fuentes académicas: Avance de Proyecto Final 2.pdf (páginas 1-2); S08_S1 Material.pdf (PR y flujos de merge/release); Manual_Practico_Merge_Conflict.pdf; material de Semana 9 sobre herramientas colaborativas. No se incluyen copias de las diapositivas del curso en el repositorio.

## 9. Conclusiones y pendientes

El repositorio permite rastrear los aportes por rama y PR y comprobar la versión integrada mediante CI. El trabajo pendiente de entrega consiste en revisión de estos documentos, evidencias de coordinación y conflictos, y sustentación del equipo.

Para validar el sistema completo aún falta una prueba manual de pedidos con Supabase, comprobar el despliegue público del backend y desarrollar el probador virtual con ML. No se presentan como implementados. La rúbrica específica de este avance se centra en colaboración y control de versiones.
