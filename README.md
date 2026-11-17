# Monarca

[![Pruebas Monarca](https://github.com/jpumaricradgjdgp-cyber/Monarca_herramientas_de_desarrollo/actions/workflows/pruebas.yml/badge.svg?branch=main)](https://github.com/jpumaricradgjdgp-cyber/Monarca_herramientas_de_desarrollo/actions/workflows/pruebas.yml)

Tienda en línea de moda (tops, bottoms, calzado y accesorios) desarrollada en el
curso **Herramientas de Desarrollo** (UTP). El proyecto aplica Git y GitHub para
trabajar en equipo: ramas por funcionalidad, pull requests revisados por otro
integrante y pruebas automáticas en cada PR.

## Entrega académica

- [Informe del Avance 2 y matriz del equipo](docs/INFORME_AVANCE_2.md)
- [Índice de evidencias](docs/EVIDENCIAS.md)
- [Ejercicio controlado de conflictos](docs/CONFLICTOS.md)
- [Preparación del lanzamiento](docs/LANZAMIENTO.md)
- [Guion de exposición](docs/GUION_EXPOSICION.md)

## Funcionalidades

- Catálogo por categorías con filtros, detalle de producto y variantes (talla y color).
- Carrito de compras y checkout con métodos de pago manuales (p. ej. Yape, Plin, transferencia).
- Registro, inicio de sesión con JWT y recuperación de contraseña por correo.
- Panel de administración para gestionar productos y el estado de los pedidos.
- Páginas informativas: Nosotros y Contacto.

## Tecnologías

| Capa | Tecnología |
| --- | --- |
| Frontend | HTML, CSS y JavaScript (sin framework) |
| Backend | Java 21, Spring Boot 3.4, Spring Security + JWT, Spring Data JPA |
| Base de datos | PostgreSQL en Supabase (esquema `monarca`) |
| Pruebas | JUnit + MockMvc + H2 (backend), Playwright (frontend) |
| CI | GitHub Actions (`.github/workflows/pruebas.yml`) |

## Estructura del repositorio

```
Monarca_herramientas_de_desarrollo/
├── backend/              API REST con Spring Boot
│   ├── src/main/java/com/Monarca/Backend/
│   │   ├── controller/   Endpoints REST
│   │   ├── service/      Reglas de negocio
│   │   ├── repository/   Acceso a datos (JPA)
│   │   ├── model/        Entidades
│   │   ├── dto/          Objetos de entrada/salida de la API
│   │   └── security/     JWT, filtros y configuración de seguridad
│   └── db/               Modelo de datos (MODELO_DATOS.md) y diagnóstico del esquema
├── fronted/              Sitio web estático
│   ├── index.html
│   ├── Paginas/          Tops, Bottoms, Calzado, Accesorios, producto, checkout, login, admin...
│   ├── css/  js/  img/
│   └── tests/            Pruebas de navegador con Playwright
├── docs/                 Informes, evidencias y guion de exposición
├── scripts/              Ejercicio local reproducible de conflictos
├── .github/              Workflow de pruebas y plantilla de Pull Request
├── PEDIDOS.md            Gestión de pedidos y pagos (administrador)
└── PRUEBAS.md            Cómo ejecutar las pruebas
```

## Puesta en marcha

### Requisitos

- Java 21
- Node.js 22 o superior (solo para las pruebas del frontend)
- Una base de datos PostgreSQL con el esquema `monarca` (el equipo usa Supabase)

### 1. Variables de entorno del backend

El backend **no guarda claves en el código**: las lee de variables de entorno.

| Variable | Obligatoria | Descripción |
| --- | --- | --- |
| `DB_URL` | Sí | URL JDBC de PostgreSQL, p. ej. `jdbc:postgresql://<host>:5432/postgres` |
| `DB_USERNAME` | Sí | Usuario de la base de datos |
| `DB_PASSWORD` | Sí | Contraseña de la base de datos |
| `JWT_SECRET` | Sí | Clave para firmar los tokens de sesión (mínimo 32 bytes) |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | No | SMTP para recuperar contraseña |
| `PASSWORD_RESET_URL` | No | Página del frontend a la que apunta el correo de recuperación |

En PowerShell (solo para la sesión actual):

```powershell
$env:DB_URL = "jdbc:postgresql://<host>:5432/postgres"
$env:DB_USERNAME = "<usuario>"
$env:DB_PASSWORD = "<contraseña>"
$env:JWT_SECRET = "<clave-larga-y-secreta>"
```

> Nunca subas estos valores al repositorio.

### 2. Ejecutar el backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8080/api`. El backend usa
`ddl-auto=validate`, así que el esquema debe existir previamente en la base de datos.

### 3. Abrir el frontend

Sirve la carpeta `fronted/` con cualquier servidor estático, por ejemplo la
extensión **Live Server** de VS Code, y abre `index.html`. El frontend consume la
API en `http://localhost:8080/api` (configurado en `fronted/js/tienda.js`).

## API principal

| Recurso | Endpoints |
| --- | --- |
| Autenticación | `POST /api/auth/registro`, `/login`, `/olvide-password`, `/restablecer-password` |
| Productos | `GET /api/productos`, `GET /api/productos/{id}`; `POST`, `PUT /{id}` y `DELETE /{id}` solo ADMIN |
| Categorías | `GET /api/categorias`, `GET /api/categorias/{id}` |
| Métodos de pago | `GET /api/metodos-pago` |
| Pedidos | `POST /api/pedidos/procesar`, `GET /api/pedidos`; `PUT /api/pedidos/{id}/estado` solo ADMIN |

El detalle de estados y pagos de pedidos está en [PEDIDOS.md](PEDIDOS.md).

## Pruebas

```powershell
cd backend
.\mvnw.cmd verify

cd ..\fronted
npm ci
npx playwright install chromium
npm test
```

Las pruebas no necesitan claves reales: el backend usa H2 en memoria y el frontend
simula la API. GitHub Actions las ejecuta en cada PR hacia `develop` o `main`.
Más detalles en [PRUEBAS.md](PRUEBAS.md).

## Flujo de trabajo con Git (Git Flow)

| Rama | Propósito |
| --- | --- |
| `main` | Versión estable entregada. Solo recibe merges desde `release/*` o `hotfix/*`. |
| `develop` | Integración del trabajo del equipo. Base de todas las ramas nuevas. |
| `feature/<integrante>-<tarea>` | Una funcionalidad o mejora. Sale de `develop` y vuelve a `develop` por PR. |
| `fix/<tarea>` | Corrección detectada durante el desarrollo, hacia `develop`. |
| `release/<avance>` | Preparación de una entrega. Se fusiona en `main` (y de vuelta en `develop`). |
| `hotfix/<tarea>` | Corrección urgente sobre `main`. |

Pasos para contribuir:

1. `git switch develop` y `git pull`
2. `git switch -c feature/<tu-nombre>-<tarea>`
3. Configura tu identidad en el repo: `git config user.name "..."` y `git config user.email "<correo verificado en tu GitHub>"`
4. Haz commits pequeños con mensajes claros en español (p. ej. "Agrega filtro por talla en el catálogo").
5. `git push -u origin feature/<tu-nombre>-<tarea>` y abre un PR hacia `develop`.
6. Otro integrante revisa y aprueba; se fusiona solo si las pruebas pasan.
7. Tras fusionar, se elimina la rama (poda) para mantener el repositorio limpio.

## Equipo

| Integrante | Rol principal |
| --- | --- |
| Josué Daniel Gonzales Pumaricra | Backend: productos, pedidos y pagos |
| Luz Daniela Grandez Cayo | Frontend: catálogo y diseño responsive |
| Ian Arturo Callirgos Huamán | Base de datos (Supabase / PostgreSQL) |
| Jordy Jacobo Espinoza Molina | Páginas Contacto y Nosotros, documentación |
| Diego Alexander Chávez Villegas | Pruebas automáticas e integración continua |
