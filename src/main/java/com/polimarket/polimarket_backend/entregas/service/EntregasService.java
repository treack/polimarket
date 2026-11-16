package com.polimarket.polimarket_backend.entregas.service;

import com.polimarket.polimarket_backend.bodega.facade.IBodegaFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EntregasService {
    private final IBodegaFacade bodega;
    private final com.polimarket.polimarket_backend.ventas.facade.IVentasFacade ventas;
    private final com.polimarket.polimarket_backend.entregas.repository.EntregaRepository entregas;
    private final jakarta.persistence.EntityManager em;

    public EntregasService(IBodegaFacade bodega,
            com.polimarket.polimarket_backend.ventas.facade.IVentasFacade ventas,
            com.polimarket.polimarket_backend.entregas.repository.EntregaRepository entregas,
            jakarta.persistence.EntityManager em) {
        this.bodega = bodega; this.ventas = ventas; this.entregas = entregas; this.em = em;
    }

    @Transactional
    public com.polimarket.polimarket_backend.entregas.dto.EntregaDto registrarEntrega(Long pedidoId) {
        var pedido = ventas.despacharPedido(pedidoId);
        var entrega = new com.polimarket.polimarket_backend.entregas.model.Entrega();
        entrega.setPedido(em.getReference(com.polimarket.polimarket_backend.ventas.model.Pedido.class, pedidoId));
        entrega.setDireccion(pedido.direccion());
        entrega.setFechaSalida(java.time.LocalDateTime.now());
        entregas.saveAndFlush(entrega);
        return new com.polimarket.polimarket_backend.entregas.dto.EntregaDto(
                entrega.getId(), pedidoId, entrega.getDireccion(), entrega.getFechaSalida());
    }

    @Transactional
    public boolean registrarSalidaProductos(Long productoId, int cantidad) {
        return bodega.registrarSalidaProductos(productoId, cantidad);
    }

    public java.util.List<com.polimarket.polimarket_backend.ventas.dto.PedidoDto> listarPendientes() {
        return ventas.listarPendientes();
    }
}
