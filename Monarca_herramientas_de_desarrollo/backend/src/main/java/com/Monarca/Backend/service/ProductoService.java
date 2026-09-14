package com.Monarca.Backend.service;

import com.Monarca.Backend.dto.ProductoDto;
import com.Monarca.Backend.model.Producto;
import com.Monarca.Backend.model.FamiliaProducto; // <-- Importación crucial
import com.Monarca.Backend.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal; // <-- Importación crucial para el precio
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    public Optional<Producto> buscarPorId(Integer id) {
        return productoRepository.findById(id);
    }

  // Modifica ligeramente tu método guardar para incluir la descripción
public void guardar(ProductoDto dto) {
    Producto producto = new Producto();
    producto.setNombre(dto.getNombre());
    producto.setTalla(dto.getTalla());
    producto.setPrecioBase(BigDecimal.valueOf(dto.getPrecio()));
    producto.setStockActual(dto.getStock());
    producto.setImagen(dto.getImg());
    
    FamiliaProducto familia = new FamiliaProducto();
    familia.setIdFamilia(1); 
    producto.setFamiliaProducto(familia);
    
    // Campos obligatorios adicionales
    producto.setSku("SKU-" + System.currentTimeMillis()); 
    producto.setColor("N/A"); 
    producto.setDescripcion("Sin descripción"); // <--- Añade esto
    producto.setActivo(true);
    
    productoRepository.save(producto);
}

// Y AGREGA ESTE NUEVO MÉTODO PARA EDITAR
public void actualizar(Integer id, ProductoDto dto) {
    Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            
    producto.setNombre(dto.getNombre());
    producto.setTalla(dto.getTalla());
    producto.setPrecioBase(BigDecimal.valueOf(dto.getPrecio()));
    producto.setStockActual(dto.getStock());
    producto.setImagen(dto.getImg());
    
    productoRepository.save(producto);
}
    public void eliminar(Integer id) {
        productoRepository.deleteById(id);
    }
}