package com.Monarca.Backend.repository;

import com.Monarca.Backend.model.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {
    // JpaRepository ya incluye: save(), findById(), findAll(), deleteById(), etc.
}