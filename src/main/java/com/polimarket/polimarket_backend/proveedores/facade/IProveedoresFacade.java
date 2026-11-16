package com.polimarket.polimarket_backend.proveedores.facade;

import com.polimarket.polimarket_backend.proveedores.dto.ProveedorDto;
import java.util.List;

/** Contrato publico del componente Proveedores. */
public interface IProveedoresFacade {

    /**
     * Consulta los proveedores asociados a un producto (RF-12).
     *
     * @param productoId identificador obligatorio y mayor que cero
     * @return lista de proveedores; vacia si el producto no existe o no tiene
     *         proveedores asociados, nunca nula
     * @throws IllegalArgumentException si el identificador es nulo o no positivo
     */
    List<ProveedorDto> consultarProveedores(Long productoId);
}
