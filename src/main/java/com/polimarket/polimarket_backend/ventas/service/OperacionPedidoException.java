package com.polimarket.polimarket_backend.ventas.service;

import org.springframework.http.HttpStatus;
public class OperacionPedidoException extends RuntimeException {
    private final HttpStatus status;
    public OperacionPedidoException(HttpStatus status, String mensaje) { super(mensaje); this.status = status; }
    public HttpStatus getStatus() { return status; }
}
