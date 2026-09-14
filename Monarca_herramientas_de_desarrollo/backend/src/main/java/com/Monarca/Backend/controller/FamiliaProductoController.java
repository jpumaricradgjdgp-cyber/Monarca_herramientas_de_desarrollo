package com.Monarca.Backend.controller;

import com.Monarca.Backend.model.FamiliaProducto;
import com.Monarca.Backend.repository.FamiliaProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/familias")
@CrossOrigin(origins = "*")
public class FamiliaProductoController {

    @Autowired
    private FamiliaProductoRepository familiaRepository;

    // Método público: Cualquiera puede ver la lista de familias de productos
    @GetMapping
    public List<FamiliaProducto> listarTodas() {
        return familiaRepository.findAll();
    }

    // Método protegido: Solo el ADMIN puede crear una nueva familia de productos
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public FamiliaProducto guardar(@RequestBody FamiliaProducto familia) {
        return familiaRepository.save(familia);
    }
}