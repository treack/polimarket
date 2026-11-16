package com.polimarket.polimarket_backend.rrhh.service;

import com.polimarket.polimarket_backend.rrhh.repository.VendedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RRHHService {
    private final VendedorRepository vendedores;

    public RRHHService(VendedorRepository vendedores) {
        this.vendedores = vendedores;
    }

    @Transactional(readOnly = true)
    public boolean validarAutorizacion(Long vendedorId) {
        if (vendedorId == null || vendedorId <= 0) {
            throw new IllegalArgumentException("El identificador del vendedor debe ser mayor que cero.");
        }
        return vendedores.findById(vendedorId)
                .map(vendedor -> vendedor.isAutorizado() && vendedor.getEmpleado().isActivo())
                .orElse(false);
    }
}
