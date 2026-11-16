package com.polimarket.polimarket_backend.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import com.polimarket.polimarket_backend.proveedores.dto.ProveedorDto;
import com.polimarket.polimarket_backend.proveedores.facade.IProveedoresFacade;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Proveedores")
@RestController
@RequestMapping("/api/proveedores")
public class ProveedoresController {
    private final IProveedoresFacade proveedores;

    public ProveedoresController(IProveedoresFacade proveedores) {
        this.proveedores = proveedores;
    }

    @Operation(summary = "RF-12: consultar proveedores", description = "Lista los proveedores asociados al producto, ordenados por identificador.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de proveedores; vacia si el producto no existe o no tiene asociaciones.", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Identificador invalido.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/productos/{productoId}")
    public List<ProveedorDto> consultarProveedores(@PathVariable("productoId") Long productoId) {
        return proveedores.consultarProveedores(productoId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail parametrosInvalidos(IllegalArgumentException error) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, error.getMessage());
    }
}
