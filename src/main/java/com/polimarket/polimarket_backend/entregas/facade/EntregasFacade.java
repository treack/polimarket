package com.polimarket.polimarket_backend.entregas.facade;

import com.polimarket.polimarket_backend.entregas.service.EntregasService;
import org.springframework.stereotype.Component;

@Component
public class EntregasFacade implements IEntregasFacade {
    private final EntregasService entregas;

    public EntregasFacade(EntregasService entregas) { this.entregas = entregas; }

    @Override
    public com.polimarket.polimarket_backend.entregas.dto.EntregaDto registrarEntrega(Long pedidoId) {
        return entregas.registrarEntrega(pedidoId);
    }

    @Override
    public boolean registrarSalidaProductos(Long productoId, int cantidad) {
        return entregas.registrarSalidaProductos(productoId, cantidad);
    }
}
