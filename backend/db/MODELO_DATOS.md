# Modelo de datos de Monarca

Base de datos PostgreSQL en Supabase, esquema `monarca`. Tiene 10 tablas, y cada
una corresponde a una entidad JPA de `backend/src/main/java/com/Monarca/Backend/model`.
El backend arranca con `ddl-auto=validate`: **no crea ni modifica tablas**, solo
comprueba que el esquema coincida con las entidades.

## Diagrama entidad-relación

GitHub dibuja este diagrama automáticamente (Mermaid).

```mermaid
erDiagram
    roles ||--o{ usuarios : "asigna"
    usuarios ||--o{ pedidos : "realiza"
    categorias ||--o{ productos : "agrupa"
    productos ||--|{ variantes_producto : "tiene"
    productos ||--o{ imagenes_producto : "muestra"
    variantes_producto |o--o{ imagenes_producto : "puede tener"
    pedidos ||--|{ detalles_pedido : "contiene"
    variantes_producto ||--o{ detalles_pedido : "se vende en"
    pedidos ||--o{ pagos : "se paga con"
    metodos_pago ||--o{ pagos : "usado en"

    roles {
        bigint id_rol PK
        varchar nombre UK "ADMIN, CLIENTE"
        boolean activo
    }
    usuarios {
        bigint id_usuario PK
        bigint id_rol FK
        varchar nombres
        varchar apellidos
        varchar correo UK "único sin distinguir mayúsculas"
        varchar password_hash "BCrypt"
        varchar numero_documento
        boolean activo
    }
    categorias {
        bigint id_categoria PK
        varchar nombre UK
        varchar slug UK
        boolean activo
    }
    productos {
        bigint id_producto PK
        bigint id_categoria FK
        varchar nombre
        varchar slug UK
        varchar marca
        numeric precio_base "10,2"
        boolean activo "baja lógica"
        boolean destacado
    }
    variantes_producto {
        bigint id_variante PK
        bigint id_producto FK
        varchar sku UK
        varchar talla
        varchar color
        numeric precio "10,2"
        int stock
        int stock_minimo
        boolean activo
    }
    imagenes_producto {
        bigint id_imagen PK
        bigint id_producto FK
        bigint id_variante FK "opcional"
        text url "solo HTTPS"
        boolean principal
        int orden
    }
    pedidos {
        bigint id_pedido PK
        varchar codigo_pedido UK
        bigint id_usuario FK
        varchar estado "PENDIENTE_PAGO por defecto"
        numeric subtotal
        numeric descuento
        numeric costo_envio
        numeric total
        timestamptz fecha_pedido
    }
    detalles_pedido {
        bigint id_detalle PK
        bigint id_pedido FK
        bigint id_variante FK
        int cantidad
        numeric precio_unitario
        numeric subtotal
        varchar nombre_producto "copia histórica"
        varchar sku "copia histórica"
    }
    metodos_pago {
        bigint id_metodo_pago PK
        varchar codigo UK
        varchar nombre UK
        boolean requiere_comprobante
        boolean activo
    }
    pagos {
        bigint id_pago PK
        bigint id_pedido FK
        bigint id_metodo_pago FK
        numeric monto
        varchar estado "PENDIENTE por defecto"
        varchar numero_operacion
        timestamptz fecha_confirmacion
    }
```

## Tablas

| Tabla | Para qué sirve | Relaciones |
| --- | --- | --- |
| `roles` | Tipos de usuario (administrador, cliente). | 1 rol → muchos usuarios |
| `usuarios` | Cuentas registradas. La contraseña se guarda cifrada (`password_hash`). | Pertenece a un rol; realiza pedidos |
| `categorias` | Tops, Bottoms, Calzado, Accesorios. | 1 categoría → muchos productos |
| `productos` | Datos comunes del artículo: nombre, marca, descripción, precio base. | Tiene variantes e imágenes |
| `variantes_producto` | Combinación concreta talla + color, con su SKU, precio y **stock**. Es lo que realmente se compra. | Única por (producto, talla, color) |
| `imagenes_producto` | Fotos del producto. La principal (`principal = true`) se muestra en el catálogo. | Puede ligarse a una variante (p. ej. foto del color negro) |
| `pedidos` | Cabecera de la compra: cliente, estado y montos. | 1 pedido → muchos detalles y pagos |
| `detalles_pedido` | Cada línea del pedido. Copia nombre, SKU, talla, color y precio del momento de la compra. | Apunta a la variante vendida |
| `metodos_pago` | Catálogo de medios de pago (Yape, Plin, transferencia…). | Usado por los pagos |
| `pagos` | Registro del pago de un pedido y su comprobación manual por el administrador. | Pertenece a un pedido y a un método |

## Decisiones de diseño

- **Producto y variante separados.** El stock y el precio final viven en
  `variantes_producto`, porque una misma prenda puede tener stock distinto por
  talla o color. El carrito y los pedidos trabajan siempre con `id_variante`.
- **Copia histórica en `detalles_pedido`.** Si luego se cambia el nombre o el
  precio de un producto, los pedidos anteriores conservan lo que realmente se vendió.
- **Baja lógica.** Eliminar un producto solo pone `activo = false`, así no se
  rompen los pedidos que lo referencian.
- **Montos con `numeric(10,2)`** (BigDecimal en Java) para evitar errores de
  redondeo con dinero.
- **Estados controlados con CHECK.** Los estados de `pedidos` y `pagos` están
  restringidos en la base. Las transiciones permitidas se explican en
  [PEDIDOS.md](../../PEDIDOS.md).

## Archivos relacionados

- [README.md](README.md): consultas de diagnóstico del esquema y evidencia revisada en Supabase.
- `01_diagnostico_columnas.sql`, `02_restricciones.sql`, `03_indices.sql`: consultas de solo lectura.
- `evidencias/`: resultados exportados de restricciones e índices.
