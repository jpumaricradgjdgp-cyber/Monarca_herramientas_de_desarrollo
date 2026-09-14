package com.Monarca.Backend.repository;

import com.Monarca.Backend.model.FamiliaProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FamiliaProductoRepository extends JpaRepository<FamiliaProducto, Integer> {
}