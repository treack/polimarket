package com.polimarket.polimarket_backend.bodega.service;

import java.util.List;
import com.polimarket.polimarket_backend.bodega.dto.ProductoInventarioDto;

import com.polimarket.polimarket_backend.bodega.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BodegaService {
    private final StockRepository stocks;
    private final com.polimarket.polimarket_backend.bodega.repository.ProductoRepository productos;
    private final com.polimarket.polimarket_backend.bodega.repository.SalidaInventarioRepository salidas;

    public BodegaService(StockRepository stocks,
            com.polimarket.polimarket_backend.bodega.repository.ProductoRepository productos,
            com.polimarket.polimarket_backend.bodega.repository.SalidaInventarioRepository salidas) {
        this.stocks = stocks;
        this.productos = productos;
        this.salidas = salidas;
    }

    @Transactional
    public boolean registrarSalidaProductos(Long productoId, int cantidad) {
        return registrarSalidaPedido(productoId, cantidad, null);
    }

    @Transactional
    public boolean registrarSalidaPedido(Long productoId, int cantidad, Long pedidoId) {
        if (productoId == null || productoId <= 0 || cantidad <= 0) {
            throw new IllegalArgumentException("El producto y la cantidad deben ser mayores que cero.");
        }
        var resultado = stocks.buscarParaSalida(productoId);
        if (resultado.isEmpty() || resultado.get().getCantidad() < cantidad) return false;
        var stock = resultado.get();
        var fecha = java.time.LocalDateTime.now();
        stock.setCantidad(stock.getCantidad() - cantidad);
        stock.setFechaActualizacion(fecha);
        var salida = new com.polimarket.polimarket_backend.bodega.model.SalidaInventario(
                stock.getProducto(), cantidad, fecha);
        salida.setPedidoId(pedidoId);
        salidas.save(salida);
        return true;
    }

    @Transactional(readOnly = true)
    public java.util.Optional<com.polimarket.polimarket_backend.bodega.dto.ProductoPedidoDto> consultarProductoParaPedido(Long productoId) {
        return stocks.findByProductoId(productoId).map(stock -> {
            var producto = stock.getProducto();
            return new com.polimarket.polimarket_backend.bodega.dto.ProductoPedidoDto(
                    producto.getId(), producto.getNombre(), producto.getPrecio(), stock.getCantidad());
        });
    }

    @Transactional(readOnly = true)
    public List<ProductoInventarioDto> listarProductosDisponibles() {
        return stocks.listarProductosDisponibles();
    }

    @Transactional(readOnly = true)
    public boolean consultarDisponibilidad(Long productoId, int cantidad) {
        if (productoId == null || productoId <= 0) {
            throw new IllegalArgumentException("El identificador del producto debe ser mayor que cero.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad solicitada debe ser mayor que cero.");
        }
        return stocks.findByProductoId(productoId)
                .map(stock -> stock.getCantidad() >= cantidad)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public com.polimarket.polimarket_backend.bodega.dto.DisponibilidadDto consultarDetalleDisponibilidad(Long productoId, int cantidad) {
        boolean disponible = consultarDisponibilidad(productoId, cantidad);
        String nombre = productos.findById(productoId).map(producto -> producto.getNombre()).orElse(null);
        return new com.polimarket.polimarket_backend.bodega.dto.DisponibilidadDto(
                productoId, cantidad, disponible, nombre);
    }
}
