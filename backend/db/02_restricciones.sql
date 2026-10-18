-- Inventario de PK, FK, UNIQUE y CHECK. Funciona aunque falte una tabla.
-- Revisar especialmente CHECK de pedidos.estado y pagos.estado.
SELECT t.relname AS tabla, c.conname AS restriccion,
       CASE c.contype WHEN 'p' THEN 'PRIMARY KEY' WHEN 'f' THEN 'FOREIGN KEY'
         WHEN 'u' THEN 'UNIQUE' WHEN 'c' THEN 'CHECK' END AS tipo,
       pg_get_constraintdef(c.oid) AS definicion,
       c.convalidated AS validada
FROM pg_constraint c
JOIN pg_class t ON t.oid = c.conrelid
JOIN pg_namespace n ON n.oid = t.relnamespace
WHERE n.nspname = 'monarca'
  AND t.relname IN ('categorias','productos','variantes_producto',
    'imagenes_producto','roles','usuarios','metodos_pago','pedidos',
    'detalles_pedido','pagos')
  AND c.contype IN ('p','f','u','c')
ORDER BY tabla, tipo, restriccion;
