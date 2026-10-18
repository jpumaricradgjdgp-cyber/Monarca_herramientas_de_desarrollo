# Monarca

E-commerce universitario desarrollado con Spring Boot, PostgreSQL y un frontend HTML/CSS/JavaScript. Este repositorio documenta el Avance 2 de Herramientas de Desarrollo: ramas por integrante, Pull Requests, revisión, integración y pruebas automáticas.

[![Pruebas Monarca](https://github.com/jpumaricradgjdgp-cyber/Monarca_herramientas_de_desarrollo/actions/workflows/pruebas.yml/badge.svg?branch=main)](https://github.com/jpumaricradgjdgp-cyber/Monarca_herramientas_de_desarrollo/actions/workflows/pruebas.yml)

## Entrega académica

- [Informe del Avance 2 y matriz del equipo](docs/INFORME_AVANCE_2.md).
- [Índice de evidencias y tareas pendientes](docs/EVIDENCIAS.md).
- [Ejercicio controlado de conflictos](docs/CONFLICTOS.md).
- [Preparación del lanzamiento](docs/LANZAMIENTO.md).
- [Guion de exposición](docs/GUION_EXPOSICION.md).
- [Pruebas y límites de validación](PRUEBAS.md).
- [Funcionamiento de pedidos y pagos manuales](PEDIDOS.md).
- [Diagnóstico del esquema de base de datos](backend/db/README.md).

## Funciones incorporadas en este avance

- Diálogo de la matriz de responsabilidades accesible con teclado.
- Filtros accesibles, acciones táctiles y encabezado móvil compacto.
- Diagnóstico de columnas, restricciones e índices con evidencias de Supabase.
- Gestión administrativa de estados de pedidos y confirmación manual de pagos con referencia.
- Cancelación de pedidos pendientes con devolución transaccional del stock.
- Pruebas de backend y navegador ejecutadas mediante GitHub Actions.

El pago requiere comprobación manual del administrador. El probador virtual con ML sigue pendiente.

## Estructura

```text
backend/     API Spring Boot, entidades, servicios y pruebas Java
backend/db/  Consultas de diagnóstico y CSV de metadatos
fronted/     Páginas, estilos, JavaScript y pruebas de navegador
docs/        Informe, evidencias, conflictos, lanzamiento y exposición
scripts/     Ejercicio local reproducible de conflicto
.github/     Workflow de pruebas y plantilla de Pull Request
```

## Ejecución local

Requisitos: Java 21, Node.js 22 o superior para las pruebas del frontend, y una base PostgreSQL con el esquema `monarca` compatible. El backend usa `ddl-auto=validate`; no crea automáticamente la base real.

1. Configura en tu terminal `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET`, según `backend/src/main/resources/application.properties`. JWT_SECRET requiere al menos 32 bytes de texto para la firma actual.
2. Desde `backend`, ejecuta en PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

3. Sirve `fronted` con un servidor HTTP local, por ejemplo Live Server de VS Code, y abre `index.html`.
4. La API del frontend apunta actualmente a `http://localhost:8080/api`, en `fronted/js/tienda.js`. El backend debe estar activo y las páginas deben abrirse por HTTP.

La recuperación por correo usa las variables SMTP indicadas en application.properties: MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD, MAIL_FROM y PASSWORD_RESET_URL. Mantén sus valores fuera del repositorio.

Para ejecutar las pruebas sin Supabase ni Gmail, sigue [PRUEBAS.md](PRUEBAS.md).

## Estado de la versión

El commit `0bde0f84a414753524185ea80babbd0685cdd3c7` de main tiene [backend y frontend aprobados en Actions](https://github.com/jpumaricradgjdgp-cyber/Monarca_herramientas_de_desarrollo/actions/runs/39141758866). La rama `release/avance-2` prepara la documentación posterior a esa integración; su PR y revisión forman parte del cierre de la entrega.

Queda pendiente evidencia manual del flujo de pedidos contra Supabase, el despliegue público del backend, la exposición y el probador virtual. Consulta [el tablero de tareas actuales](https://trello.com/b/inNPYEny/monarca-avance-2-trabajo-del-equipo).
