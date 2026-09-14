package com.Monarca.Backend.service;

import com.Monarca.Backend.model.Categoria;
import com.Monarca.Backend.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    // Aquí inyectamos a nuestro "bibliotecario" (El DAO)
    @Autowired
    private CategoriaRepository categoriaRepository;

    // Método para obtener TODAS las categorías
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    // Método para guardar una NUEVA categoría
    public Categoria guardar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    // Método para buscar una categoría por su ID
    public Optional<Categoria> buscarPorId(Integer id) {
        return categoriaRepository.findById(id);
    }

    // Método para eliminar una categoría
    public void eliminar(Integer id) {
        categoriaRepository.deleteById(id);
    }
}