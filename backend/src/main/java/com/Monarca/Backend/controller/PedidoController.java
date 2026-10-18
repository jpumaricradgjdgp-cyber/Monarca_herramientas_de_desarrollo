package com.Monarca.Backend.controller;

import com.Monarca.Backend.dto.PedidoRequestDto;
import com.Monarca.Backend.dto.PedidoEstadoDto;
import com.Monarca.Backend.service.PedidoEstadoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.CannotAcquireLockException;
import com.Monarca.Backend.model.Pedido;
import com.Monarca.Backend.model.Usuario;
import com.Monarca.Backend.repository.PedidoRepository;
import com.Monarca.Backend.repository.UsuarioRepository;
import com.Monarca.Backend.service.PedidoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {
    @Autowired
    private PedidoEstadoService pedidoEstadoService;

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id,
            @RequestBody(required = false) PedidoEstadoDto solicitud) {
        try {
            Pedido pedido = pedidoEstadoService.actualizar(id, solicitud);
            return ResponseEntity.ok(Map.of("idPedido", pedido.getIdPedido(), "estado", pedido.getEstado()));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", ex.getReason()));
        } catch (DataIntegrityViolationException | CannotAcquireLockException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error",
                    "No se pudo guardar el estado. Actualiza la lista y revisa los datos del pedido."));
        }
    }


    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoRepository pedidoRepository;


    // =========================================================
    // LISTAR PEDIDOS
    // =========================================================

    @GetMapping
    public ResponseEntity<?> listarTodosLosPedidos() {

        List<Map<String, Object>> respuesta =
                pedidoRepository
                        .findAll()
                        .stream()
                        .map(pedido ->
                                Map.<String, Object>of(
                                        "idPedido",
                                        pedido.getIdPedido(),

                                        "codigoPedido",
                                        pedido.getCodigoPedido(),

                                        "estado",
                                        pedido.getEstado(),

                                        "subtotal",
                                        pedido.getSubtotal(),

                                        "total",
                                        pedido.getTotal(),

                                        "cliente",
                                        pedido
                                                .getUsuario()
                                                .getCorreo(),

                                        "fechaPedido",
                                        pedido.getFechaPedido()
                                )
                        )
                        .toList();


        return ResponseEntity.ok(
                respuesta
        );
    }


    // =========================================================
    // CREAR PEDIDO
    // =========================================================

    @PostMapping("/procesar")
    public ResponseEntity<?> crearPedido(
            @RequestBody PedidoRequestDto pedidoRequest,
            Authentication authentication
    ) {

        try {

            if (authentication == null
                    || !authentication.isAuthenticated()) {

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(
                                Map.of(
                                        "error",
                                        "Usuario no autenticado"
                                )
                        );
            }


            // Usuario obtenido del JWT
            String correoCliente =
                    authentication.getName();


            Usuario usuario =
                    usuarioRepository
                            .findByCorreoIgnoreCase(
                                    correoCliente
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Cliente no encontrado"
                                    )
                            );


            // Nunca confiamos en el idUsuario
            // enviado por el navegador.
            pedidoRequest.setIdUsuario(
                    usuario.getIdUsuario()
            );


            Pedido pedidoGuardado =
                    pedidoService.procesarCompra(
                            pedidoRequest
                    );


            Map<String, Object> response =
                    Map.of(
                            "mensaje",
                            "Pedido registrado correctamente",

                            "idPedido",
                            pedidoGuardado.getIdPedido(),

                            "codigoPedido",
                            pedidoGuardado.getCodigoPedido(),

                            "estado",
                            pedidoGuardado.getEstado(),

                            "total",
                            pedidoGuardado.getTotal()
                    );


            return ResponseEntity.ok(
                    response
            );


        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }
}