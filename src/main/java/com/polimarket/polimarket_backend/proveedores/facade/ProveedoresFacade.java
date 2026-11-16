package com.polimarket.polimarket_backend.proveedores.facade;

import com.polimarket.polimarket_backend.proveedores.dto.ProveedorDto;
import com.polimarket.polimarket_backend.proveedores.service.ProveedoresService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProveedoresFacade implements IProveedoresFacade {
    private final ProveedoresService proveedores;

    public ProveedoresFacade(ProveedoresService proveedores) {
        this.proveedores = proveedores;
    }

    @Override
    public List<ProveedorDto> consultarProveedores(Long productoId) {
        return proveedores.consultarProveedores(productoId);
    }
}
