package com.polimarket.polimarket_backend.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import com.polimarket.polimarket_backend.bodega.dto.DisponibilidadDto;
import com.polimarket.polimarket_backend.bodega.facade.IBodegaFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Bodega")
@RestController
@RequestMapping("/api/bodega")
public class BodegaController {
    private final IBodegaFacade bodega;

    public BodegaController(IBodegaFacade bodega) {
        this.bodega = bodega;
    }

    @Operation(summary = "RF-02: consultar disponibilidad", description = "Consulta si existe stock suficiente. No reserva ni descuenta unidades.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resultado de disponibilidad; false si no existe stock suficiente o el producto no existe.", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Identificador o cantidad invalidos.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/productos/{productoId}/disponibilidad")
    public DisponibilidadDto consultarDisponibilidad(
            @PathVariable("productoId") Long productoId,
            @RequestParam("cantidad") int cantidad) {
        return new DisponibilidadDto(productoId, cantidad,
                bodega.consultarDisponibilidad(productoId, cantidad));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail parametrosInvalidos(IllegalArgumentException error) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, error.getMessage());
    }
}
