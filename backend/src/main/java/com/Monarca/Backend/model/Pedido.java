package com.Monarca.Backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime; 
// import java.util.List; <-- Ya no necesitamos esto por ahora

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer idPedido;

    @Column(name = "fecha_pedido")
    private LocalDateTime fechaPedido;

    private Double total;

    @Transient // <-- Esto le dice a Hibernate: "No busques esta columna en la BD"
    private String estado;
    
    // Relación: Un pedido pertenece a un solo usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    @JsonIgnoreProperties({"pedidos", "password", "authorities", "hibernateLazyInitializer", "handler"}) 
    private Usuario usuario;

    // ========================================================
    // 🚨 AQUÍ ESTABA EL ERROR: ESTO DEBE ESTAR COMENTADO 🚨
    // ========================================================
    // @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // @JsonIgnoreProperties({"pedido"}) 
    // private List<DetallePedido> detalles;
    // ========================================================

    @ManyToOne
    @JoinColumn(name = "id_metodo_pago")
    private MetodoPago metodoPago;

    @ManyToOne
    @JoinColumn(name = "id_estatus_envio")
    private EstatusEnvio estatusEnvio;

    // --- Constructor ---
    public Pedido() {
        this.fechaPedido = LocalDateTime.now(); 
        this.estado = "PENDIENTE";
    }

    // --- Getters y Setters ---
    public Integer getIdPedido() { return idPedido; }
    public void setIdPedido(Integer idPedido) { this.idPedido = idPedido; }

    public LocalDateTime getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(LocalDateTime fechaPedido) { this.fechaPedido = fechaPedido; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public EstatusEnvio getEstatusEnvio() { return estatusEnvio; }
    public void setEstatusEnvio(EstatusEnvio estatusEnvio) { this.estatusEnvio = estatusEnvio; }

    // ========================================================
    // 🚨 TAMBIÉN COMENTAMOS EL GETTER DE DETALLES 🚨
    // ========================================================
    // public List<DetallePedido> getDetalles() { return detalles; }
    // public void setDetalles(List<DetallePedido> detalles) { ... }
    // ========================================================
}