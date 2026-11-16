package com.polimarket.polimarket_backend.rrhh.facade;

/** Contrato publico del componente Recursos Humanos. */
public interface IRRHHFacade {

    /**
     * Verifica la autorizacion del vendedor (RF-10).
     *
     * @param vendedorId identificador obligatorio y mayor que cero
     * @return {@code true} si el vendedor esta autorizado y su empleado esta activo;
     *         {@code false} si no existe o no cumple ambas condiciones
     * @throws IllegalArgumentException si el identificador es nulo o no positivo
     */
    boolean validarAutorizacion(Long vendedorId);
}
