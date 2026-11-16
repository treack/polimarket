package com.polimarket.polimarket_backend;

import com.polimarket.polimarket_backend.bodega.model.Producto;
import com.polimarket.polimarket_backend.bodega.model.Stock;
import com.polimarket.polimarket_backend.bodega.repository.StockRepository;
import com.polimarket.polimarket_backend.proveedores.model.ProductoProveedor;
import com.polimarket.polimarket_backend.proveedores.model.Proveedor;
import com.polimarket.polimarket_backend.proveedores.repository.ProductoProveedorRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class PersistenciaTests {
    @Autowired EntityManager em;
    @Autowired StockRepository stocks;
    @Autowired ProductoProveedorRepository proveedores;

    @Test
    void consultaStockYProveedoresDelProductoPersistido() {
        Producto producto = new Producto();
        producto.setNombre("Arroz");
        producto.setPrecio(new java.math.BigDecimal("4500.00"));
        em.persist(producto);
        Stock stock = new Stock();
        stock.setProducto(producto);
        stock.setCantidad(20);
        em.persist(stock);
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre("Distribuidor");
        em.persist(proveedor);
        ProductoProveedor relacion = new ProductoProveedor();
        relacion.setProducto(producto);
        relacion.setProveedor(proveedor);
        em.persist(relacion);
        em.flush();
        em.clear();

        Stock recuperado = stocks.findByProductoId(producto.getId()).orElseThrow();
        assertEquals(20, recuperado.getCantidad());
        assertEquals("Arroz", recuperado.getProducto().getNombre());
        assertEquals(new java.math.BigDecimal("4500.00"), recuperado.getProducto().getPrecio());
        assertEquals("Distribuidor", proveedores.findByProductoId(producto.getId())
                .getFirst().getProveedor().getNombre());
    }
}
