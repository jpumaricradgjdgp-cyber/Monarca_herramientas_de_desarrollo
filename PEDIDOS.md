# Gestión de pedidos y pagos manuales

El administrador puede actualizar pedidos desde `fronted/Paginas/admin.html`.
Reinicia el backend para cargar el nuevo endpoint y recarga el navegador con
Ctrl+F5. La URL del backend sigue siendo la configurada actualmente en tienda.js.

## Uso

1. Inicia sesión con una cuenta ADMIN y abre el administrador.
2. En Gestión de Pedidos localiza el pedido pendiente.
3. Comprueba el abono en el medio de pago utilizado por el cliente.
4. Selecciona Confirmar pago y escribe el número de operación o comprobante.
5. Pulsa Guardar estado. El pedido y su pago pasarán a PAGADO juntos.
6. Avanza a Confirmar pedido, Preparar pedido o Marcar enviado según el trabajo
   realizado. Después de enviarlo, puedes marcarlo como entregado.

Confirmar pago registra una comprobación manual. No cobra dinero ni consulta
automáticamente bancos, Yape o Plin. La referencia se guarda en pagos.numero_operacion.

Para cancelar un pedido pendiente, selecciona Cancelar pedido y confirma la acción.
Se devuelve el stock de cada variante y se marca el pago como CANCELADO en la misma
transacción. Repetir la misma cancelación no vuelve a sumar stock.

## Transiciones admitidas

| Desde | Hacia |
| --- | --- |
| PENDIENTE_PAGO | PAGADO o CANCELADO |
| PAGADO | CONFIRMADO, PREPARANDO o ENVIADO |
| CONFIRMADO | PREPARANDO o ENVIADO |
| PREPARANDO | ENVIADO |
| ENVIADO | ENTREGADO |
| ENTREGADO / CANCELADO | Sin nuevas transiciones |

Repetir el estado actual se acepta sin volver a modificar pago o inventario.
Un pedido pagado requiere un proceso de reembolso para cancelarse: ese proceso
queda pendiente y esta pantalla no permite realizarlo.

## Contrato y reglas del servidor

PUT /api/pedidos/{id}/estado, solo ROLE_ADMIN:

```json
{"estado":"PAGADO","referenciaPago":"OPERACION-123"}
```

La referencia es obligatoria al confirmar el pago y admite hasta 100 caracteres.
Se exige un único registro de pago cuyo monto coincida con el total del pedido.
Un pago rechazado se puede cancelar, pero no confirmar directamente.
Los pasos posteriores al pago exigen que su registro siga marcado como PAGADO.
Los estados coinciden con las restricciones de Supabase documentadas en backend/db.
No se modifica el esquema de la base de datos.

Respuestas: 200 con idPedido y estado; 400 para datos inválidos; 403 para acceso
no autorizado; 404 si no existe; 409 para transiciones o datos incompatibles.
Ante un error, la pantalla conserva los campos y muestra el mensaje junto al pedido.

El pedido se bloquea mientras cambia de estado. La cancelación bloquea las variantes
en orden de identificador para serializar cambios de inventario. Un error revierte
toda la operación. No editar estados directamente en SQL durante el uso del sistema,
porque eso omitiría estas reglas del servicio.

## Validación

Se añadieron pruebas de permisos, referencia, importe, transiciones, reintentos y
stock. Las pruebas de integración usan H2 y repositorios reales para comprobar que
pago y pedido se guardan juntos, que un fallo revierte la devolución y que dos
cancelaciones concurrentes restituyen el stock una sola vez.

El test de navegador usa una API simulada y comprueba acciones, referencia requerida,
conflictos, conservación de campos, bloqueo del doble envío y permisos denegados.
Para ejecutar todo, sigue PRUEBAS.md.

Pendiente de validación manual contra Supabase: crear un pedido de prueba, confirmar
su pago con una referencia y avanzar sus estados; crear otro y cancelarlo para
comprobar el stock. Esta prueba real debe hacerse con datos de prueba y un administrador.
La ejecución de GitHub Actions de este nuevo PR queda pendiente hasta subirlo.
