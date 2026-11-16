package com.polimarket.polimarket_backend.ventas.dto;

import com.polimarket.polimarket_backend.ventas.model.EstadoPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
public record PedidoDto(Long id, Long vendedorId, String cliente, String direccion,
        LocalDateTime fecha, EstadoPedido estado, List<DetallePedidoDto> detalles, BigDecimal total) { }
