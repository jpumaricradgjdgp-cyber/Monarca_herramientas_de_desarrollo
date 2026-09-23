package com.Monarca.Backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemCarritoDto {

    // Nuevo sistema
    private Long idVariante;

    // Compatibilidad temporal con el frontend antiguo
    private Long idProducto;

    private Integer cantidad;
}