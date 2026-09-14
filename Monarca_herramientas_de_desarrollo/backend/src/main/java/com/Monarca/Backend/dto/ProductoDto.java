package com.Monarca.Backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductoDto {
    private String nombre;
    private String categoria;
    private String talla;
    private Double precio;
    private Integer stock;
    private String img;
}