package com.polimarket.polimarket_backend.ventas.facade;

import com.polimarket.polimarket_backend.bodega.facade.IBodegaFacade;
import com.polimarket.polimarket_backend.ventas.dto.ProductoDisponibleDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class VentasFacade implements IVentasFacade {
    private final IBodegaFacade bodega;
    private final com.polimarket.polimarket_backend.ventas.service.PedidosService pedidos;

    public VentasFacade(IBodegaFacade bodega,
            com.polimarket.polimarket_backend.ventas.service.PedidosService pedidos) {
        this.bodega = bodega;
        this.pedidos = pedidos;
    }

    @Override
    public com.polimarket.polimarket_backend.ventas.dto.PedidoDto crearPedido(com.polimarket.polimarket_backend.ventas.dto.CrearPedidoDto solicitud) {
        return pedidos.crearPedido(solicitud);
    }
    @Override
    public List<com.polimarket.polimarket_backend.ventas.dto.PedidoDto> listarPedidos(Long vendedorId) {
        return pedidos.listarPedidos(vendedorId);
    }
    @Override
    public com.polimarket.polimarket_backend.ventas.dto.PedidoDto despacharPedido(Long pedidoId) {
        return pedidos.despacharPedido(pedidoId);
    }

    @Override
    public List<ProductoDisponibleDto> listarProductos() {
        return bodega.listarProductosDisponibles().stream()
                .map(producto -> new ProductoDisponibleDto(
                        producto.id(), producto.nombre(), producto.existencias()))
                .toList();
    }
}
