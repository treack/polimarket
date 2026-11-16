package com.polimarket.polimarket_backend.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import com.polimarket.polimarket_backend.entregas.facade.IEntregasFacade;
import com.polimarket.polimarket_backend.entregas.dto.RegistrarSalidaDto;
import com.polimarket.polimarket_backend.entregas.dto.ResultadoSalidaDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Entregas")
@RestController
@RequestMapping("/api/entregas")
public class EntregasController {
    private final IEntregasFacade entregas;

    public EntregasController(IEntregasFacade entregas) { this.entregas = entregas; }

    @Operation(summary = "Registrar entrega y salida de un pedido", description = "Registra el despacho y descuenta todos los productos en una transaccion. Un pedido solo puede despacharse una vez. No representa confirmacion de recepcion del cliente.")
    @PostMapping("/pedidos/{pedidoId}")
    public ResponseEntity<com.polimarket.polimarket_backend.entregas.dto.EntregaDto> registrarEntrega(
            @PathVariable("pedidoId") Long pedidoId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(entregas.registrarEntrega(pedidoId));
    }

    @Operation(summary = "RF-13: registrar salida de productos", description = "Registra el movimiento y descuenta existencias en una misma transaccion. Cada ejecucion exitosa modifica el inventario.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Salida registrada y stock actualizado.", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Cuerpo JSON, identificador o cantidad invalidos.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Producto sin stock suficiente, inexistente o conflicto de concurrencia.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ResultadoSalidaDto.class)),
            @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
        })
    })
    @PostMapping("/salidas")
    public ResponseEntity<ResultadoSalidaDto> registrarSalida(@RequestBody RegistrarSalidaDto solicitud) {
        if (solicitud == null || solicitud.cantidad() == null) {
            throw new IllegalArgumentException("La cantidad es obligatoria.");
        }
        boolean registrada = entregas.registrarSalidaProductos(solicitud.productoId(), solicitud.cantidad());
        return ResponseEntity.status(registrada ? HttpStatus.OK : HttpStatus.CONFLICT)
                .body(new ResultadoSalidaDto(solicitud.productoId(), solicitud.cantidad(), registrada));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail parametrosInvalidos(IllegalArgumentException error) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, error.getMessage());
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    public ProblemDetail conflictoConcurrente(ConcurrencyFailureException error) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "No se pudo completar la salida por una operacion concurrente. Consulte el inventario antes de reintentar.");
    }
}
