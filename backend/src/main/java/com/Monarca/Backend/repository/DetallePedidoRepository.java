package com.Monarca.Backend.repository;

import com.Monarca.Backend.model.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DetallePedidoRepository
        extends JpaRepository<DetallePedido, Long> {
    List<DetallePedido> findByPedido_IdPedido(Long idPedido);

}