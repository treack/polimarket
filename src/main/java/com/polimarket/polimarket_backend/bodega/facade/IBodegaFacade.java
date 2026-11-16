package com.polimarket.polimarket_backend.bodega.facade;

import java.util.List;
import com.polimarket.polimarket_backend.bodega.dto.ProductoInventarioDto;

/**
 * Contrato publico del componente Bodega para consultar inventario y registrar salidas.
 */
public interface IBodegaFacade {

    /** Consulta RF-02 con el nombre del producto; nombre nulo si no existe. */
    com.polimarket.polimarket_backend.bodega.dto.DisponibilidadDto consultarDetalleDisponibilidad(Long productoId, int cantidad);

    /** Consulta de datos de producto para pedidos, sin exponer entidades persistentes. */
    java.util.Optional<com.polimarket.polimarket_backend.bodega.dto.ProductoPedidoDto> consultarProductoParaPedido(Long productoId);

    /** Salida vinculada al pedido. Debe ejecutarse dentro de la transaccion de despacho. */
    boolean registrarSalidaPedido(Long productoId, int cantidad, Long pedidoId);

    /** Registra un movimiento de salida y descuenta stock atomicamente.
     * @return false si no existe stock o es insuficiente, sin efectuar cambios
     * @throws IllegalArgumentException si el ID es nulo/no positivo o cantidad no positiva
     */
    boolean registrarSalidaProductos(Long productoId, int cantidad);

    /**
     * Lista los productos con stock mayor que cero, ordenados por identificador.
     * Permite a otros componentes consultar el inventario sin acceder a entidades.
     * @return lista de productos; vacia si no hay existencias, nunca nula
     */
    List<ProductoInventarioDto> listarProductosDisponibles();

    /**
     * Consulta si hay existencias suficientes para la cantidad solicitada (RF-02).
     * Esta operacion no reserva ni descuenta productos del inventario.
     *
     * @param productoId identificador del producto, obligatorio y mayor que cero
     * @param cantidad cantidad solicitada, mayor que cero
     * @return {@code true} si el producto existe y tiene stock suficiente;
     *         {@code false} si no existe o sus existencias son insuficientes
     * @throws IllegalArgumentException si el identificador es nulo o no positivo,
     *                                  o la cantidad no es positiva
     */
    boolean consultarDisponibilidad(Long productoId, int cantidad);
}
