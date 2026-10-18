# Pruebas automáticas de Monarca

## Ejecutar localmente

Requisitos: Java 21 y Node.js 22 o superior. Desde la raíz del repositorio,
en PowerShell:

```powershell
cd backend
.\mvnw.cmd --batch-mode --no-transfer-progress verify
cd ..\fronted
npm ci
npx playwright install chromium
npm test
```

Las pruebas no necesitan claves de Supabase ni Gmail. La prueba de arranque usa
H2 en memoria y una clave JWT exclusiva de pruebas. La configuración de producción
continúa usando PostgreSQL y ddl-auto=validate. Los demás tests de Java usan mocks.
H2 no sustituye la validación de las restricciones reales de PostgreSQL.

Los tests de navegador levantan un servidor local temporal, simulan la API y
bloquean recursos externos HTTPS. Comprueban registro, recuperación, filtros,
variantes, carrito, checkout, encabezado móvil y el diálogo de Nosotros.
No certifican envío real de correos, compras reales ni disponibilidad de los CDN.
Opcionalmente se puede usar Chrome instalado estableciendo BROWSER_EXECUTABLE.

## GitHub Actions

Al crear o actualizar un PR hacia develop/main, o subir cambios a esas ramas,
el workflow Pruebas Monarca ejecuta dos trabajos: Backend Java 21 y Frontend
Chromium. Abre la pestaña Checks del PR para ver resultados y errores.
No fusionar si fallan las pruebas; revisar el paso fallido y corregir la causa.
Si se desea imponer esta regla, el propietario debe configurar los checks como
obligatorios en las reglas de protección de la rama.

Verificación local (2026-10-18): Maven verify terminó con BUILD SUCCESS, 24 pruebas sin fallos ni errores y sin omisiones. npm ci y las cinco suites de navegador pasaron en Windows con Chrome instalado: login, flujo de compra, catálogo, encabezado y Nosotros. Las tres últimas suites incorporan las comprobaciones preparadas durante los cambios anteriores del equipo.
Estado GitHub Actions: PENDIENTE hasta subir el workflow y ejecutar el PR.
No registrar como aprobada una ejecución que todavía no ocurrió.

Referencias de configuración: [Playwright CI](https://playwright.dev/docs/ci) y [setup-java](https://github.com/actions/setup-java/tree/v6).
