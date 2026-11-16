package com.polimarket.polimarket_backend.bodega.facade;

import java.util.List;
import com.polimarket.polimarket_backend.bodega.dto.ProductoInventarioDto;

import com.polimarket.polimarket_backend.bodega.service.BodegaService;
import org.springframework.stereotype.Component;

@Component
public class BodegaFacade implements IBodegaFacade {
    private final BodegaService bodega;

    public BodegaFacade(BodegaService bodega) {
        this.bodega = bodega;
    }

    @Override
    public List<ProductoInventarioDto> listarProductosDisponibles() {
        return bodega.listarProductosDisponibles();
    }

    @Override
    public boolean registrarSalidaProductos(Long productoId, int cantidad) {
        return bodega.registrarSalidaProductos(productoId, cantidad);
    }

    @Override
    public boolean registrarSalidaPedido(Long productoId, int cantidad, Long pedidoId) {
        return bodega.registrarSalidaPedido(productoId, cantidad, pedidoId);
    }

    @Override
    public java.util.Optional<com.polimarket.polimarket_backend.bodega.dto.ProductoPedidoDto> consultarProductoParaPedido(Long productoId) {
        return bodega.consultarProductoParaPedido(productoId);
    }

    @Override
    public boolean consultarDisponibilidad(Long productoId, int cantidad) {
        return bodega.consultarDisponibilidad(productoId, cantidad);
    }
}
