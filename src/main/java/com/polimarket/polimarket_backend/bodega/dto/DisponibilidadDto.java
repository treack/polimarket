package com.polimarket.polimarket_backend.bodega.dto;

/** Resultado de consulta; no representa una reserva del inventario. */
public record DisponibilidadDto(Long productoId, int cantidadSolicitada, boolean disponible, String nombreProducto) {
}
