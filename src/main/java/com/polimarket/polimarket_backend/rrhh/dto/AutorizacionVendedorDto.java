package com.polimarket.polimarket_backend.rrhh.dto;

/** Resultado de la consulta RF-10; no constituye una sesion autenticada. */
public record AutorizacionVendedorDto(Long vendedorId, boolean autorizado) {
}
