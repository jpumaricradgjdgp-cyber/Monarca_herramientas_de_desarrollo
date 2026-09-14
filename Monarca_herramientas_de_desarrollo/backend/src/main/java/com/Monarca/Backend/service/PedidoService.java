package com.Monarca.Backend.service;

import com.Monarca.Backend.dto.ItemCarritoDto;
import com.Monarca.Backend.dto.PedidoRequestDto;
import com.Monarca.Backend.model.*;
import com.Monarca.Backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PedidoService {

    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private MetodoPagoRepository metodoPagoRepository; 
    @Autowired private EstatusEnvioRepository estatusEnvioRepository; 

    @Transactional
    public Pedido procesarCompra(PedidoRequestDto pedidoDto) {
        
        // 1. Obtener Usuario
        Usuario usuario = usuarioRepository.findById(pedidoDto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Crear y configurar Pedido base
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstado("PENDIENTE");

        // 3. Asignar Método de Pago
        MetodoPago metodo = metodoPagoRepository.findById(pedidoDto.getIdMetodoPago())
            .orElseThrow(() -> new RuntimeException("Método de pago no encontrado"));
        pedido.setMetodoPago(metodo);

        // 4. Asignar Estatus de Envío
        EstatusEnvio estatus = estatusEnvioRepository.findById(pedidoDto.getIdEstatusEnvio())
            .orElseThrow(() -> new RuntimeException("Estatus de envío no encontrado"));
        pedido.setEstatusEnvio(estatus);

        // 5. Procesar Items, descontar stock y calcular total
        Double totalCalculado = 0.0;

        for (ItemCarritoDto item : pedidoDto.getItems()) {
            Producto producto = productoRepository.findById(item.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getIdProducto()));

            if (producto.getStockActual() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente: " + producto.getNombre());
            }

            // Descontar stock del inventario
            producto.setStockActual(producto.getStockActual() - item.getCantidad());
            productoRepository.save(producto);

            // Calcular precio y sumarlo al total del pedido
            Double precioUnitario = producto.getPrecioBase().doubleValue();
            totalCalculado += (precioUnitario * item.getCantidad());
        }

        // 6. Asignar el total final y guardar (SIN los detalles)
        pedido.setTotal(totalCalculado);

        return pedidoRepository.save(pedido);
    }
}