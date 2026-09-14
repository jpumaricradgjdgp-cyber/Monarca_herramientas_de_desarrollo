package com.Monarca.Backend.repository;

import com.Monarca.Backend.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
    // Util para asignar el rol predeterminado "ROLE_CLIENTE" en los registros
    Optional<Rol> findByNombre(String nombre);
}   