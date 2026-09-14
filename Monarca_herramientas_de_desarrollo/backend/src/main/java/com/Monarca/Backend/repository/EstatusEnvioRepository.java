package com.Monarca.Backend.repository;

import com.Monarca.Backend.model.EstatusEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstatusEnvioRepository extends JpaRepository<EstatusEnvio, Integer> {
    // JpaRepository ya incluye: save(), findById(), findAll(), deleteById(), etc.
}