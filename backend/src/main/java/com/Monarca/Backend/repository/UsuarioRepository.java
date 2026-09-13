package com.Monarca.Backend.repository;

import com.Monarca.Backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    // Este método será vital para que Spring Security busque al usuario por su correo al loguearse
    Optional<Usuario> findByEmail(String email);
}