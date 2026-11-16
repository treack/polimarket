package com.polimarket.polimarket_backend.entregas.dto;

import java.time.LocalDateTime;
/** La fecha corresponde al despacho, no a la recepcion por el cliente. */
public record EntregaDto(Long id, Long pedidoId, String direccion, LocalDateTime fechaSalida) { }
