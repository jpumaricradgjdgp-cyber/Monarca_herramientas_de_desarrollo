package com.Monarca.Backend.controller;

import com.Monarca.Backend.model.Categoria;
import com.Monarca.Backend.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*") // ¡Súper importante para que tu JavaScript pueda conectarse sin bloqueos de seguridad!
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    // Cuando tu frontend haga un GET a http://localhost:8080/api/categorias (PÚBLICO)
    @GetMapping
    public List<Categoria> listarTodas() {
        return categoriaService.listarTodas();
    }

    // Cuando tu frontend haga un POST enviando un JSON (SOLO ADMIN)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public Categoria guardar(@RequestBody Categoria categoria) {
        return categoriaService.guardar(categoria);
    }

    // Cuando tu frontend haga un GET buscando una sola (ej. http://localhost:8080/api/categorias/1) (PÚBLICO)
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> buscarPorId(@PathVariable Integer id) {
        Optional<Categoria> categoria = categoriaService.buscarPorId(id);
        if (categoria.isPresent()) {
            return ResponseEntity.ok(categoria.get());
        } else {
            return ResponseEntity.notFound().build(); // Devuelve un error 404 si no existe
        }
    }

    // Cuando tu frontend haga un DELETE (ej. http://localhost:8080/api/categorias/1) (SOLO ADMIN)
  
}