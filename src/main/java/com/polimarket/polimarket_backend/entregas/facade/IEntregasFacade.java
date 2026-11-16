package com.polimarket.polimarket_backend.entregas.facade;

/** Contrato publico del componente Entregas. */
public interface IEntregasFacade {
    java.util.List<com.polimarket.polimarket_backend.ventas.dto.PedidoDto> listarPendientes();
    /** Registra el despacho de un pedido, su entrega y todas sus salidas atomicamente. */
    com.polimarket.polimarket_backend.entregas.dto.EntregaDto registrarEntrega(Long pedidoId);


    /**
     * Registra la salida de un producto y coordina el descuento de inventario
     * con Bodega en una misma transaccion (RF-13).
     *
     * @param productoId identificador obligatorio y mayor que cero
     * @param cantidad unidades que salen del inventario, mayor que cero
     * @return {@code true} si se registraron la salida y el descuento;
     *         {@code false} si el producto no existe o no hay stock suficiente,
     *         sin registrar movimientos ni modificar existencias
     * @throws IllegalArgumentException si el identificador es nulo o no positivo,
     *                                  o la cantidad no es positiva
     */
    boolean registrarSalidaProductos(Long productoId, int cantidad);
}
