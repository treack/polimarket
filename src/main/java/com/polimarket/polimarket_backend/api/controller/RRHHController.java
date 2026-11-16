package com.polimarket.polimarket_backend.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import com.polimarket.polimarket_backend.rrhh.dto.AutorizacionVendedorDto;
import com.polimarket.polimarket_backend.rrhh.facade.IRRHHFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Recursos Humanos")
@RestController
@RequestMapping("/api/rrhh")
public class RRHHController {
    private final IRRHHFacade rrhh;

    public RRHHController(IRRHHFacade rrhh) {
        this.rrhh = rrhh;
    }

    @Operation(summary = "RF-10: validar autorizacion del vendedor", description = "Requiere un vendedor autorizado y su empleado activo. No inicia una sesion.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resultado de autorizacion; false si no existe o no esta autorizado.", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Identificador invalido.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/vendedores/{vendedorId}/autorizacion")
    public AutorizacionVendedorDto validarAutorizacion(@PathVariable("vendedorId") Long vendedorId) {
        return new AutorizacionVendedorDto(vendedorId, rrhh.validarAutorizacion(vendedorId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail parametrosInvalidos(IllegalArgumentException error) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, error.getMessage());
    }
}
