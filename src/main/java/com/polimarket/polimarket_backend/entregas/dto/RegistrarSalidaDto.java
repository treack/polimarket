package com.polimarket.polimarket_backend.entregas.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegistrarSalidaDto(
        @Schema(description = "ID del producto", example = "1", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long productoId,
        @Schema(description = "Unidades que salen del inventario", example = "5", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer cantidad) {
}
