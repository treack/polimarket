package com.polimarket.polimarket_backend.ventas.facade;

import com.polimarket.polimarket_backend.ventas.dto.ProductoDisponibleDto;
import java.util.List;

/** Contrato publico del componente Ventas. */
public interface IVentasFacade {

    com.polimarket.polimarket_backend.ventas.dto.PedidoDto crearPedido(com.polimarket.polimarket_backend.ventas.dto.CrearPedidoDto solicitud);
    List<com.polimarket.polimarket_backend.ventas.dto.PedidoDto> listarPedidos(Long vendedorId);
    /** Operacion interna para Entregas: bloquea el pedido y descuenta inventario una sola vez. */
    com.polimarket.polimarket_backend.ventas.dto.PedidoDto despacharPedido(Long pedidoId);

    /**
     * Lista los productos con existencias disponibles para la venta (RF-05).
     *
     * @return lista de productos; vacia si ninguno esta disponible, nunca nula
     */
    List<ProductoDisponibleDto> listarProductos();
}
