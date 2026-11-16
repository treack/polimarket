package com.polimarket.polimarket_backend.proveedores.service;

import com.polimarket.polimarket_backend.proveedores.dto.ProveedorDto;
import com.polimarket.polimarket_backend.proveedores.repository.ProductoProveedorRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProveedoresService {
    private final ProductoProveedorRepository relaciones;

    public ProveedoresService(ProductoProveedorRepository relaciones) {
        this.relaciones = relaciones;
    }

    @Transactional(readOnly = true)
    public List<ProveedorDto> consultarProveedores(Long productoId) {
        if (productoId == null || productoId <= 0) {
            throw new IllegalArgumentException("El identificador del producto debe ser mayor que cero.");
        }
        return relaciones.consultarProveedores(productoId);
    }
}
