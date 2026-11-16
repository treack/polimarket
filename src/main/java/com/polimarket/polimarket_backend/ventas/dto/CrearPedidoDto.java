package com.polimarket.polimarket_backend.ventas.dto;

import java.util.List;
public record CrearPedidoDto(Long vendedorId, String cliente, String direccion, List<LineaPedidoDto> detalles) { }
