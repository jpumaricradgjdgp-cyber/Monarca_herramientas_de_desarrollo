package com.Monarca.Backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "metodos_pago")
@Getter
@Setter
public class MetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metodo")
    private Integer idMetodo;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    // Constructor vacío requerido por JPA
    public MetodoPago() {}

    // Constructor opcional para facilitar la creación
    public MetodoPago(String nombre) {
        this.nombre = nombre;
    }
}