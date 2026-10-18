package com.Monarca.Backend.repository;

import com.Monarca.Backend.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PagoRepository
        extends JpaRepository<Pago, Long> {
    List<Pago> findByPedido_IdPedido(Long idPedido);

}