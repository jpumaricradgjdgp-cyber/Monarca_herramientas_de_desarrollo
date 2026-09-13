package com.Monarca.Backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
public class Producto {
@Column(name = "nombre", nullable = false, length = 150)
    private String nombre;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    // Relación con Familia de Productos
    @ManyToOne
    @JoinColumn(name = "id_familia", nullable = false)
    private FamiliaProducto familiaProducto;

    @Column(name = "talla", nullable = false, length = 10)
    private String talla;

    @Column(name = "color", nullable = false, length = 50)
    private String color;

    @Column(name = "sku", nullable = false, unique = true, length = 50)
    private String sku;

    @Column(name = "precio_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual = 0; // Por defecto inicia en 0 hasta que hagamos una Orden de Compra

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
    @Column(length = 1000)
    private String descripcion;

    private String imagen;
}