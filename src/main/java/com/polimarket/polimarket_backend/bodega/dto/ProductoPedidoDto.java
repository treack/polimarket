package com.polimarket.polimarket_backend.bodega.dto;

import java.math.BigDecimal;
/** Instantanea del producto para preparar un pedido, sin reservar stock. */
public record ProductoPedidoDto(Long id, String nombre, BigDecimal precio, int existencias) { }
