package com.Monarca.Backend.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class PedidoRequestDto {
    private Integer idUsuario;
    private Double total;             // <--- Faltaba el total
    private Integer idMetodoPago;
    private Integer idEstatusEnvio;   // <--- ¡Este era el que causaba el error 400!
    private Integer idTiendaRetiro;   // Puede ser null
    private List<ItemCarritoDto> items;
}