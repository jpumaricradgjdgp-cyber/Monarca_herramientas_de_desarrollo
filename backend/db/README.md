# Diagnóstico del esquema Monarca

Consultas de solo lectura basadas en las 10 entidades de esta rama. Consultan
metadatos; no crean tablas, cambian restricciones ni muestran registros de clientes.
El backend usa `spring.jpa.hibernate.ddl-auto=validate`: necesita un esquema ya creado.

## Ejecución en Supabase

1. Abre el proyecto correcto y entra a SQL Editor > New query.
2. Copia el contenido completo de `01_diagnostico_columnas.sql` y pulsa Run.
3. Repite por separado con `02_restricciones.sql` y `03_indices.sql`.
4. Revisa los resultados antes de proponer cualquier migración.

La primera consulta devuelve 93 filas, una por columna mapeada. `FALTA_TABLA`
y `FALTA_COLUMNA` indican objetos ausentes o no visibles para el usuario SQL.
`REVISAR_TIPO` y `REVISAR_NULOS` requieren comparación con la entidad Java.
`OK` solo cubre existencia, tipo y los NOT NULL explícitos en @Column/@JoinColumn.
No certifica la integridad completa ni sustituye arrancar el backend.

Longitudes, precisión, escala, identidad y valores por defecto se muestran para
revisión manual: comparar con las anotaciones de `src/main/java/com/Monarca/Backend/model`.
Los importes BigDecimal están mapeados con precisión 10 y escala 2.
Los identificadores @Id deben ser claves primarias y contar con generación de valor
(IDENTITY o secuencia compatible). Su nulabilidad se verifica al revisar la PK.

## Revisión de restricciones

- Cada una de las 10 tablas debe tener su clave primaria.
- Relaciones: usuarios -> roles; productos -> categorias; variantes_producto -> productos;
  imagenes_producto -> productos y variantes_producto; pedidos -> usuarios;
  detalles_pedido -> pedidos y variantes_producto; pagos -> pedidos y metodos_pago.
- Revisar unicidad de categorias(nombre y slug), productos(slug), roles(nombre),
  metodos_pago(codigo y nombre), pedidos(codigo_pedido), variantes_producto(sku)
  y variantes_producto(id_producto, talla, color). Los nombres de las restricciones
  pueden variar; comparar sus columnas y definiciones, no solo su nombre.
- Revisar los índices existentes antes de proponer nuevos; la tercera consulta
  permite detectar unicidad implementada mediante índices.
- El servicio actual crea pedidos con `PENDIENTE_PAGO` y pagos con `PENDIENTE`.
  Los CHECK deben admitir esos valores. No ejecutar cambios basados únicamente
  en una lista de estados tomada de otra versión del proyecto.
- En esta rama PedidoController no tiene un endpoint para actualizar estados.
  Ese flujo necesita trabajo de backend; estas consultas no lo implementan.

## Evidencia revisada

Revisión de los resultados aportados en esta conversación: 2026-10-18.
La hora exacta de ejecución en Supabase no consta en los CSV.

- Columnas: el usuario informó que todos los resultados fueron `OK`. La consulta
  contempla 93 columnas; no se adjuntó el CSV de esta primera consulta.
- Restricciones: el CSV adjunto contiene 43, todas con `validada=true`:
  10 PRIMARY KEY, 10 FOREIGN KEY, 9 UNIQUE y 14 CHECK. Las claves primarias,
  relaciones y unicidad esperadas en la lista anterior están presentes.
- Los CHECK admiten los estados iniciales usados por PedidoService:
  pedidos `PENDIENTE_PAGO` y pagos `PENDIENTE`.
- Índices: 35 en total, 21 únicos y 14 no únicos. Incluyen los índices de las
  10 claves primarias y las 9 restricciones UNIQUE.
- Además, usuarios tiene unicidad por `lower(correo)` y por `numero_documento`
  cuando este no es NULL. Esto explica por qué esas dos reglas no aparecían
  como restricciones UNIQUE en la segunda consulta.
- Hay índices para búsquedas por usuario, estado y fecha de pedidos, por pedido
  en pagos y detalles, y por categoría de productos, entre otros.

Archivos originales de metadatos, sin cambios en su contenido:

- [Restricciones](evidencias/restricciones.csv).
- [Índices](evidencias/indices.csv).

No se propone una migración a partir de estas comprobaciones. El inventario de
índices no mide rendimiento ni prueba que cada índice sea utilizado; esa evaluación
requiere consultas reales y planes de ejecución. Tampoco comprueba por sí solo
la validez operativa de todos los índices.

Pendiente: revisar longitudes, precisión, escala y generación de identificadores
con la salida completa de columnas, y arrancar el backend contra esta base.
Esta evidencia no certifica el flujo completo de pedidos ni implementa cambios
de estado. La revisión de otro integrante del PR también queda pendiente.
