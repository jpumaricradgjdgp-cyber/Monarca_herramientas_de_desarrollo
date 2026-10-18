package com.Monarca.Backend.service;

import com.Monarca.Backend.dto.PedidoEstadoDto;
import com.Monarca.Backend.model.*;
import com.Monarca.Backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PedidoEstadoServiceTest {
    PedidoRepository pedidos = mock(PedidoRepository.class);
    PagoRepository pagos = mock(PagoRepository.class);
    DetallePedidoRepository detalles = mock(DetallePedidoRepository.class);
    VarianteProductoRepository variantes = mock(VarianteProductoRepository.class);
    PedidoEstadoService servicio = new PedidoEstadoService(pedidos, pagos, detalles, variantes);
    Pedido pedido;
    Pago pago;

    @BeforeEach void preparar() {
        pedido = new Pedido(); pedido.setIdPedido(1L); pedido.setTotal(new BigDecimal("130.00"));
        pago = new Pago(); pago.setMonto(new BigDecimal("130.00"));
        when(pedidos.findByIdForUpdate(1L)).thenReturn(Optional.of(pedido));
        when(pedidos.save(any())).thenAnswer(i -> i.getArgument(0));
        when(pagos.findByPedido_IdPedido(1L)).thenReturn(List.of(pago));
    }
    ResponseStatusException falla(String destino, String referencia) {
        return assertThrows(ResponseStatusException.class, () -> servicio.actualizar(1L, new PedidoEstadoDto(destino, referencia)));
    }
    @Test void pagoVerificadoGuardaEstadoCompatibleReferenciaYFechas() {
        servicio.actualizar(1L, new PedidoEstadoDto("PAGADO", "  YAPE-123  "));
        assertEquals("PAGADO", pedido.getEstado()); assertEquals("PAGADO", pago.getEstado());
        assertEquals("YAPE-123", pago.getNumeroOperacion());
        assertNotNull(pago.getFechaPago()); assertNotNull(pago.getFechaConfirmacion());
        verifyNoInteractions(variantes, detalles);
    }
    @Test void pagoEnRevisionTambienPuedeConfirmarse() {
        pago.setEstado("EN_REVISION");
        servicio.actualizar(1L, new PedidoEstadoDto("PAGADO", "BANCO-1"));
        assertEquals("PAGADO", pago.getEstado());
    }
    @Test void pagoRequiereReferenciaValidaAntesDeTocarElPedido() {
        for (String ref : new String[]{null, "  ", "x".repeat(101)}) {
            assertEquals(400, falla("PAGADO", ref).getStatusCode().value());
        }
        verify(pedidos, never()).findByIdForUpdate(anyLong());
    }
    @Test void estadoInexistenteEs400YPedidoAusente404() {
        assertEquals(400, falla("BORRADO", null).getStatusCode().value());
        when(pedidos.findByIdForUpdate(1L)).thenReturn(Optional.empty());
        assertEquals(404, falla("CANCELADO", null).getStatusCode().value());
    }
    @Test void noSePuedeEnviarSinPagar() {
        assertEquals(409, falla("ENVIADO", null).getStatusCode().value());
        assertEquals("PENDIENTE_PAGO", pedido.getEstado());
        verify(pagos, never()).save(any());
    }
    @Test void pagoConMontoDistintoNoSeConfirma() {
        pago.setMonto(new BigDecimal("0.01"));
        assertEquals(409, falla("PAGADO", "YAPE-1").getStatusCode().value());
        assertEquals("PENDIENTE", pago.getEstado());
    }
    @Test void pagoRechazadoNoSeConfirma() {
        pago.setEstado("RECHAZADO");
        assertEquals(409, falla("PAGADO", "YAPE-1").getStatusCode().value());
    }
    @Test void pagosAusentesODuplicadosRequierenRevision() {
        for (List<Pago> registros : List.of(List.<Pago>of(), List.of(pago, pago))) {
            when(pagos.findByPedido_IdPedido(1L)).thenReturn(registros);
            assertEquals(409, falla("PAGADO", "YAPE-1").getStatusCode().value());
        }
    }
    @Test void repetirCancelacionDevuelveStockUnaSolaVez() {
        VarianteProducto variante = new VarianteProducto(); variante.setIdVariante(10L); variante.setStock(3);
        DetallePedido linea = new DetallePedido(); linea.setVariante(variante); linea.setCantidad(2);
        when(detalles.findByPedido_IdPedido(1L)).thenReturn(List.of(linea));
        when(variantes.findByIdForUpdate(10L)).thenReturn(Optional.of(variante));
        servicio.actualizar(1L, new PedidoEstadoDto("CANCELADO", null));
        servicio.actualizar(1L, new PedidoEstadoDto("CANCELADO", null));
        assertEquals(5, variante.getStock()); assertEquals("CANCELADO", pago.getEstado());
        verify(variantes, times(1)).save(variante); verify(pagos, times(1)).save(pago);
    }
    @Test void cancelacionSinDetallesNoSeGuarda() {
        when(detalles.findByPedido_IdPedido(1L)).thenReturn(List.of());
        assertEquals(409, falla("CANCELADO", null).getStatusCode().value());
        verify(pagos, never()).save(any());
    }
    @Test void estadosPagadosAvanzanHastaEntregaSinDescontarMasStock() {
        servicio.actualizar(1L, new PedidoEstadoDto("PAGADO", "YAPE-1"));
        for (String estado : List.of("CONFIRMADO", "PREPARANDO", "ENVIADO", "ENTREGADO")) {
            servicio.actualizar(1L, new PedidoEstadoDto(estado, null));
            assertEquals(estado, pedido.getEstado());
        }
        assertEquals(409, falla("PAGADO", "YAPE-1").getStatusCode().value());
        verifyNoInteractions(variantes, detalles);
    }
    @Test void pedidosPagadosNoSeCancelanSinFlujoDeReembolso() {
        pedido.setEstado("PAGADO"); pago.setEstado("PAGADO");
        assertEquals(409, falla("CANCELADO", null).getStatusCode().value());
        verifyNoInteractions(variantes, detalles);
    }
    @Test void pedidoMarcadoPagadoConPagoPendienteNoSeEnvia() {
        pedido.setEstado("PAGADO");
        assertEquals(409, falla("ENVIADO", null).getStatusCode().value());
    }
}
