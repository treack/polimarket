package com.polimarket.polimarket_backend.rrhh.facade;

import com.polimarket.polimarket_backend.rrhh.service.RRHHService;
import org.springframework.stereotype.Component;

@Component
public class RRHHFacade implements IRRHHFacade {
    private final RRHHService rrhh;

    public RRHHFacade(RRHHService rrhh) {
        this.rrhh = rrhh;
    }

    @Override
    public boolean validarAutorizacion(Long vendedorId) {
        return rrhh.validarAutorizacion(vendedorId);
    }
}
