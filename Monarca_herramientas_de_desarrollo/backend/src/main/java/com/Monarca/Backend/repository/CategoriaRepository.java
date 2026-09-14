package com.Monarca.Backend.repository;

import com.Monarca.Backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    
    // Con solo extender de JpaRepository, Spring Boot te regala todos estos métodos:
    // save(), findAll(), findById(), deleteById()... ¡No tienes que programarlos!
    
}