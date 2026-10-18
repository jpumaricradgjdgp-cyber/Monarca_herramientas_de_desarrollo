package com.Monarca.Backend.service;

import com.Monarca.Backend.dto.PedidoEstadoDto;
import com.Monarca.Backend.model.*;
import com.Monarca.Backend.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:estados_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS monarca",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "jwt.secret=clave-exclusiva-de-integracion-no-usar-en-produccion",
        "spring.mail.host=", "monarca.mail.from=", "monarca.password-reset-url="
})
class PedidoEstadoIntegracionTest {
    @Autowired PedidoEstadoService servicio;
    @Autowired PedidoRepository pedidos;
    @Autowired PagoRepository pagos;
    @Autowired DetallePedidoRepository detalles;
    @Autowired VarianteProductoRepository variantes;
    @Autowired ProductoRepository productos;
    @Autowired CategoriaRepository categorias;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired MetodoPagoRepository metodos;
    @Autowired org.springframework.transaction.PlatformTransactionManager tx;

    record Datos(Long pedido, Long primera, Long segunda) {}
    Datos preparar(boolean desbordamiento) {
        return new TransactionTemplate(tx).execute(status -> {
            String clave = UUID.randomUUID().toString();
            Rol rol = new Rol(); rol.setNombre(clave.substring(0, 20)); roles.save(rol);
            Usuario usuario = new Usuario(); usuario.setRol(rol); usuario.setNombres("Prueba"); usuario.setApellidos("Pedido");
            usuario.setCorreo(clave+"@example.test"); usuario.setPassword("hash-de-fixture"); usuarios.save(usuario);
            Categoria cat = new Categoria(); cat.setNombre(clave); cat.setSlug(clave); categorias.save(cat);
            Producto producto = new Producto(); producto.setCategoria(cat); producto.setNombre("Prenda"); producto.setSlug(clave);
            producto.setPrecioBase(new BigDecimal("10.00")); productos.save(producto);
            VarianteProducto primera = variante(producto, clave+"-S", "S", 3);
            VarianteProducto segunda = desbordamiento ? variante(producto, clave+"-M", "M", Integer.MAX_VALUE) : null;
            Pedido pedido = new Pedido(); pedido.setUsuario(usuario); pedido.setCodigoPedido(clave);
            pedido.setSubtotal(new BigDecimal("20.00")); pedido.setTotal(new BigDecimal("20.00")); pedidos.save(pedido);
            linea(pedido, primera, desbordamiento ? 1 : 2);
            if (segunda != null) linea(pedido, segunda, 1);
            MetodoPago metodo = new MetodoPago(); metodo.setNombre(clave); metodo.setCodigo(clave.substring(0, 20)); metodos.save(metodo);
            Pago pago = new Pago(); pago.setPedido(pedido); pago.setMetodoPago(metodo); pago.setMonto(pedido.getTotal()); pagos.save(pago);
            return new Datos(pedido.getIdPedido(), primera.getIdVariante(), segunda == null ? null : segunda.getIdVariante());
        });
    }
    VarianteProducto variante(Producto p, String sku, String talla, int stock) {
        VarianteProducto v = new VarianteProducto(); v.setProducto(p); v.setSku(sku); v.setTalla(talla); v.setColor("Negro");
        v.setPrecio(new BigDecimal("10.00")); v.setStock(stock); return variantes.save(v);
    }
    void linea(Pedido pedido, VarianteProducto v, int cantidad) {
        DetallePedido d = new DetallePedido(); d.setPedido(pedido); d.setVariante(v); d.setCantidad(cantidad);
        d.setNombreProducto("Prenda"); d.setSku(v.getSku()); d.setPrecioUnitario(v.getPrecio());
        d.setSubtotal(v.getPrecio().multiply(BigDecimal.valueOf(cantidad))); detalles.save(d);
    }
    @Test void pagoYPedidoPersistenJuntosConLaReferencia() {
        Datos datos = preparar(false);
        servicio.actualizar(datos.pedido(), new PedidoEstadoDto("PAGADO", "BANCO-123"));
        assertEquals("PAGADO", pedidos.findById(datos.pedido()).orElseThrow().getEstado());
        Pago pago = pagos.findByPedido_IdPedido(datos.pedido()).getFirst();
        assertEquals("PAGADO", pago.getEstado()); assertEquals("BANCO-123", pago.getNumeroOperacion());
        assertNotNull(pago.getFechaConfirmacion());
        assertEquals(3, variantes.findById(datos.primera()).orElseThrow().getStock());
    }
    @Test void falloEnSegundaVarianteRevierteLaPrimeraYLosEstados() {
        Datos datos = preparar(true);
        assertThrows(ResponseStatusException.class, () -> servicio.actualizar(datos.pedido(), new PedidoEstadoDto("CANCELADO", null)));
        assertEquals(3, variantes.findById(datos.primera()).orElseThrow().getStock());
        assertEquals(Integer.MAX_VALUE, variantes.findById(datos.segunda()).orElseThrow().getStock());
        assertEquals("PENDIENTE_PAGO", pedidos.findById(datos.pedido()).orElseThrow().getEstado());
        assertEquals("PENDIENTE", pagos.findByPedido_IdPedido(datos.pedido()).getFirst().getEstado());
    }
    @Test void dosCancelacionesSimultaneasDevuelvenElStockUnaSolaVez() throws Exception {
        Datos datos = preparar(false);
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            CountDownLatch inicio = new CountDownLatch(1);
            Callable<Void> cancelar = () -> { inicio.await(); servicio.actualizar(datos.pedido(), new PedidoEstadoDto("CANCELADO", null)); return null; };
            Future<Void> primera = pool.submit(cancelar), segunda = pool.submit(cancelar);
            inicio.countDown(); primera.get(10, TimeUnit.SECONDS); segunda.get(10, TimeUnit.SECONDS);
        }
        assertEquals(5, variantes.findById(datos.primera()).orElseThrow().getStock());
        assertEquals("CANCELADO", pagos.findByPedido_IdPedido(datos.pedido()).getFirst().getEstado());
    }
}
