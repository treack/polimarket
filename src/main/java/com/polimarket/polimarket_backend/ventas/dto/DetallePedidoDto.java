package com.polimarket.polimarket_backend.ventas.dto;

import java.math.BigDecimal;
public record DetallePedidoDto(Long productoId, String nombre, int cantidad, BigDecimal precioUnitario) { }
