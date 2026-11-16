package com.polimarket.polimarket_backend.ventas.service;

import com.polimarket.polimarket_backend.bodega.facade.IBodegaFacade;
import com.polimarket.polimarket_backend.bodega.model.Producto;
import com.polimarket.polimarket_backend.rrhh.facade.IRRHHFacade;
import com.polimarket.polimarket_backend.rrhh.model.Vendedor;
import com.polimarket.polimarket_backend.ventas.dto.*;
import com.polimarket.polimarket_backend.ventas.model.*;
import com.polimarket.polimarket_backend.ventas.repository.ContratoRepository;
import com.polimarket.polimarket_backend.ventas.repository.PedidoRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidosService {
    private final PedidoRepository pedidos;
    private final ContratoRepository contratos;
    private final IBodegaFacade bodega;
    private final IRRHHFacade rrhh;
    private final EntityManager em;

    public PedidosService(PedidoRepository pedidos, ContratoRepository contratos,
            IBodegaFacade bodega, IRRHHFacade rrhh, EntityManager em) {
        this.pedidos = pedidos; this.contratos = contratos;
        this.bodega = bodega; this.rrhh = rrhh; this.em = em;
    }

    private void validarVendedor(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("El vendedor es obligatorio y debe ser positivo.");
        if (!rrhh.validarAutorizacion(id)) throw new OperacionPedidoException(HttpStatus.FORBIDDEN, "El vendedor no esta autorizado o su empleado esta inactivo.");
    }

    @Transactional
    public PedidoDto crearPedido(CrearPedidoDto solicitud) {
        if (solicitud == null) throw new IllegalArgumentException("El pedido es obligatorio.");
        validarVendedor(solicitud.vendedorId());
        String cliente = texto(solicitud.cliente(), "El nombre del cliente", 150);
        String direccion = texto(solicitud.direccion(), "La direccion", 255);
        if (solicitud.detalles() == null || solicitud.detalles().isEmpty() || solicitud.detalles().size() > 100)
            throw new IllegalArgumentException("El pedido debe tener entre 1 y 100 productos.");
        var cantidades = new TreeMap<Long, Integer>();
        for (var linea : solicitud.detalles()) {
            if (linea == null || linea.productoId() == null || linea.productoId() <= 0
                    || linea.cantidad() == null || linea.cantidad() <= 0)
                throw new IllegalArgumentException("Cada producto debe tener un ID y una cantidad positivos.");
            if (cantidades.putIfAbsent(linea.productoId(), linea.cantidad()) != null)
                throw new IllegalArgumentException("Un producto no puede aparecer repetido en el pedido.");
        }
        Pedido pedido = new Pedido();
        pedido.setVendedor(em.getReference(Vendedor.class, solicitud.vendedorId()));
        pedido.setDireccion(direccion);
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        for (var linea : cantidades.entrySet()) {
            var producto = bodega.consultarProductoParaPedido(linea.getKey()).orElseThrow(
                    () -> new OperacionPedidoException(HttpStatus.CONFLICT, "Producto sin inventario: " + linea.getKey()));
            if (producto.existencias() < linea.getValue())
                throw new OperacionPedidoException(HttpStatus.CONFLICT, "Stock insuficiente para " + producto.nombre());
            if (producto.precio() == null || producto.precio().signum() < 0)
                throw new OperacionPedidoException(HttpStatus.CONFLICT, "El producto no tiene un precio valido: " + producto.nombre());
            DetallePedido detalle = new DetallePedido();
            detalle.setProducto(em.getReference(Producto.class, producto.id()));
            detalle.setCantidad(linea.getValue());
            detalle.setPrecioUnitario(producto.precio());
            pedido.getDetalles().add(detalle);
        }
        Contrato contrato = new Contrato();
        contrato.setNumero("PM-" + UUID.randomUUID());
        contrato.setCliente(cliente);
        contrato.setFecha(LocalDate.now());
        contrato.setEstado(EstadoContrato.CONFIRMADO);
        pedido.setContrato(contratos.save(contrato));
        return dto(pedidos.saveAndFlush(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoDto> listarPedidos(Long vendedorId) {
        validarVendedor(vendedorId);
        return pedidos.findTop100ByVendedorIdOrderByIdDesc(vendedorId).stream().map(this::dto).toList();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public PedidoDto despacharPedido(Long pedidoId) {
        if (pedidoId == null || pedidoId <= 0) throw new IllegalArgumentException("El ID del pedido debe ser positivo.");
        Pedido pedido = pedidos.buscarParaDespacho(pedidoId).orElseThrow(
                () -> new OperacionPedidoException(HttpStatus.NOT_FOUND, "El pedido no existe."));
        if (pedido.getEstado() != EstadoPedido.PENDIENTE)
            throw new OperacionPedidoException(HttpStatus.CONFLICT, "El pedido ya fue despachado.");
        if (pedido.getDetalles().isEmpty())
            throw new OperacionPedidoException(HttpStatus.CONFLICT, "El pedido no tiene productos.");
        // Orden estable de bloqueo para evitar interbloqueos entre pedidos multiproducto.
        var detalles = pedido.getDetalles().stream()
                .sorted(java.util.Comparator.comparing(d -> d.getProducto().getId())).toList();
        for (var detalle : detalles) {
            if (detalle.getCantidad() <= 0 || !bodega.registrarSalidaPedido(
                    detalle.getProducto().getId(), detalle.getCantidad(), pedidoId))
                throw new OperacionPedidoException(HttpStatus.CONFLICT,
                        "Stock insuficiente para despachar " + detalle.getProducto().getNombre() + ". No se registro ninguna salida.");
        }
        pedido.setEstado(EstadoPedido.DESPACHADO);
        return dto(pedido);
    }

    private String texto(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank() || valor.strip().length() > maximo)
            throw new IllegalArgumentException(campo + " es obligatorio y admite hasta " + maximo + " caracteres.");
        return valor.strip();
    }

    private PedidoDto dto(Pedido pedido) {
        var detalles = pedido.getDetalles().stream().map(d -> new DetallePedidoDto(
                d.getProducto().getId(), d.getProducto().getNombre(), d.getCantidad(), d.getPrecioUnitario())).toList();
        BigDecimal total = detalles.stream()
                .map(d -> d.precioUnitario().multiply(BigDecimal.valueOf(d.cantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PedidoDto(pedido.getId(), pedido.getVendedor().getId(), pedido.getContrato().getCliente(),
                pedido.getDireccion(), pedido.getFecha(), pedido.getEstado(), detalles, total);
    }
}
