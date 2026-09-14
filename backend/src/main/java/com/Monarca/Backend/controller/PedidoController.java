package com.Monarca.Backend.controller;

import com.Monarca.Backend.dto.OrdenDto;
import com.Monarca.Backend.dto.PedidoRequestDto;
import com.Monarca.Backend.model.Pedido;
import com.Monarca.Backend.model.Producto;
import com.Monarca.Backend.model.Usuario;
import com.Monarca.Backend.repository.ProductoRepository;
import com.Monarca.Backend.repository.UsuarioRepository;
import com.Monarca.Backend.repository.PedidoRepository; // <-- ¡ESTA ES LA LÍNEA QUE FALTABA!
import com.Monarca.Backend.service.PedidoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PedidoService pedidoService;
    
    @Autowired
    private PedidoRepository pedidoRepository;

   @GetMapping
    // Quitamos la doble validación que estaba chocando y causando el 403
    public ResponseEntity<?> listarTodosLosPedidos() {
        // Retorna toda la tabla de pedidos a tu frontend
        return ResponseEntity.ok(pedidoRepository.findAll());
    }
    // --- TU MÉTODO ORIGINAL RESTAURADO ---
    @PostMapping("/procesar")
   public ResponseEntity<?> crearPedido(@RequestBody PedidoRequestDto pedidoRequest, Authentication authentication) {
    System.out.println(">>> OBJETO RECIBIDO EN JAVA:");
    System.out.println(">>> Total: " + pedidoRequest.getTotal());
    System.out.println(">>> Items: " + pedidoRequest.getItems());
        try {
            // 1. Identificamos al cliente a través de su Token JWT
            String emailCliente = authentication.getName(); 
            
            // 2. Buscamos al cliente en la base de datos
            Usuario usuario = usuarioRepository.findByEmail(emailCliente)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado o Token inválido"));

            // 3. Le pasamos el ID del usuario validado a nuestro DTO para que el Service lo use
            pedidoRequest.setIdUsuario(usuario.getIdUsuario());

            // 4. Enviamos el carrito al servicio. Él calcula el precio real, descuenta stock y guarda todo.
            Pedido pedidoGuardado = pedidoService.procesarCompra(pedidoRequest);

            // 5. Retornamos el pedido completo con Status 200 OK
            return ResponseEntity.ok(pedidoGuardado);

        } catch (RuntimeException e) {
            // Si el servicio detecta que no hay stock o hay un error, lo atrapamos aquí 
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- MÉTODO DE PRUEBA RÁPIDA (Opcional) ---
    @PostMapping("/checkout")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> procesarPago(@RequestBody OrdenDto orden) {
        
        Producto producto = productoRepository.findById(orden.getIdProducto())
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (producto.getStockActual() < orden.getCantidad()) {
            return ResponseEntity.badRequest().body("Stock insuficiente para el producto: " + producto.getNombre());
        }

        int nuevoStock = producto.getStockActual() - orden.getCantidad();
        producto.setStockActual(nuevoStock);
        productoRepository.save(producto);

        return ResponseEntity.ok("Pago procesado y stock actualizado correctamente");
    }
}