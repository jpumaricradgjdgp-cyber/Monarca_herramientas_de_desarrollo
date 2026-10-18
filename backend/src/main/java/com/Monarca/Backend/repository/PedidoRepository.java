package com.Monarca.Backend.repository;

import com.Monarca.Backend.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository
        extends JpaRepository<Pedido, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.idPedido = :id")
    Optional<Pedido> findByIdForUpdate(@Param("id") Long id);


    List<Pedido> findByUsuario_IdUsuario(
            Long idUsuario
    );
}