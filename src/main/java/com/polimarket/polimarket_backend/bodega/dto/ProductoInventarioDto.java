package com.polimarket.polimarket_backend.bodega.dto;

/** Datos publicos de un producto con stock positivo. */
public record ProductoInventarioDto(Long id, String nombre, int existencias) {
}
