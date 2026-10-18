package com.Monarca.Backend.service;

import com.Monarca.Backend.dto.PedidoEstadoDto;
import com.Monarca.Backend.model.*;
import com.Monarca.Backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class PedidoEstadoService {
    private static final Map<String, Set<String>> TRANSICIONES = Map.of(
            "PENDIENTE_PAGO", Set.of("PAGADO", "CANCELADO"),
            "PAGADO", Set.of("CONFIRMADO", "PREPARANDO", "ENVIADO"),
            "CONFIRMADO", Set.of("PREPARANDO", "ENVIADO"),
            "PREPARANDO", Set.of("ENVIADO"),
            "ENVIADO", Set.of("ENTREGADO"));
    private static final Set<String> ESTADOS = Set.of("PENDIENTE_PAGO", "PAGADO", "CONFIRMADO",
            "PREPARANDO", "ENVIADO", "ENTREGADO", "CANCELADO");
    private final PedidoRepository pedidos;
    private final PagoRepository pagos;
    private final DetallePedidoRepository detalles;
    private final VarianteProductoRepository variantes;

    public PedidoEstadoService(PedidoRepository pedidos, PagoRepository pagos,
                               DetallePedidoRepository detalles, VarianteProductoRepository variantes) {
        this.pedidos = pedidos;
        this.pagos = pagos;
        this.detalles = detalles;
        this.variantes = variantes;
    }

    @Transactional
    public Pedido actualizar(Long id, PedidoEstadoDto solicitud) {
        if (id == null || id <= 0 || solicitud == null || !ESTADOS.contains(
                solicitud.estado() == null ? "" : solicitud.estado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica un pedido y un estado válidos.");
        }
        String destino = solicitud.estado();
        String referencia = solicitud.referenciaPago() == null ? "" : solicitud.referenciaPago().trim();
        if ("PAGADO".equals(destino) && (referencia.isBlank() || referencia.length() > 100)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Indica la referencia del pago verificado (máximo 100 caracteres).");
        }
        // Serializa confirmaciones y cancelaciones concurrentes sobre el mismo pedido.
        Pedido pedido = pedidos.findByIdForUpdate(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado."));
        if (destino.equals(pedido.getEstado())) return pedido;
        if (!TRANSICIONES.getOrDefault(pedido.getEstado(), Set.of()).contains(destino)) {
            throw conflicto("No se puede cambiar de " + pedido.getEstado() + " a " + destino + ". Actualiza la lista.");
        }
        List<Pago> registros = pagos.findByPedido_IdPedido(id);
        if (registros.size() != 1) throw conflicto("El pedido debe tener un único registro de pago. Revisa sus datos.");
        Pago pago = registros.getFirst();
        if (pago.getMonto() == null || pedido.getTotal() == null || pago.getMonto().compareTo(pedido.getTotal()) != 0) {
            throw conflicto("El monto del pago no coincide con el total del pedido.");
        }
        if ("PENDIENTE_PAGO".equals(pedido.getEstado())) {
            if (!Set.of("PENDIENTE", "EN_REVISION", "RECHAZADO").contains(pago.getEstado())) {
                throw conflicto("El estado del pago no permite esta operación.");
            }
            if ("PAGADO".equals(destino)) {
                if ("RECHAZADO".equals(pago.getEstado())) throw conflicto("No se puede confirmar un pago rechazado.");
                OffsetDateTime ahora = OffsetDateTime.now();
                // PAGADO coincide con chk_pago_estado del esquema real de Supabase.
                pago.setEstado("PAGADO");
                pago.setNumeroOperacion(referencia);
                pago.setFechaPago(ahora);
                pago.setFechaConfirmacion(ahora);
            } else {
                devolverStock(id);
                pago.setEstado("CANCELADO");
            }
            pagos.save(pago);
        } else if (!"PAGADO".equals(pago.getEstado())) {
            throw conflicto("Primero verifica el pago antes de preparar o enviar el pedido.");
        }
        pedido.setEstado(destino);
        return pedidos.save(pedido);
    }

    private void devolverStock(Long idPedido) {
        List<DetallePedido> lineas = detalles.findByPedido_IdPedido(idPedido);
        if (lineas.isEmpty()) throw conflicto("El pedido no tiene detalles para devolver stock.");
        Map<Long, Integer> cantidades = new TreeMap<>();
        try {
            for (DetallePedido linea : lineas) {
                if (linea.getVariante() == null || linea.getVariante().getIdVariante() == null
                        || linea.getCantidad() == null || linea.getCantidad() <= 0) {
                    throw conflicto("Hay detalles inválidos en el pedido.");
                }
                cantidades.merge(linea.getVariante().getIdVariante(), linea.getCantidad(), Math::addExact);
            }
            // Mismo orden de bloqueo de variantes que en el checkout para evitar interbloqueos.
            for (var entrada : cantidades.entrySet()) {
                VarianteProducto variante = variantes.findByIdForUpdate(entrada.getKey())
                        .orElseThrow(() -> conflicto("No se encontró una variante del pedido."));
                if (variante.getStock() == null || variante.getStock() < 0) throw conflicto("Stock inválido.");
                variante.setStock(Math.addExact(variante.getStock(), entrada.getValue()));
                variantes.save(variante);
            }
        } catch (ArithmeticException ex) {
            throw conflicto("La devolución excede el stock permitido. Revisa los datos del pedido.");
        }
    }

    private ResponseStatusException conflicto(String mensaje) {
        return new ResponseStatusException(HttpStatus.CONFLICT, mensaje);
    }
}
