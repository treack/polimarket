package com.polimarket.polimarket_backend.api.controller;

import com.polimarket.polimarket_backend.ventas.service.OperacionPedidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.polimarket.polimarket_backend.api.controller")
public class PedidosExceptionHandler {
    @ExceptionHandler(OperacionPedidoException.class)
    public ProblemDetail operacion(OperacionPedidoException error) {
        return ProblemDetail.forStatusAndDetail(error.getStatus(), error.getMessage());
    }
    @ExceptionHandler({ConcurrencyFailureException.class, DataIntegrityViolationException.class})
    public ProblemDetail conflicto(RuntimeException error) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "La operacion entro en conflicto con otro registro. Actualiza los pedidos antes de reintentar.");
    }
}
