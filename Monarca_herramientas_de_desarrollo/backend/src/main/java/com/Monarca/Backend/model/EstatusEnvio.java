package com.Monarca.Backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "estatus_envio")
@Getter
@Setter
public class EstatusEnvio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estatus")
    private Integer idEstatus;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    public EstatusEnvio() {}

    public EstatusEnvio(String nombre) {
        this.nombre = nombre;
    }
}