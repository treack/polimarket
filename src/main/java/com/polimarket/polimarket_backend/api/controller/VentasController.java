package com.polimarket.polimarket_backend.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.polimarket.polimarket_backend.ventas.dto.ProductoDisponibleDto;
import com.polimarket.polimarket_backend.ventas.facade.IVentasFacade;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import com.polimarket.polimarket_backend.ventas.dto.CrearPedidoDto;
import com.polimarket.polimarket_backend.ventas.dto.PedidoDto;

@Tag(name = "Ventas")
@RestController
@RequestMapping("/api/ventas")
public class VentasController {
    private final IVentasFacade ventas;

    public VentasController(IVentasFacade ventas) {
        this.ventas = ventas;
    }

    @Operation(summary = "Crear pedido", description = "Valida el vendedor y las existencias, guarda precios actuales y crea un pedido pendiente. No reserva ni descuenta stock.")
    @PostMapping("/pedidos")
    public ResponseEntity<PedidoDto> crearPedido(@RequestBody CrearPedidoDto solicitud) {
        var pedido = ventas.crearPedido(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    @Operation(summary = "Consultar pedidos del vendedor", description = "Devuelve los ultimos 100 pedidos, incluyendo su estado y total.")
    @GetMapping("/pedidos")
    public List<PedidoDto> listarPedidos(@RequestParam("vendedorId") Long vendedorId) {
        return ventas.listarPedidos(vendedorId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail parametrosInvalidos(IllegalArgumentException error) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, error.getMessage());
    }

    @Operation(summary = "RF-05: listar productos disponibles", description = "Devuelve productos con stock positivo, consultando la fachada de Bodega.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de productos disponibles; vacia si no hay existencias.", useReturnTypeSchema = true)
    })
    @GetMapping("/productos")
    public List<ProductoDisponibleDto> listarProductos() {
        return ventas.listarProductos();
    }
}
