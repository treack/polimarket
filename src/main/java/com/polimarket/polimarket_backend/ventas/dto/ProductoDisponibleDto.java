package com.polimarket.polimarket_backend.ventas.dto;

/** Datos de consulta de un producto disponible, sin exponer la entidad persistente. */
public record ProductoDisponibleDto(Long id, String nombre, int existencias) {
}
