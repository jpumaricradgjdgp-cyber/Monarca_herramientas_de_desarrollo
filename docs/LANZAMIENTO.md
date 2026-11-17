# Preparación del lanzamiento del Avance 2

## Versión integrada y versión en preparación

- La integración funcional auditada está en main, commit `0bde0f84a414753524185ea80babbd0685cdd3c7`, con ambos trabajos de CI aprobados.
- La rama `release/avance-2` parte de develop (`abb86853a6cefa6bd2fb2f3b4055a818ab4a5338`) y prepara la documentación y el laboratorio de entrega.
- El nombre propuesto para una etiqueta de esta entrega es `v0.2.0-avance2`. No se ha creado ni publicado esa etiqueta durante la preparación de estos documentos.

Esta preparación tiene lugar después del PR #10; no modifica ni reemplaza el historial anterior.

## Flujo de cierre

1. Revisar README, informe, evidencias, práctica y datos académicos del grupo.
2. Ejecutar CI del PR de release y resolver cualquier fallo.
3. Otro integrante revisa los archivos y registra su aprobación si está conforme.
4. Fusionar release/avance-2 hacia main.
5. Devolver la preparación a develop mediante PR, para que conserve la documentación del lanzamiento.
6. Si se decide presentar una release publicada, crear una etiqueta sobre el commit final revisado y preparar sus notas desde ese commit exacto.

No publicar una versión como sistema productivo completo: siguen pendientes backend público, verificación manual de Supabase y probador virtual con ML. Una versión académica puede publicarse como pre-release identificando sus límites.

## Notas de versión propuestas

**Monarca - Avance 2 de Herramientas de Desarrollo**

Incluye accesibilidad de Nosotros, filtros accesibles y encabezado móvil, diagnóstico del esquema, gestión de estados y pagos manuales de pedidos, cancelación transaccional de inventario y CI para backend/frontend. Añade informe de colaboración, matriz del equipo, evidencias y laboratorio separado de conflictos.

Validación de la versión funcional anterior: 44 pruebas de backend y seis suites de navegador, con Actions de main aprobado. Antes de etiquetar el cierre documental se debe registrar el resultado de su propio PR y commit final.

Limitaciones: H2 y API simulada en pruebas; falta evidencia del flujo manual con Supabase. El pago requiere verificación del administrador. El probador virtual no está implementado.

## Papel de hotfix

Un hotfix se reservaría para una reparación urgente de una versión de producción: se crea desde main y se integra también en develop y, si corresponde, en una release activa. No se ha ejecutado ese escenario en Monarca. La corrección del PR #9 se desarrolló sobre develop como fix de pruebas.
