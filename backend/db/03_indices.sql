-- Incluye indices unicos que pueden existir sin restriccion UNIQUE.
SELECT tablename AS tabla, indexname AS indice, indexdef AS definicion
FROM pg_indexes
WHERE schemaname = 'monarca'
  AND tablename IN ('categorias','productos','variantes_producto',
    'imagenes_producto','roles','usuarios','metodos_pago','pedidos',
    'detalles_pedido','pagos')
ORDER BY tabla, indice;
